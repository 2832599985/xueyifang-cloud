package com.xueyifang.cloud.gateway.filter;

import com.xueyifang.cloud.common.core.context.TraceConstants;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class GatewayRequestIdFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = resolveRequestId(exchange.getRequest().getHeaders());
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(builder -> builder.headers(headers ->
                        headers.set(TraceConstants.REQUEST_ID_HEADER, requestId)))
                .build();
        mutatedExchange.getResponse().getHeaders().set(TraceConstants.REQUEST_ID_HEADER, requestId);

        return Mono.defer(() -> {
            MDC.put(TraceConstants.MDC_REQUEST_ID_KEY, requestId);
            return chain.filter(mutatedExchange)
                    .doFinally(signalType -> MDC.remove(TraceConstants.MDC_REQUEST_ID_KEY));
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private String resolveRequestId(HttpHeaders headers) {
        String requestId = headers.getFirst(TraceConstants.REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return requestId.trim();
    }
}
