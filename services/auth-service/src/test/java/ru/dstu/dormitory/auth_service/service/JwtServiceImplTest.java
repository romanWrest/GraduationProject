package ru.dstu.dormitory.auth_service.service;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.dstu.dormitory.auth_service.config.JwtProperties;
import ru.dstu.dormitory.auth_service.domain.model.RoleCode;
import ru.dstu.dormitory.auth_service.service.Impl.JwtServiceImpl;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceImplTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("0123456789-0123456789-0123456789-secret");
        props.setAccessTtl(Duration.ofMinutes(15));
        props.setRefreshTtl(Duration.ofDays(7));
        props.setIssuer("auth-service-test");
        jwtService = new JwtServiceImpl(props);
    }

    @Test
    void generate_and_parse_round_trip() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateAccessToken(userId, "user@example.com", Set.of(RoleCode.ADMIN));

        JwtService.ParsedAccessToken parsed = jwtService.parseAndValidate(token);

        assertThat(parsed.userId()).isEqualTo(userId);
        assertThat(parsed.email()).isEqualTo("user@example.com");
        assertThat(parsed.roles()).containsExactly(RoleCode.ADMIN);
        assertThat(parsed.jti()).isNotBlank();
    }

    @Test
    void parse_rejects_tampered_token() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateAccessToken(userId, "u@e.com", Set.of(RoleCode.RESIDENT));
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> jwtService.parseAndValidate(tampered))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void parse_rejects_expired_token() {
        JwtProperties props = new JwtProperties();
        props.setSecret("0123456789-0123456789-0123456789-secret");
        props.setAccessTtl(Duration.ofMillis(1));
        props.setRefreshTtl(Duration.ofDays(1));
        props.setIssuer("auth-service-test");
        JwtService shortLived = new JwtServiceImpl(props);

        String token = shortLived.generateAccessToken(UUID.randomUUID(), "u@e.com", Set.of(RoleCode.RESIDENT));
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThatThrownBy(() -> shortLived.parseAndValidate(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void accessTtlSeconds_reflects_configuration() {
        assertThat(jwtService.accessTtlSeconds()).isEqualTo(900L);
    }
}
