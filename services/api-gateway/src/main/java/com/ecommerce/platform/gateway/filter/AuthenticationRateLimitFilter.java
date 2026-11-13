package com.ecommerce.platform.gateway.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationRateLimitFilter implements GlobalFilter, Ordered {

    private final Cache<String, AtomicInteger> requestCounts = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(2))
        .maximumSize(100_000)
        .build();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        RateLimitRule rule = ruleFor(exchange.getRequest());
        if (rule == null) {
            return chain.filter(exchange);
        }

        long currentMinute = Instant.now().getEpochSecond() / 60;
        String key = clientAddress(exchange) + "|" + rule.path() + "|" + currentMinute;
        int count = requestCounts.asMap()
            .computeIfAbsent(key, ignored -> new AtomicInteger())
            .incrementAndGet();
        if (count <= rule.limit()) {
            return chain.filter(exchange);
        }

        long secondsRemaining = 60 - (Instant.now().getEpochSecond() % 60);
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().set("Retry-After", Long.toString(secondsRemaining));
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    private RateLimitRule ruleFor(ServerHttpRequest request) {
        if (!HttpMethod.POST.equals(request.getMethod())) {
            return null;
        }
        return switch (request.getURI().getPath()) {
            case "/api/v1/auth/login" -> new RateLimitRule("/api/v1/auth/login", 10);
            case "/api/v1/auth/register" -> new RateLimitRule("/api/v1/auth/register", 5);
            case "/api/v1/auth/refresh" -> new RateLimitRule("/api/v1/auth/refresh", 30);
            default -> null;
        };
    }

    private String clientAddress(ServerWebExchange exchange) {
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress == null) {
            return "unknown";
        }
        return remoteAddress.getAddress() == null
            ? remoteAddress.getHostString()
            : remoteAddress.getAddress().getHostAddress();
    }

    private record RateLimitRule(String path, int limit) {
    }
}
