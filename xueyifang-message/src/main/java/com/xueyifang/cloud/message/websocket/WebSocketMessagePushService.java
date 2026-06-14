package com.xueyifang.cloud.message.websocket;

import com.xueyifang.cloud.message.dto.ChatMessageResponse;
import com.xueyifang.cloud.message.dto.NotificationResponse;
import com.xueyifang.cloud.message.service.MessagePushService;
import org.springframework.stereotype.Component;

@Component
public class WebSocketMessagePushService implements MessagePushService {

    private final MessageWebSocketSessionManager sessionManager;

    public WebSocketMessagePushService(MessageWebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void pushChatMessage(Long receiverId, ChatMessageResponse message) {
        sessionManager.sendToUser(receiverId, RealtimeMessage.newChat(message));
    }

    @Override
    public void pushNotification(Long recipientId, NotificationResponse notification) {
        sessionManager.sendToUser(recipientId, RealtimeMessage.newNotification(notification));
    }
}
