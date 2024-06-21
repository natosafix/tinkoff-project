package org.example;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.example.domain.Filter;
import org.example.kafka.KafkaDoneImage;
import org.example.kafka.KafkaImageFiltersRequest;
import org.example.kafka.KafkaProducer;
import org.example.kafka.ProcessedImageId;
import org.example.minio.MinioService;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class InverseColorProcessor {
  private final KafkaProducer kafkaProducer;
  private final ProcessedImageRepository repository;
  private final MinioService minioService;

  public void process(KafkaImageFiltersRequest request) throws Exception {
    var oldProcessedImage = new ProcessedImageId(request.getRequestId(), request.getImageId());
    if (request.getFilters()[0] != Filter.InverseColor || repository.existsById(oldProcessedImage)) {
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
  }

  private byte[] processImage(byte[] image, String contentType) throws IOException, InterruptedException {
    var inputStream = new ByteArrayInputStream(image);
    var originalImage = ImageIO.read(inputStream);
    var width = originalImage.getWidth();
    var height = originalImage.getHeight();
    var invertedImage = new BufferedImage(width, height, originalImage.getType());

    var numThreads = Runtime.getRuntime().availableProcessors();
    var executor = Executors.newFixedThreadPool(numThreads);

    for (var y = 0; y < height; y++) {
      var finalY = y;
      executor.execute(() -> {
        for (var x = 0; x < width; x++) {
          var color = new Color(originalImage.getRGB(x, finalY));
          var newColor = new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue());
          invertedImage.setRGB(x, finalY, newColor.getRGB());
        }
      });
    }

    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.HOURS);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(invertedImage, contentType, outputStream);
    return outputStream.toByteArray();
  }
}
