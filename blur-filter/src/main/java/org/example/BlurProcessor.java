package org.example;

import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.domain.Filter;
import org.example.kafka.KafkaDoneImage;
import org.example.kafka.KafkaImageFiltersRequest;
import org.example.kafka.KafkaProducer;
import org.example.kafka.ProcessedImageId;
import org.example.minio.MinioService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@RequiredArgsConstructor
public class BlurProcessor {
  private final KafkaProducer kafkaProducer;
  private final ProcessedImageRepository repository;
  private final MinioService minioService;

  /**
   * Consume kafka messages.
   *
   * @param record         record
   * @param acknowledgment acknowledgment
   */
  @KafkaListener(
          topics = "${application.kafka.topic-wip}",
          groupId = "${application.kafka.group-id}",
          concurrency = "${application.kafka.partitions}",
          properties = {
                  ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG + "=false",
                  ConsumerConfig.ISOLATION_LEVEL_CONFIG + "=read_committed",
                  ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG
                  + "=org.apache.kafka.clients.consumer.RoundRobinAssignor"
          }
  )
  public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) throws Exception {
    var request = new Gson().fromJson(record.value(), KafkaImageFiltersRequest.class);
    log.info("Получено следующее сообщение из топика {}:\nkey: {},\nvalue: {}",
            record.topic(), record.key(), request);

    if (request.getFilters()[0] != Filter.Blur) {
      acknowledgment.acknowledge();
      return;
    }

    if (repository.existsById(new ProcessedImageId(request.getRequestId(), request.getImageId()))) {
      acknowledgment.acknowledge();
      return;
    }

    var isLastFilter = request.getFilters().length == 1;
    var image = minioService.downloadImage(request.getImageId().toString());
    var metaInfo = minioService.getImageMeta(request.getImageId().toString());
    var contentType = metaInfo.contentType().split("/")[1];
    var processedImageBytes = processImage(image, contentType);

    var newImageId = minioService.uploadImage(processedImageBytes, metaInfo, !isLastFilter);
    var processedImage = new ProcessedImage()
            .setRequestId(request.getRequestId())
            .setImageId(newImageId);
    repository.save(processedImage);

    if (isLastFilter) {
      var kafkaDoneMessage = new KafkaDoneImage(newImageId, request.getRequestId());
      kafkaProducer.sendDone(new Gson().toJson(kafkaDoneMessage));
    } else {
      request.setImageId(newImageId);
      request.setFilters(Arrays.copyOfRange(request.getFilters(), 1, request.getFilters().length));
      kafkaProducer.sendWip(new Gson().toJson(request));
    }

    acknowledgment.acknowledge();
  }

  private byte[] processImage(byte[] image, String contentType) throws IOException, InterruptedException {
    var inputStream = new ByteArrayInputStream(image);
    var originalImage = ImageIO.read(inputStream);
    var width = originalImage.getWidth();
    var height = originalImage.getHeight();
    var blurredImage = new BufferedImage(width, height, originalImage.getType());

    var numThreads = Runtime.getRuntime().availableProcessors();
    var executor = Executors.newFixedThreadPool(numThreads);

    var radius = 9;
    for (int y = 0; y < height; y++) {
      var finalY = y;
      executor.submit(() -> {
        for (int x = 0; x < width; x++) {
          int a = 0, r = 0, g = 0, b = 0;
          int count = 0;

          for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
              int ix = x + dx;
              int iy = finalY + dy;

              if (ix >= 0 && ix < width && iy >= 0 && iy < height) {
                var rgb = new Color(originalImage.getRGB(ix, iy));
                a += rgb.getAlpha();
                r += rgb.getRed();
                g += rgb.getGreen();
                b += rgb.getBlue();
                count++;
              }
            }
          }

          int newA = a / count;
          int newR = r / count;
          int newG = g / count;
          int newB = b / count;

          var blurredRgb = new Color(newR, newG, newB, newA);
          blurredImage.setRGB(x, finalY, blurredRgb.getRGB());
        }
      });
    }

    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.HOURS);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(blurredImage, contentType, outputStream);
    return outputStream.toByteArray();
  }
}
