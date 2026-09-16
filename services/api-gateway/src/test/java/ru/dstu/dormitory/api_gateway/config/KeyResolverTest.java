package ru.dstu.dormitory.api_gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;
import ru.dstu.dormitory.api_gateway.filter.JwtAuthGlobalFilter;

class KeyResolverTest {

    private KeyResolver keyResolver;

    @BeforeEach
    void setUp() {
        SecurityProperties props = new SecurityProperties();
        keyResolver = new GatewayConfig(props).userKeyResolver();
    }

    @Test
    @DisplayName("Если есть X-User-Id — ключ user:<id>")
    void userIdPresent_returnsUserKey() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/requests")
                .header(JwtAuthGlobalFilter.HEADER_USER_ID, "abc-123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(keyResolver.resolve(exchange))
                .expectNext("user:abc-123")
                .verifyComplete();
    }

    @Test
    @DisplayName("Если X-User-Id нет — fallback на IP")
    void userIdMissing_fallsBackToIp() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/auth/login")
                .header("X-Forwarded-For", "203.0.113.5, 10.0.0.1")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(keyResolver.resolve(exchange))
                .expectNext("ip:203.0.113.5")
                .verifyComplete();
    }
}
