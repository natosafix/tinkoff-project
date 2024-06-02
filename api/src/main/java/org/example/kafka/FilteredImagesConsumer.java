package org.example.kafka;

import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.domain.Image;
import org.example.domain.Status;
import org.example.minio.MinioService;
import org.example.repositories.ImageFiltersRequestRepository;
import org.example.repositories.ImageRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilteredImagesConsumer {

  private final ImageFiltersRequestRepository repository;
  private final ImageRepository imageRepository;
  private final MinioService minioService;

  /**
   * Consume kafka messages.
   *
   * @param record record
   * @param acknowledgment acknowledgment
   */
  @KafkaListener(
          topics = "${application.kafka.topic-done}",
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
    var image = new Gson().fromJson(record.value(), KafkaDoneImage.class);
    log.info("Получено следующее сообщение из топика {}:\nkey: {},\nvalue: {}",
            record.topic(), record.key(), image);
    var request = repository.findById(image.getRequestId()).get();
    var meta = minioService.getImageMeta(image.getImageId().toString());
    var filteredImage = new Image()
            .setImageId(image.getImageId())
            .setSize(meta.size())
            .setUser(request.getSourceImage().getUser())
            .setFilename("filtered_image");
    request.setFilteredImage(filteredImage);
    request.setStatus(Status.DONE);
    repository.save(request);
    acknowledgment.acknowledge();
  }
}
