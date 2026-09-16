package ru.dstu.dormitory.reports_service.service.consumer;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResidentEventsReportConsumer {

    private final KafkaEventDispatcher dispatcher;

    @KafkaListener(topics = "${kafka.topics.resident-events:resident.events}",
            groupId = "${spring.kafka.consumer.group-id:reports-service-group}",
            containerFactory = "kafkaListenerContainerFactory")
    public void onResidentEvent(ConsumerRecord<String, String> record, Acknowledgment ack) {
        dispatcher.dispatch(record.topic(), record.value());
        ack.acknowledge();
    }
}
