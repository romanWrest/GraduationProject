package ru.dstu.dormitory.api_gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.dstu.dormitory.api_gateway.config.JwtProperties;
import ru.dstu.dormitory.api_gateway.exception.JwtMissingException;
import ru.dstu.dormitory.api_gateway.service.JwtValidator;
import ru.dstu.dormitory.api_gateway.service.JwtValidator.ParsedAccessToken;

import java.util.List;

import static ru.dstu.dormitory.api_gateway.util.LogPatterns.JWT_MISSING;
import static ru.dstu.dormitory.api_gateway.util.LogPatterns.JWT_VALIDATED;

/**
 * Глобальный фильтр валидации JWT.
 * <p>
 * Для путей из whitelist пропускает запрос без проверки.
 * Иначе извлекает Authorization: Bearer, валидирует и кладёт в downstream-запрос
 * заголовки X-User-Id, X-User-Email, X-User-Roles.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_EMAIL = "X-User-Email";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String BEARER_PREFIX = "Bearer ";

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtValidator jwtValidator;
    private final JwtProperties jwtProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(stripUserHeaders(exchange));
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn(JWT_MISSING, path);
            throw new JwtMissingException("Отсутствует заголовок Authorization");
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        ParsedAccessToken parsed = jwtValidator.parseAndValidate(token);

        log.debug(JWT_VALIDATED, parsed.userId(), parsed.roles());

        ServerWebExchange mutated = exchange.mutate()
                .request(builder -> builder
                        .header(HEADER_USER_ID, parsed.userId().toString())
                        .header(HEADER_USER_EMAIL, parsed.email() == null ? "" : parsed.email())
                        .header(HEADER_USER_ROLES, String.join(",", parsed.roles())))
                .build();

        return chain.filter(mutated);
    }

    /**
     * Удаляет потенциально подделанные user-заголовки из публичных запросов,
     * чтобы downstream-сервис не доверял неподтверждённым данным.
     */
    private ServerWebExchange stripUserHeaders(ServerWebExchange exchange) {
        return exchange.mutate()
                .request(builder -> builder
                        .headers(headers -> {
                            headers.remove(HEADER_USER_ID);
                            headers.remove(HEADER_USER_EMAIL);
                            headers.remove(HEADER_USER_ROLES);
                        }))
                .build();
    }

    private boolean isPublicPath(String path) {
        List<String> patterns = jwtProperties.getPublicPaths();
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        for (String pattern : patterns) {
            if (PATH_MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}
