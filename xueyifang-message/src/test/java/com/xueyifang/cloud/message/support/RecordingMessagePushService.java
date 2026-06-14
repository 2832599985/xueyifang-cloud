package com.xueyifang.cloud.message.support;

import com.xueyifang.cloud.message.dto.ChatMessageResponse;
import com.xueyifang.cloud.message.dto.NotificationResponse;
import com.xueyifang.cloud.message.service.MessagePushService;

import java.util.ArrayList;
import java.util.List;

public class RecordingMessagePushService implements MessagePushService {

    private final List<ChatPush> chatPushes = new ArrayList<>();

    private final List<NotificationPush> notificationPushes = new ArrayList<>();

    @Override
    public void pushChatMessage(Long receiverId, ChatMessageResponse message) {
        chatPushes.add(new ChatPush(receiverId, message));
    }

    @Override
    public void pushNotification(Long recipientId, NotificationResponse notification) {
        notificationPushes.add(new NotificationPush(recipientId, notification));
    }

    public List<ChatPush> chatPushes() {
        return chatPushes;
    }

    public List<NotificationPush> notificationPushes() {
        return notificationPushes;
    }

    public record ChatPush(Long receiverId, ChatMessageResponse message) {
    }

    public record NotificationPush(Long recipientId, NotificationResponse notification) {
    }
}
