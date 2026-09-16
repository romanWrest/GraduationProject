package ru.dstu.dormitory.notifications_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import ru.dstu.dormitory.notifications_service.domain.repo.EmailLogRepository;
import ru.dstu.dormitory.notifications_service.domain.repo.NotificationRepository;
import ru.dstu.dormitory.notifications_service.domain.repo.ProcessedEventRepository;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class EndToEndWelcomeIT extends AbstractIT {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    EmailLogRepository emailLogRepository;

    @Autowired
    ProcessedEventRepository processedEventRepository;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void cleanState() throws Exception {
        emailLogRepository.deleteAll();
        notificationRepository.deleteAll();
        processedEventRepository.deleteAll();
        WIRE_MOCK.resetAll();
        GREEN_MAIL.purgeEmailFromAllMailboxes();
    }

    @Test
    void publishUserCreated_thenInAppCreatedAndEmailSent() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        // WireMock: auth-service /internal/users/{id}
        WIRE_MOCK.stubFor(get(urlEqualTo("/api/v1/internal/users/" + userId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(Map.of(
                                "id", userId.toString(),
                                "email", "ivan@example.com",
                                "fullName", "Иван Иванов",
                                "active", true,
                                "roles", java.util.List.of("RESIDENT"),
                                "createdAt", Instant.now().toString(),
                                "updatedAt", Instant.now().toString())))));

        String envelope = """
                {
                  "eventId": "%s",
                  "eventType": "UserCreated",
                  "occurredAt": "%s",
                  "payload": {
                    "userId": "%s",
                    "email": "ivan@example.com",
                    "fullName": "Иван Иванов",
                    "temporaryPassword": "topsecret-pwd"
                  }
                }
                """.formatted(eventId, Instant.now(), userId);

        kafkaTemplate.send("user.events", userId.toString(), envelope).get(5, TimeUnit.SECONDS);

        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(notificationRepository.count()).isEqualTo(1);
            assertThat(emailLogRepository.count()).isEqualTo(1);
            MimeMessage[] received = GREEN_MAIL.getReceivedMessages();
            assertThat(received).hasSize(1);
            assertThat(received[0].getSubject()).contains("Добро пожаловать");
            String body = (String) received[0].getContent();
            assertThat(body).contains("topsecret-pwd");
            assertThat(body).contains("Иван Иванов");
        });
    }

    @Test
    void duplicateEvent_processedOnce() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        WIRE_MOCK.stubFor(get(urlEqualTo("/api/v1/internal/users/" + userId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(Map.of(
                                "id", userId.toString(),
                                "email", "ivan@example.com",
                                "fullName", "Иван",
                                "active", true,
                                "roles", java.util.List.of("RESIDENT"),
                                "createdAt", Instant.now().toString(),
                                "updatedAt", Instant.now().toString())))));

        String envelope = """
                {
                  "eventId": "%s",
                  "eventType": "UserCreated",
                  "occurredAt": "%s",
                  "payload": {
                    "userId": "%s",
                    "email": "ivan@example.com",
                    "fullName": "Иван",
                    "temporaryPassword": "p1"
                  }
                }
                """.formatted(eventId, Instant.now(), userId);

        kafkaTemplate.send("user.events", userId.toString(), envelope).get(5, TimeUnit.SECONDS);
        kafkaTemplate.send("user.events", userId.toString(), envelope).get(5, TimeUnit.SECONDS);
        kafkaTemplate.send("user.events", userId.toString(), envelope).get(5, TimeUnit.SECONDS);

        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(notificationRepository.count()).isEqualTo(1);
            assertThat(emailLogRepository.count()).isEqualTo(1);
            assertThat(GREEN_MAIL.getReceivedMessages()).hasSize(1);
            assertThat(processedEventRepository.count()).isEqualTo(1);
        });
    }
}
