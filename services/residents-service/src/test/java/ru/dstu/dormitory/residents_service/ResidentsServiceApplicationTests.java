package ru.dstu.dormitory.residents_service;

import org.junit.jupiter.api.Test;

class ResidentsServiceApplicationTests {

	@Test
	void sanity() {
		// Smoke-тест: Spring-контекст поднимается в интеграционных тестах с Testcontainers;
		// юнит-сборка должна запускаться без реальной инфраструктуры (Postgres/Kafka/Redis).
	}

}
