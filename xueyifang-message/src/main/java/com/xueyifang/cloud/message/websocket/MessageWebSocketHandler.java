package com.xueyifang.cloud.message.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class MessageWebSocketHandler extends TextWebSocketHandler {

    public static final String USER_ID_ATTRIBUTE = "userId";

    private final MessageWebSocketSessionManager sessionManager;

    public MessageWebSocketHandler(MessageWebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = userId(session);
        sessionManager.addSession(userId, session);
        sessionManager.sendToUser(userId, RealtimeMessage.connected(userId));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        if (message.getPayload() != null && message.getPayload().contains("\"PING\"")) {
            sessionManager.sendToUser(userId(session), RealtimeMessage.pong());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionManager.removeSession(userId(session), session);
    }

    private Long userId(WebSocketSession session) {
        Object userId = session.getAttributes().get(USER_ID_ATTRIBUTE);
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(userId));
    }
}
