package com.xueyifang.cloud.message.service;

import com.xueyifang.cloud.message.dto.ChatMessageResponse;
import com.xueyifang.cloud.message.dto.NotificationResponse;

public interface MessagePushService {

    void pushChatMessage(Long receiverId, ChatMessageResponse message);

    void pushNotification(Long recipientId, NotificationResponse notification);
}
