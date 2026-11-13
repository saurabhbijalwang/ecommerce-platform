package com.ecommerce.platform.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class GatewayRequestContextFilterTest {

    private final GatewayRequestContextFilter filter = new GatewayRequestContextFilter();

    @Test
    void replacesInvalidRequestIdAndPropagatesSafeCorrelationId() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/products")
                .header(GatewayRequestContextFilter.REQUEST_ID_HEADER, "invalid id")
                .header(GatewayRequestContextFilter.CORRELATION_ID_HEADER, "trace-2026:abc")
                .build()
        );
        AtomicReference<ServerWebExchange> forwardedExchange = new AtomicReference<>();

        filter.filter(exchange, forwarded -> {
            forwardedExchange.set(forwarded);
            return Mono.empty();
        }).block();

        String requestId = forwardedExchange.get().getRequest().getHeaders()
            .getFirst(GatewayRequestContextFilter.REQUEST_ID_HEADER);
        assertThat(requestId).matches("[0-9a-f-]{36}");
        assertThat(forwardedExchange.get().getRequest().getHeaders()
            .getFirst(GatewayRequestContextFilter.CORRELATION_ID_HEADER)).isEqualTo("trace-2026:abc");
        assertThat(exchange.getResponse().getHeaders()
            .getFirst(GatewayRequestContextFilter.REQUEST_ID_HEADER)).isEqualTo(requestId);
    }

    @Test
    void usesGeneratedRequestIdAsCorrelationIdWhenHeadersAreMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/products").build()
        );
        AtomicReference<ServerWebExchange> forwardedExchange = new AtomicReference<>();

        filter.filter(exchange, forwarded -> {
            forwardedExchange.set(forwarded);
            return Mono.empty();
        }).block();

        String requestId = forwardedExchange.get().getRequest().getHeaders()
            .getFirst(GatewayRequestContextFilter.REQUEST_ID_HEADER);
        assertThat(forwardedExchange.get().getRequest().getHeaders()
            .getFirst(GatewayRequestContextFilter.CORRELATION_ID_HEADER)).isEqualTo(requestId);
    }
}
