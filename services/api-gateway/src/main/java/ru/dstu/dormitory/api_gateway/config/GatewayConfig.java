package ru.dstu.dormitory.api_gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;
import ru.dstu.dormitory.api_gateway.filter.JwtAuthGlobalFilter;

import java.net.InetSocketAddress;

/**
 * Конфигурация CORS и резолвера ключей для rate limiter.
 */
@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final SecurityProperties securityProperties;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        SecurityProperties.Cors cors = securityProperties.getCors();

        config.setAllowedOrigins(cors.getAllowedOrigins());
        config.setAllowedMethods(cors.getAllowedMethods());
        config.setAllowedHeaders(cors.getAllowedHeaders());
        config.setExposedHeaders(cors.getExposedHeaders());
        config.setAllowCredentials(cors.isAllowCredentials());
        config.setMaxAge(cors.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }

    /**
     * Ключ для rate limiter: userId (если JWT прошёл фильтр) или IP клиента.
     */
    @Bean("userKeyResolver")
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst(JwtAuthGlobalFilter.HEADER_USER_ID);
            if (userId != null && !userId.isBlank()) {
                return Mono.just("user:" + userId);
            }
            String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return Mono.just("ip:" + forwarded.split(",")[0].trim());
            }
            InetSocketAddress address = exchange.getRequest().getRemoteAddress();
            String ip = address == null ? "unknown" : address.getAddress().getHostAddress();
            return Mono.just("ip:" + ip);
        };
    }
}
