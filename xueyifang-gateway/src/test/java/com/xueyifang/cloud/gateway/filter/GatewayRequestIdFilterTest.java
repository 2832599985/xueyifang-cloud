package com.xueyifang.cloud.gateway.filter;

import com.xueyifang.cloud.common.core.context.TraceConstants;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayRequestIdFilterTest {

    private final GatewayRequestIdFilter filter = new GatewayRequestIdFilter();

    @Test
    void generatesAndForwardsRequestIdWhenMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/user/health").build());
        AtomicReference<String> requestIdInChain = new AtomicReference<>();

        filter.filter(exchange, chainExchange -> {
            String requestId = chainExchange.getRequest().getHeaders()
                    .getFirst(TraceConstants.REQUEST_ID_HEADER);
            requestIdInChain.set(requestId);
            assertThat(MDC.get(TraceConstants.MDC_REQUEST_ID_KEY)).isEqualTo(requestId);
            return chainExchange.getResponse().setComplete();
        }).block();

        assertThat(requestIdInChain.get()).isNotBlank();
        assertThat(exchange.getResponse().getHeaders().getFirst(TraceConstants.REQUEST_ID_HEADER))
                .isEqualTo(requestIdInChain.get());
        assertThat(MDC.get(TraceConstants.MDC_REQUEST_ID_KEY)).isNull();
    }

    @Test
    void reusesIncomingRequestId() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/user/health")
                .header(TraceConstants.REQUEST_ID_HEADER, "  req-456  ")
                .build());

        filter.filter(exchange, chainExchange -> {
            assertThat(chainExchange.getRequest().getHeaders().getFirst(TraceConstants.REQUEST_ID_HEADER))
                    .isEqualTo("req-456");
            return chainExchange.getResponse().setComplete();
        }).block();

        assertThat(exchange.getResponse().getHeaders().getFirst(TraceConstants.REQUEST_ID_HEADER))
                .isEqualTo("req-456");
    }
}
