package ru.dstu.dormitory.consumables_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic consumableEventsTopic(KafkaTopicsProperties props) {
        return TopicBuilder.name(props.getConsumableEvents())
                .partitions(3)
                .replicas(1)
                .build();
    }
}
