package com.xueyifang.cloud.message.websocket;

import com.xueyifang.cloud.message.dto.ChatMessageResponse;
import com.xueyifang.cloud.message.dto.NotificationResponse;
import com.xueyifang.cloud.message.service.MessagePushService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "xueyifang.message.push.redis", name = "enabled", havingValue = "false",
        matchIfMissing = true)
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
