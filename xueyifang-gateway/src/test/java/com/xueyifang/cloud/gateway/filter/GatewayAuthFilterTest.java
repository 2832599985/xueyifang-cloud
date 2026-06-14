package com.xueyifang.cloud.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.auth.AuthConstants;
import com.xueyifang.cloud.common.core.auth.JwtToken;
import com.xueyifang.cloud.common.core.auth.JwtTokenProperties;
import com.xueyifang.cloud.common.core.auth.JwtTokenService;
import com.xueyifang.cloud.gateway.config.GatewayAuthProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayAuthFilterTest {

    private static final String SECRET = "xueyifang-secret-key-2025-graduation-project";

    private static final Instant NOW = Instant.parse("2026-06-14T00:00:00Z");

    private final JwtTokenService jwtTokenService = new JwtTokenService(
            new JwtTokenProperties(SECRET, Duration.ofDays(7), JwtTokenProperties.DEFAULT_ISSUER),
            Clock.fixed(NOW, ZoneOffset.UTC));

    private final Set<String> blacklistedTokens = new HashSet<>();

    private final GatewayAuthFilter filter = new GatewayAuthFilter(
            new GatewayAuthProperties(),
            jwtTokenService,
            token -> Mono.just(blacklistedTokens.contains(token)),
            new ObjectMapper());

    @Test
    void allowsPublicPathWithoutTokenAndRemovesSpoofedUserHeaders() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/auth/login")
                .header(AuthConstants.USER_ID_HEADER, "999")
                .build());
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.filter(exchange, chainExchange -> {
            chainCalled.set(true);
            assertThat(chainExchange.getRequest().getHeaders()).doesNotContainKey(AuthConstants.USER_ID_HEADER);
            return chainExchange.getResponse().setComplete();
        }).block();

        assertThat(chainCalled).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void allowsSystemPublicReadPathsWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/trade-location/list/all").build());
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.filter(exchange, chainExchange -> {
            chainCalled.set(true);
            return chainExchange.getResponse().setComplete();
        }).block();

        assertThat(chainCalled).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void rejectsProtectedPathWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/order/my-buying").build());
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.filter(exchange, chainExchange -> {
            chainCalled.set(true);
            return chainExchange.getResponse().setComplete();
        }).block();

        assertThat(chainCalled).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getBodyAsString().block())
                .contains("\"code\":" + ErrorCode.USER_NOT_LOGIN.getCode())
                .contains("请先登录");
    }

    @Test
    void forwardsTrustedUserHeadersForValidToken() {
        JwtToken token = jwtTokenService.createToken(123L, 2, 1);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/order/my-buying")
                .header(AuthConstants.AUTHORIZATION_HEADER, AuthConstants.BEARER_PREFIX + token.token())
                .header(AuthConstants.USER_ID_HEADER, "999")
                .build());

        filter.filter(exchange, chainExchange -> {
            assertThat(chainExchange.getRequest().getHeaders().getFirst(AuthConstants.USER_ID_HEADER))
                    .isEqualTo("123");
            assertThat(chainExchange.getRequest().getHeaders().getFirst(AuthConstants.USER_ROLE_HEADER))
                    .isEqualTo("2");
            assertThat(chainExchange.getRequest().getHeaders()
                    .getFirst(AuthConstants.USER_PUBLISH_PERMISSION_HEADER))
                    .isEqualTo("1");
            return chainExchange.getResponse().setComplete();
        }).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void rejectsMalformedToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/wallet/balance")
                .header(AuthConstants.AUTHORIZATION_HEADER, AuthConstants.BEARER_PREFIX + "bad-token")
                .build());

        filter.filter(exchange, chainExchange -> chainExchange.getResponse().setComplete()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getBodyAsString().block())
                .contains("\"code\":" + ErrorCode.TOKEN_INVALID.getCode())
                .contains("Token无效");
    }

    @Test
    void rejectsBlacklistedToken() {
        JwtToken token = jwtTokenService.createToken(123L, 2, 1);
        blacklistedTokens.add(token.token());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/wallet/balance")
                .header(AuthConstants.AUTHORIZATION_HEADER, AuthConstants.BEARER_PREFIX + token.token())
                .build());
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.filter(exchange, chainExchange -> {
            chainCalled.set(true);
            return chainExchange.getResponse().setComplete();
        }).block();

        assertThat(chainCalled).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getBodyAsString().block())
                .contains("\"code\":" + ErrorCode.TOKEN_INVALID.getCode())
                .contains("Token has been logged out");
    }
}
