package ru.dstu.dormitory.api_gateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import ru.dstu.dormitory.api_gateway.config.JwtProperties;
import ru.dstu.dormitory.api_gateway.exception.JwtExpiredException;
import ru.dstu.dormitory.api_gateway.exception.JwtInvalidException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Валидатор и парсер access-токенов, выданных auth-service.
 */
@Component
public class JwtValidator {

    private final SecretKey signingKey;

    public JwtValidator(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Проверяет подпись токена и его срок действия.
     *
     * @throws JwtExpiredException если токен просрочен
     * @throws JwtInvalidException если токен некорректен (подпись, формат, claims)
     */
    public ParsedAccessToken parseAndValidate(String token) {
        try {
            Jws<Claims> parsed = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);

            Claims claims = parsed.getPayload();
            UUID userId = UUID.fromString(claims.getSubject());
            String email = claims.get("email", String.class);
            Set<String> roles = extractRoles(claims);
            String jti = claims.getId();
            return new ParsedAccessToken(userId, email, roles, jti);
        } catch (ExpiredJwtException e) {
            throw new JwtExpiredException("Срок действия токена истёк", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtInvalidException("Невалидный JWT: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractRoles(Claims claims) {
        Object raw = claims.get("roles");
        if (raw instanceof List<?> list) {
            return ((List<String>) list).stream().collect(Collectors.toUnmodifiableSet());
        }
        return Set.of();
    }

    public record ParsedAccessToken(UUID userId, String email, Set<String> roles, String jti) {
    }
}
