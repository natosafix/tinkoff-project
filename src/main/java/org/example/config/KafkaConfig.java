package org.example.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;


@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    public static final String DONE_KAFKA_TEMPLATE = "doneKafkaTemplate";
    public static final String WIP_KAFKA_TEMPLATE = "wipKafkaTemplate";

    private final KafkaProperties properties;

    @Bean
    public KafkaAdmin.NewTopics topic(
            @Value("${application.kafka.topic-wip}") String topicWip,
            @Value("${application.kafka.topic-done}") String topicDone,
            @Value("${application.kafka.partitions}") int partitions,
            @Value("${application.kafka.replicas}") short replicas) {

        return new KafkaAdmin.NewTopics(new NewTopic(topicWip, partitions, replicas), new NewTopic(topicDone, partitions, replicas));
    }

    @Bean(WIP_KAFKA_TEMPLATE)
    public KafkaTemplate<String, String> wipKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean(DONE_KAFKA_TEMPLATE)
    public KafkaTemplate<String, String> doneKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    private <V> ProducerFactory<String, V> producerFactory() {
        var props = properties.buildProducerProperties(null);

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, RoundRobinPartitioner.class);

        props.put(ProducerConfig.LINGER_MS_CONFIG, 0);
        props.put(ProducerConfig.RETRIES_CONFIG, 3);

        props.put(ProducerConfig.ACKS_CONFIG, "all");

        return new DefaultKafkaProducerFactory<>(props);
    }

}
