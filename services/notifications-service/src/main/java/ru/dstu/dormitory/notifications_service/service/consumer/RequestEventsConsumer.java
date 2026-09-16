package ru.dstu.dormitory.notifications_service.service.consumer;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestEventsConsumer {

    private final KafkaEventDispatcher dispatcher;

    @KafkaListener(topics = "${kafka.topics.request-events:request.events}",
            groupId = "${spring.kafka.consumer.group-id:notifications-service-group}",
            containerFactory = "kafkaListenerContainerFactory")
    public void onRequestEvent(ConsumerRecord<String, String> record, Acknowledgment ack) {
        dispatcher.dispatch(record.topic(), record.value());
        ack.acknowledge();
    }
}
