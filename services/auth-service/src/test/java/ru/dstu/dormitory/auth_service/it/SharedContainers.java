package ru.dstu.dormitory.auth_service.it;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * Singleton-контейнеры для интеграционных тестов.
 * Запускаются один раз на JVM и переиспользуются всеми тестами.
 */
public final class SharedContainers {

    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:17-alpine"))
            .withDatabaseName("dormitory")
            .withUsername("postgres")
            .withPassword("postgres")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("init-db/01-create-schemas.sql"),
                    "/docker-entrypoint-initdb.d/01-create-schemas.sql")
            .withReuse(true);

    @SuppressWarnings("resource")
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withReuse(true);

    @SuppressWarnings("resource")
    static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1"))
            .withReuse(true);

    private SharedContainers() {
    }

    static synchronized void startIfNeeded() {
        if (!POSTGRES.isRunning()) POSTGRES.start();
        if (!REDIS.isRunning()) REDIS.start();
        if (!KAFKA.isRunning()) KAFKA.start();
    }

    public static void register(DynamicPropertyRegistry registry) {
        startIfNeeded();
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }
}
