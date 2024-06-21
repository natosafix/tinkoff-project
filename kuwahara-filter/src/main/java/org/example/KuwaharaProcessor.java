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

@Service
@RequiredArgsConstructor
public class KuwaharaProcessor {
  private final KafkaProducer kafkaProducer;
  private final ProcessedImageRepository repository;
  private final MinioService minioService;

  public static final int RADIUS = 4;

  public void process(KafkaImageFiltersRequest request) throws Exception {
    var oldProcessedImage = new ProcessedImageId(request.getRequestId(), request.getImageId());
    if (request.getFilters()[0] != Filter.Kuwahara || repository.existsById(oldProcessedImage)) {
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

  private byte[] processImage(byte[] image, String contentType) throws IOException {
    var inputStream = new ByteArrayInputStream(image);
    var originalImage = ImageIO.read(inputStream);
    var width = originalImage.getWidth();
    var height = originalImage.getHeight();
    BufferedImage filteredImage = new BufferedImage(width, height, originalImage.getType());

    for (int y = RADIUS; y < height - RADIUS; y++) {
      for (int x = RADIUS; x < width - RADIUS; x++) {
        var resultColor = calculateResultColor(x, y, originalImage);
        filteredImage.setRGB(x, y, resultColor.getRGB());
      }
    }

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(filteredImage, contentType, outputStream);
    return outputStream.toByteArray();
  }

  private Color calculateResultColor(int x, int y, BufferedImage originalImage) {
    int[][] regions = new int[4][(RADIUS + 1) * (RADIUS + 1)];
    int[] regionSizes = new int[4];

    int[] sumR = new int[4];
    int[] sumG = new int[4];
    int[] sumB = new int[4];
    int[] sumA = new int[4];

    int[] meanR = new int[4];
    int[] meanG = new int[4];
    int[] meanB = new int[4];
    int[] meanA = new int[4];
    double[] variances = new double[4];

    calculateRegions(sumA, sumR, sumG, sumB, x, y, originalImage, regions, regionSizes);

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

    return new Color(meanR[bestRegion], meanG[bestRegion], meanB[bestRegion], meanA[bestRegion]);
  }

  private void calculateRegions(int[] sumA, int[] sumR, int[] sumG, int[] sumB, int x, int y,
                                BufferedImage originalImage, int[][] regions, int[] regionSizes) {
    var regionCount = 0;
    for (var xFirst : Arrays.asList(-RADIUS, 0)) {
      for (var yFirst : Arrays.asList(-RADIUS, 0)) {
        for (int dy = xFirst; dy <= xFirst + RADIUS; dy++) {
          for (int dx = yFirst; dx <= yFirst + RADIUS; dx++) {
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
  }
}
