package org.example.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static org.example.config.KafkaConfig.WIP_KAFKA_TEMPLATE;
import static org.example.config.KafkaConfig.DONE_KAFKA_TEMPLATE;


@Slf4j
@Service
public class KafkaProducer {

    @Value("${application.kafka.topic-wip}")
    private String topicWip;

    @Value("${application.kafka.topic-done}")
    private String topicDone;

    private final KafkaTemplate<String, KafkaImageFiltersRequest> wipTemplate;
    private final KafkaTemplate<String, KafkaDoneImage> doneTemplate;

    public KafkaProducer(
            @Qualifier(WIP_KAFKA_TEMPLATE)
            KafkaTemplate<String, KafkaImageFiltersRequest> wipTemplate,

            @Qualifier(DONE_KAFKA_TEMPLATE)
            KafkaTemplate<String, KafkaDoneImage> doneTemplate
    ) {
        this.wipTemplate = wipTemplate;
        this.doneTemplate = doneTemplate;
    }

    public void send(KafkaImageFiltersRequest message) {
        log.info("Отправляем сообщение {}", message);
        wipTemplate.send(topicWip, message);
    }

    public void send(KafkaDoneImage message) {
        log.info("Отправляем сообщение {}", message);
        doneTemplate.send(topicDone, message);
    }
}
