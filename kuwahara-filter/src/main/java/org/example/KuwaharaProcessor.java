package org.example;

import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.checkerframework.checker.units.qual.C;
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
public class KuwaharaProcessor {
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

    if (request.getFilters()[0] != Filter.Kuwahara) {
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
    BufferedImage filteredImage = new BufferedImage(width, height, originalImage.getType());

    int radius = 4;

    var numThreads = Runtime.getRuntime().availableProcessors();
    var executor = Executors.newFixedThreadPool(numThreads);

    for (int y = radius; y < height - radius; y++) {
      for (int x = radius; x < width - radius; x++) {
        int[][] regions = new int[4][(radius + 1) * (radius + 1)];
        int[] regionSizes = new int[4];

        int[] sumR = new int[4];
        int[] sumG = new int[4];
        int[] sumB = new int[4];
        int[] sumA = new int[4];

        var regionCount = 0;
        for (var xFirst : Arrays.asList(-radius, 0)) {
          for (var yFirst : Arrays.asList(-radius, 0)) {
            for (int dy = xFirst; dy <= xFirst + radius; dy++) {
              for (int dx = yFirst; dx <= yFirst + radius; dx++) {
                int rgb = originalImage.getRGB(x + dx, y + dy);
                regions[regionCount][regionSizes[regionCount]++] = rgb;
                var color = new Color(rgb);
                sumA[regionCount] += color.getAlpha();
                sumR[regionCount] += color.getRed();
                sumG[regionCount] += color.getGreen();
                sumB[regionCount] += color.getBlue();
              }
            }
            regionCount++;
          }
        }

        int[] meanR = new int[4];
        int[] meanG = new int[4];
        int[] meanB = new int[4];
        int[] meanA = new int[4];
        double[] variances = new double[4];

        for (int i = 0; i < 4; i++) {
          meanA[i] = sumA[i] / regionSizes[i];
          meanR[i] = sumR[i] / regionSizes[i];
          meanG[i] = sumG[i] / regionSizes[i];
          meanB[i] = sumB[i] / regionSizes[i];

          double variance = 0;
          for (int j = 0; j < regionSizes[i]; j++) {
            int rgb = regions[i][j];
            var color = new Color(rgb);
            variance += Math.pow(color.getRed() - meanR[i], 2)
                        + Math.pow(color.getGreen() - meanG[i], 2)
                        + Math.pow(color.getBlue() - meanB[i], 2);
          }
          variances[i] = variance / regionSizes[i];
        }

        int bestRegion = 0;
        for (int i = 1; i < 4; i++) {
          if (variances[i] < variances[bestRegion]) {
            bestRegion = i;
          }
        }

        var resultColor = new Color(meanR[bestRegion], meanG[bestRegion], meanB[bestRegion], meanA[bestRegion]);
        filteredImage.setRGB(x, y, resultColor.getRGB());
      }
    }

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(filteredImage, contentType, outputStream);
    return outputStream.toByteArray();
  }
}
