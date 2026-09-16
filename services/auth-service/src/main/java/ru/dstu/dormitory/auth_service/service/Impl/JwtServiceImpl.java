package ru.dstu.dormitory.auth_service.service.Impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.dstu.dormitory.auth_service.config.JwtProperties;
import ru.dstu.dormitory.auth_service.domain.model.RoleCode;
import ru.dstu.dormitory.auth_service.service.JwtService;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtServiceImpl(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateAccessToken(UUID userId, String email, Set<RoleCode> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.getAccessTtl());
        List<String> roleCodes = roles.stream().map(Enum::name).collect(Collectors.toList());

        return Jwts.builder()
                .subject(userId.toString())
                .issuer(properties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .id(UUID.randomUUID().toString())
                .claim("email", email)
                .claim("roles", roleCodes)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    @Override
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

    @Override
    public long accessTtlSeconds() {
        return properties.getAccessTtl().toSeconds();
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
}
