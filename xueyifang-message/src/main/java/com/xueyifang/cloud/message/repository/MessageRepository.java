package com.xueyifang.cloud.message.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface MessageRepository {

    Optional<UserSummary> findUserById(Long userId);

    Map<Long, UserSummary> findUsersByIds(Set<Long> userIds);

    Long createChatMessage(ChatCreateCommand command);

    Optional<ChatMessageItem> findChatMessageById(Long messageId);

    MessagePage<ChatMessageItem> findChatMessages(Long userId, Long targetUserId, int offset, int limit);

    int markConversationRead(Long userId, Long targetUserId);

    List<ChatConversationItem> findConversations(Long userId);

    Long createNotification(NotificationCreateCommand command);

    Optional<NotificationItem> findNotificationById(Long notificationId);

    MessagePage<NotificationItem> findNotifications(Long recipientId, Integer notificationType, int offset, int limit);

    long countUnreadNotifications(Long recipientId);

    boolean markNotificationRead(Long notificationId, Long recipientId);

    int markAllNotificationsRead(Long recipientId);
}
