package com.ecommerce.platform.gateway.filter;

import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GatewayRequestContextFilter implements WebFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9._:-]{1,128}");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestId = safeId(exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER))
            .orElseGet(() -> UUID.randomUUID().toString());
        String correlationId = safeId(exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER))
            .orElse(requestId);

        ServerHttpRequest request = exchange.getRequest().mutate()
            .headers(headers -> {
                headers.set(REQUEST_ID_HEADER, requestId);
                headers.set(CORRELATION_ID_HEADER, correlationId);
            })
            .build();
        exchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, requestId);
        exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, correlationId);
        return chain.filter(exchange.mutate().request(request).build());
    }

    private java.util.Optional<String> safeId(String candidate) {
        if (candidate == null || !SAFE_ID.matcher(candidate).matches()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(candidate);
    }
}
