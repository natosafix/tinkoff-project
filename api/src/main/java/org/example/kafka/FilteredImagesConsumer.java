package org.example.kafka;

import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.domain.Status;
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
  public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
    var image = new Gson().fromJson(record.value(), KafkaDoneImage.class);
    log.info("Получено следующее сообщение из топика {}:\nkey: {},\nvalue: {}",
            record.topic(), record.key(), image);
    var request = repository.findById(image.getRequestId()).get();
    request.setFilteredImage(imageRepository.findById(UUID.fromString(image.getImageId())).get());
    request.setStatus(Status.DONE);
    repository.save(request);
    acknowledgment.acknowledge();
  }
}
