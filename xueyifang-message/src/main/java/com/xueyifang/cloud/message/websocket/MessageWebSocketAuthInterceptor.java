package com.xueyifang.cloud.message.websocket;

import com.xueyifang.cloud.common.core.auth.AuthConstants;
import com.xueyifang.cloud.common.core.auth.JwtTokenService;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
public class MessageWebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtTokenService jwtTokenService;

    public MessageWebSocketAuthInterceptor(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        Long userId = resolveGatewayUserId(request.getHeaders());
        if (userId == null) {
            userId = resolveTokenUserId(request);
        }
        if (userId == null) {
            return false;
        }
        attributes.put(MessageWebSocketHandler.USER_ID_ATTRIBUTE, userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No state to clean up.
    }

    private Long resolveGatewayUserId(HttpHeaders headers) {
        String userId = headers.getFirst(AuthConstants.USER_ID_HEADER);
        if (!StringUtils.hasText(userId)) {
            return null;
        }
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Long resolveTokenUserId(ServerHttpRequest request) {
        String token = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("token");
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            return jwtTokenService.parseToken(token).userId();
        } catch (BusinessException exception) {
            return null;
        }
    }
}
