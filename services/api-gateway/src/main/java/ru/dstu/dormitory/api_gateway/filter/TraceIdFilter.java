package ru.dstu.dormitory.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Гарантирует наличие X-Request-Id во входящем и downstream-запросе и в response.
 * Срабатывает раньше других фильтров.
 */
@Component
public class TraceIdFilter implements GlobalFilter, Ordered {

    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = exchange.getRequest().getHeaders().getFirst(HEADER_REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        final String finalRequestId = requestId;

        ServerWebExchange mutated = exchange.mutate()
                .request(builder -> builder.header(HEADER_REQUEST_ID, finalRequestId))
                .build();

        mutated.getResponse().getHeaders().set(HEADER_REQUEST_ID, finalRequestId);
        mutated.getAttributes().put(HEADER_REQUEST_ID, finalRequestId);

        return chain.filter(mutated);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
