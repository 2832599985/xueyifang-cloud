package com.xueyifang.cloud.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.common.core.auth.AuthConstants;
import com.xueyifang.cloud.common.core.auth.AuthTokenUtils;
import com.xueyifang.cloud.common.core.auth.JwtTokenClaims;
import com.xueyifang.cloud.common.core.auth.JwtTokenService;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.gateway.auth.ReactiveTokenBlacklistService;
import com.xueyifang.cloud.gateway.config.GatewayAuthProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Component
public class GatewayAuthFilter implements GlobalFilter, Ordered {

    private static final List<String> USER_CONTEXT_HEADERS = List.of(
            AuthConstants.USER_ID_HEADER,
            AuthConstants.USER_ROLE_HEADER,
            AuthConstants.USER_PUBLISH_PERMISSION_HEADER
    );

    private final GatewayAuthProperties properties;

    private final JwtTokenService jwtTokenService;

    private final ReactiveTokenBlacklistService tokenBlacklistService;

    private final ObjectMapper objectMapper;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public GatewayAuthFilter(GatewayAuthProperties properties, JwtTokenService jwtTokenService,
                             ReactiveTokenBlacklistService tokenBlacklistService, ObjectMapper objectMapper) {
        this.properties = properties;
        this.jwtTokenService = jwtTokenService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerWebExchange trustedExchange = removeUntrustedUserHeaders(exchange);

        if (!properties.isEnabled()) {
            return chain.filter(trustedExchange);
        }

        if (isPublicRequest(trustedExchange)) {
            return continueWithOptionalUserContext(trustedExchange, chain);
        }

        Optional<String> token = resolveToken(trustedExchange.getRequest().getHeaders());
        if (token.isEmpty()) {
            return writeError(trustedExchange, ErrorCode.USER_NOT_LOGIN.getCode(), "请先登录");
        }

        String rawToken = token.get();
        return isBlacklisted(rawToken).flatMap(blacklisted -> {
            if (blacklisted) {
                return writeError(trustedExchange, ErrorCode.TOKEN_INVALID.getCode(), "Token has been logged out");
            }

            try {
                JwtTokenClaims claims = jwtTokenService.parseToken(rawToken);
                return chain.filter(addUserContextHeaders(trustedExchange, claims));
            } catch (BusinessException exception) {
                return writeError(trustedExchange, exception.getCode(), exception.getMessage());
            }
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    private Mono<Void> continueWithOptionalUserContext(ServerWebExchange exchange, GatewayFilterChain chain) {
        Optional<String> token = resolveToken(exchange.getRequest().getHeaders());
        if (token.isEmpty()) {
            return chain.filter(exchange);
        }

        String rawToken = token.get();
        return isBlacklisted(rawToken).flatMap(blacklisted -> {
            if (blacklisted) {
                return chain.filter(exchange);
            }

            try {
                JwtTokenClaims claims = jwtTokenService.parseToken(rawToken);
                return chain.filter(addUserContextHeaders(exchange, claims));
            } catch (BusinessException exception) {
                return chain.filter(exchange);
            }
        });
    }

    private Mono<Boolean> isBlacklisted(String token) {
        return tokenBlacklistService.isBlacklisted(token).defaultIfEmpty(false);
    }

    private ServerWebExchange removeUntrustedUserHeaders(ServerWebExchange exchange) {
        return exchange.mutate()
                .request(builder -> builder.headers(headers ->
                        USER_CONTEXT_HEADERS.forEach(headers::remove)))
                .build();
    }

    private ServerWebExchange addUserContextHeaders(ServerWebExchange exchange, JwtTokenClaims claims) {
        return exchange.mutate()
                .request(builder -> builder.headers(headers -> {
                    headers.set(AuthConstants.USER_ID_HEADER, String.valueOf(claims.userId()));
                    setHeaderIfNotNull(headers, AuthConstants.USER_ROLE_HEADER, claims.role());
                    setHeaderIfNotNull(headers, AuthConstants.USER_PUBLISH_PERMISSION_HEADER,
                            claims.publishPermission());
                }))
                .build();
    }

    private void setHeaderIfNotNull(HttpHeaders headers, String headerName, Object value) {
        if (value != null) {
            headers.set(headerName, String.valueOf(value));
        }
    }

    private boolean isPublicRequest(ServerWebExchange exchange) {
        HttpMethod method = exchange.getRequest().getMethod();
        if (HttpMethod.OPTIONS.equals(method)) {
            return true;
        }

        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        if (matchesAny(path, properties.getPublicPaths())) {
            return true;
        }

        return HttpMethod.GET.equals(method) && matchesAny(path, properties.getPublicGetPaths());
    }

    private boolean matchesAny(String path, List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        return patterns.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Optional<String> resolveToken(HttpHeaders headers) {
        return AuthTokenUtils.resolveToken(
                headers.getFirst(AuthConstants.AUTHORIZATION_HEADER),
                headers.getFirst(AuthConstants.LEGACY_TOKEN_HEADER));
    }

    private Mono<Void> writeError(ServerWebExchange exchange, int code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(mapHttpStatus(code));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        BaseResponse<Void> body = ResultUtils.error(code, message);
        byte[] bytes = serializeBody(body);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    private byte[] serializeBody(BaseResponse<Void> body) {
        try {
            return objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException exception) {
            return "{\"code\":50000,\"message\":\"系统错误\",\"data\":null}".getBytes();
        }
    }

    private HttpStatus mapHttpStatus(int code) {
        if (code == ErrorCode.USER_NOT_LOGIN.getCode()
                || code == ErrorCode.TOKEN_INVALID.getCode()
                || code == ErrorCode.TOKEN_EXPIRED.getCode()) {
            return HttpStatus.UNAUTHORIZED;
        }

        if (code == ErrorCode.NO_AUTH_ERROR.getCode()
                || code == ErrorCode.FORBIDDEN_ERROR.getCode()
                || code == ErrorCode.PERMISSION_DENIED.getCode()) {
            return HttpStatus.FORBIDDEN;
        }

        return HttpStatus.BAD_REQUEST;
    }
}
