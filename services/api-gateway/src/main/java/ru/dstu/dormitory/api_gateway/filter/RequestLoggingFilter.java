package ru.dstu.dormitory.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

import static ru.dstu.dormitory.api_gateway.util.LogPatterns.REQUEST_INCOMING;
import static ru.dstu.dormitory.api_gateway.util.LogPatterns.REQUEST_RESPONSE;

/**
 * Структурное логирование входящего запроса и ответа с длительностью.
 * Срабатывает после {@link JwtAuthGlobalFilter}, чтобы userId был уже в заголовке.
 */
@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long start = System.currentTimeMillis();

        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String userId = request.getHeaders().getFirst(JwtAuthGlobalFilter.HEADER_USER_ID);
        String ip = remoteIp(request);

        log.info(REQUEST_INCOMING, method, path, userId == null ? "-" : userId, ip);

        return chain.filter(exchange).doFinally(signal -> {
            long elapsed = System.currentTimeMillis() - start;
            HttpStatusCode status = exchange.getResponse().getStatusCode();
            String statusValue = status == null ? "-" : String.valueOf(status.value());
            log.info(REQUEST_RESPONSE, method, path, statusValue, elapsed);
        });
    }

    private String remoteIp(ServerHttpRequest request) {
        String forwarded = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        InetSocketAddress address = request.getRemoteAddress();
        return address == null ? "-" : address.getAddress().getHostAddress();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 200;
    }
}
