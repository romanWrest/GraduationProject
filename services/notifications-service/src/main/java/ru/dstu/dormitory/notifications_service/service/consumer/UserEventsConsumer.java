package ru.dstu.dormitory.notifications_service.service.consumer;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventsConsumer {

    private final KafkaEventDispatcher dispatcher;

    @KafkaListener(topics = "${kafka.topics.user-events:user.events}",
            groupId = "${spring.kafka.consumer.group-id:notifications-service-group}",
            containerFactory = "kafkaListenerContainerFactory")
    public void onUserEvent(ConsumerRecord<String, String> record, Acknowledgment ack) {
        dispatcher.dispatch(record.topic(), record.value());
        ack.acknowledge();
    }
}
