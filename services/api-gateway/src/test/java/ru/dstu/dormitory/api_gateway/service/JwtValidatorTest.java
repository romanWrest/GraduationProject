package ru.dstu.dormitory.api_gateway.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.dstu.dormitory.api_gateway.config.JwtProperties;
import ru.dstu.dormitory.api_gateway.exception.JwtExpiredException;
import ru.dstu.dormitory.api_gateway.exception.JwtInvalidException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtValidatorTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-test-secret";

    private JwtValidator validator;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        validator = new JwtValidator(props);
        signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Валидный токен парсится в ParsedAccessToken с userId, email и ролями")
    void validToken_parses() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60)))
                .claim("email", "user@example.com")
                .claim("roles", List.of("ADMIN", "RESIDENT"))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        JwtValidator.ParsedAccessToken parsed = validator.parseAndValidate(token);

        assertThat(parsed.userId()).isEqualTo(userId);
        assertThat(parsed.email()).isEqualTo("user@example.com");
        assertThat(parsed.roles()).containsExactlyInAnyOrder("ADMIN", "RESIDENT");
    }

    @Test
    @DisplayName("Просроченный токен → JwtExpiredException")
    void expiredToken_throwsExpired() {
        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuedAt(Date.from(now.minusSeconds(120)))
                .expiration(Date.from(now.minusSeconds(60)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        assertThatThrownBy(() -> validator.parseAndValidate(token))
                .isInstanceOf(JwtExpiredException.class);
    }

    @Test
    @DisplayName("Токен с чужой подписью → JwtInvalidException")
    void wrongSignature_throwsInvalid() {
        SecretKey otherKey = Keys.hmacShaKeyFor("another-secret-with-min-32-char-length-yyyy".getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60)))
                .signWith(otherKey, Jwts.SIG.HS256)
                .compact();

        assertThatThrownBy(() -> validator.parseAndValidate(token))
                .isInstanceOf(JwtInvalidException.class);
    }

    @Test
    @DisplayName("Мусорный токен → JwtInvalidException")
    void garbageToken_throwsInvalid() {
        assertThatThrownBy(() -> validator.parseAndValidate("not-a-jwt"))
                .isInstanceOf(JwtInvalidException.class);
    }
}
