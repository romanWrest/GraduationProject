package ru.dstu.dormitory.appliances_service;

import org.junit.jupiter.api.Test;

class AppliancesServiceApplicationTests {

    @Test
    void sanity() {
        // Smoke-тест: Spring-контекст поднимается в интеграционных тестах с Testcontainers;
        // юнит-сборка должна запускаться без реальной инфраструктуры (Postgres/Kafka/Redis).
    }
}
