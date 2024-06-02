package org.example.kafka;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class FilterApplier {

    private final KafkaProducer kafkaProducer;

    @KafkaListener(
            topics = "${application.kafka.topic-wip}",
            groupId = "${application.kafka.group-id}",
            concurrency = "${application.kafka.partitions}",
            properties = {
                    ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG + "=false",
                    ConsumerConfig.ISOLATION_LEVEL_CONFIG + "=read_committed",
                    ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG +
                            "=org.apache.kafka.clients.consumer.RoundRobinAssignor"
            }
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        var request = new Gson().fromJson(record.value(), KafkaImageFiltersRequest.class);
        log.info("""
                Получено следующее сообщение из топика {}:
                key: {},
                value: {}
                """, record.topic(), record.key(), request);
        kafkaProducer.sendDone(new Gson().toJson(new KafkaDoneImage(request.getImageId(), request.getRequestId())));
        acknowledgment.acknowledge();
    }
}
