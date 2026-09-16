package ru.dstu.dormitory.consumables_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.dstu.dormitory.consumables_service.aspect.ArgMasker;
import ru.dstu.dormitory.consumables_service.aspect.DefaultArgMasker;
import ru.dstu.dormitory.consumables_service.aspect.LoggingAspect;
import ru.dstu.dormitory.consumables_service.client.ResidentsFacade;
import ru.dstu.dormitory.consumables_service.client.ResidentsServiceClient;
import ru.dstu.dormitory.consumables_service.client.dto.ResidentDto;
import ru.dstu.dormitory.consumables_service.config.AppPropertiesConfig;
import ru.dstu.dormitory.consumables_service.domain.enums.ConsumableUnit;
import ru.dstu.dormitory.consumables_service.domain.model.ConsumableType;
import ru.dstu.dormitory.consumables_service.domain.repo.ConsumableIssueRepository;
import ru.dstu.dormitory.consumables_service.domain.repo.ConsumableTypeRepository;
import ru.dstu.dormitory.consumables_service.exception.OutOfStockException;
import ru.dstu.dormitory.consumables_service.service.event.EventPublisher;
import ru.dstu.dormitory.consumables_service.web.dto.request.IssueRequest;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Тест двух параллельных выдач последнего экземпляра.
 * Один поток получает успех, другой — {@link OutOfStockException}.
 * Pessimistic lock на ConsumableType гарантирует детерминированное поведение.
 */
@SpringBootTest(classes = ConcurrentIssuanceIT.TestContext.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ConcurrentIssuanceIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("dormitory")
            .withUsername("postgres")
            .withPassword("postgres")
            .withInitScript("db/init/create-schema.sql");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.yaml");
        registry.add("spring.liquibase.default-schema", () -> "consumables");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "consumables");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("jwt.secret", () -> "test-secret-with-at-least-32-characters-long-please");
        registry.add("security.service-token", () -> "test-token");
        registry.add("integration.residents.url", () -> "http://localhost:0");
        registry.add("kafka.topics.consumable-events", () -> "consumable.events");
        registry.add("kafka.topics.resident-events", () -> "resident.events");
        registry.add("kafka.topics.notification-events", () -> "notification.events");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = KafkaAutoConfiguration.class)
    @ComponentScan(basePackages = "ru.dstu.dormitory.consumables_service",
            excludeFilters = {
                    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                            classes = {
                                    ru.dstu.dormitory.consumables_service.service.event.OutboxRelay.class,
                                    ru.dstu.dormitory.consumables_service.service.consumer.ResidentEventsConsumer.class,
                                    ru.dstu.dormitory.consumables_service.config.SecurityConfig.class,
                                    ru.dstu.dormitory.consumables_service.config.CorsConfig.class,
                                    ru.dstu.dormitory.consumables_service.config.WebMvcConfig.class,
                                    ru.dstu.dormitory.consumables_service.config.RedisConfig.class,
                                    ru.dstu.dormitory.consumables_service.config.KafkaConfig.class
                            })
            })
    static class TestContext {
        @org.springframework.context.annotation.Bean
        ArgMasker argMasker() {
            return new DefaultArgMasker();
        }

        @org.springframework.context.annotation.Bean
        LoggingAspect loggingAspect(ArgMasker argMasker) {
            return new LoggingAspect(argMasker);
        }

        @org.springframework.context.annotation.Bean
        AppPropertiesConfig appPropertiesConfig() {
            return new AppPropertiesConfig();
        }

        @org.springframework.context.annotation.Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @org.springframework.context.annotation.Bean
        EventPublisher eventPublisher() {
            return (a, b, c, d) -> {
            };
        }

        @org.springframework.context.annotation.Bean
        ResidentsServiceClient residentsServiceClient() {
            return mock(ResidentsServiceClient.class);
        }

        @org.springframework.context.annotation.Bean
        ResidentsFacade residentsFacade() {
            ResidentsFacade facade = mock(ResidentsFacade.class);
            return facade;
        }
    }

    @Autowired
    private ConsumableTypeRepository typeRepository;
    @Autowired
    private ConsumableIssueRepository issueRepository;
    @Autowired
    private ConsumableIssueService issueService;
    @Autowired
    private ResidentsFacade residentsFacade;

    private UUID typeId;
    private UUID residentId;
    private UUID userId;
    private UUID actorId;

    @BeforeEach
    void prepare() {
        issueRepository.deleteAll();
        typeRepository.deleteAll();

        typeId = UUID.randomUUID();
        residentId = UUID.randomUUID();
        userId = UUID.randomUUID();
        actorId = UUID.randomUUID();

        ConsumableType type = ConsumableType.builder()
                .id(typeId).name("Подушка").unit(ConsumableUnit.PIECE)
                .stock(1).lowStockThreshold(0).build();
        typeRepository.saveAndFlush(type);

        when(residentsFacade.requireResident(any())).thenReturn(
                new ResidentDto(residentId, userId, "STUDENT", null, null, null,
                        null, null, null, null, null, null, null, null));
    }

    @Test
    @DisplayName("Два потока пытаются выдать последний экземпляр: один получает успех, другой 409")
    void concurrentIssue_lastUnit() throws InterruptedException {
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger outOfStockCount = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);

        Runnable task = () -> {
            try {
                start.await();
                issueService.issue(new IssueRequest(residentId, typeId, 1, null), actorId);
                successCount.incrementAndGet();
            } catch (OutOfStockException ex) {
                outOfStockCount.incrementAndGet();
            } catch (Exception ex) {
                // прочие — фиксируем как "не успех"
            } finally {
                done.countDown();
            }
        };
        pool.submit(task);
        pool.submit(task);

        start.countDown();
        boolean finished = done.await(15, TimeUnit.SECONDS);
        pool.shutdownNow();

        assertThat(finished).isTrue();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(outOfStockCount.get()).isEqualTo(1);

        ConsumableType reloaded = typeRepository.findById(typeId).orElseThrow();
        assertThat(reloaded.getStock()).isZero();
    }
}
