package org.example.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.repositories.ImageFiltersRequestRepository;
import org.example.repositories.ImageRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilteredImagesConsumer {

    private final ImageFiltersRequestRepository repository;
    private final ImageRepository imageRepository;

    @KafkaListener(
            topics = "${application.kafka.topic-done}",
            groupId = "${application.kafka.group-id}",
            concurrency = "${application.kafka.partitions}",
            properties = {
                    ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG + "=false",
                    //                    ConsumerConfig.ISOLATION_LEVEL_CONFIG + "=read_uncommitted",
                    ConsumerConfig.ISOLATION_LEVEL_CONFIG + "=read_committed",
                    ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG +
                            "=org.apache.kafka.clients.consumer.RoundRobinAssignor"
            }
    )
    public void consume(ConsumerRecord<String, KafkaDoneImage> record, Acknowledgment acknowledgment) {
        var image = record.value();
        log.info("""
                 Получено следующее сообщение из топика {}:
                 key: {},
                 value: {}
                 """, record.topic(), record.key(), image);
        acknowledgment.acknowledge();
        var request = repository.findById(image.getRequestId()).get();
        request.setFilteredImage(imageRepository.findById(image.getImageId()).get());
        repository.save(request);
    }
}
