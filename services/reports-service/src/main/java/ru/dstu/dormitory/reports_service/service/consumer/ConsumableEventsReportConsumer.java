package ru.dstu.dormitory.reports_service.service.consumer;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConsumableEventsReportConsumer {

    private final KafkaEventDispatcher dispatcher;

    @KafkaListener(topics = "${kafka.topics.consumable-events:consumable.events}",
            groupId = "${spring.kafka.consumer.group-id:reports-service-group}",
            containerFactory = "kafkaListenerContainerFactory")
    public void onConsumableEvent(ConsumerRecord<String, String> record, Acknowledgment ack) {
        dispatcher.dispatch(record.topic(), record.value());
        ack.acknowledge();
    }
}
