package ru.dstu.dormitory.notifications_service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Testcontainers
@SpringBootTest(
        classes = NotificationsServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.profiles.active=test",
                "spring.kafka.consumer.auto-offset-reset=earliest",
                "spring.autoconfigure.exclude="
                        + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration",
                "logging.level.root=WARN",
                "logging.level.ru.dstu.dormitory.notifications_service=INFO"
        })
public abstract class AbstractIT {

    protected static final PostgreSQLContainer<?> POSTGRES;
    protected static final KafkaContainer KAFKA;
    protected static WireMockServer WIRE_MOCK;

    @RegisterExtension
    protected static final GreenMailExtension GREEN_MAIL = new GreenMailExtension(
            new ServerSetup(0, null, "smtp"));

    static {
        POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                .withDatabaseName("dormitory")
                .withUsername("postgres")
                .withPassword("postgres")
                .withInitScript("init/init-schemas.sql");
        POSTGRES.start();

        KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));
        KAFKA.start();
    }

    @BeforeAll
    static void startWireMock() {
        if (WIRE_MOCK == null) {
            WIRE_MOCK = new WireMockServer(options().dynamicPort());
            WIRE_MOCK.start();
        }
    }

    @AfterAll
    static void stopWireMock() {
        if (WIRE_MOCK != null) {
            WIRE_MOCK.stop();
            WIRE_MOCK = null;
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);

        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> GREEN_MAIL.getSmtp().getPort());
        registry.add("spring.mail.properties.mail.smtp.auth", () -> "false");
        registry.add("spring.mail.properties.mail.smtp.starttls.enable", () -> "false");

        registry.add("integration.auth.url",
                () -> "http://localhost:" + (WIRE_MOCK != null ? WIRE_MOCK.port() : 0));
        registry.add("jwt.secret", () -> "test-jwt-secret-32-characters-or-more-please-here");
        registry.add("security.service-token", () -> "service-token-for-tests");
        registry.add("app.frontend.base-url", () -> "http://localhost:3000");

        // circuit breaker не нужен в тестах — реальный wiremock-эндпоинт всегда доступен
        registry.add("spring.cloud.openfeign.circuitbreaker.enabled", () -> "false");
    }
}
