package ru.dstu.dormitory.notifications_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import ru.dstu.dormitory.notifications_service.config.JwtProperties;
import ru.dstu.dormitory.notifications_service.domain.enums.RoleCode;

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
public class JwtTokenParser {

    private final SecretKey signingKey;

    public JwtTokenParser(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public ParsedAccessToken parseAndValidate(String token) {
        Jws<Claims> parsed = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token);

        Claims claims = parsed.getPayload();
        UUID userId = UUID.fromString(claims.getSubject());
        String email = claims.get("email", String.class);
        Set<RoleCode> roles = extractRoles(claims);
        String jti = claims.getId();
        return new ParsedAccessToken(userId, email, roles, jti);
    }

    @SuppressWarnings("unchecked")
    private Set<RoleCode> extractRoles(Claims claims) {
        Object raw = claims.get("roles");
        if (raw instanceof List<?> list) {
            return ((List<String>) list).stream()
                    .map(RoleCode::valueOf)
                    .collect(Collectors.toUnmodifiableSet());
        }
        return Set.of();
    }

    public record ParsedAccessToken(UUID userId, String email, Set<RoleCode> roles, String jti) {
    }
}
