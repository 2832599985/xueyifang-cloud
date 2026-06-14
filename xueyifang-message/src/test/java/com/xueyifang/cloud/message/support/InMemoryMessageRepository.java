package com.xueyifang.cloud.message.support;

import com.xueyifang.cloud.message.repository.ChatConversationItem;
import com.xueyifang.cloud.message.repository.ChatCreateCommand;
import com.xueyifang.cloud.message.repository.ChatMessageItem;
import com.xueyifang.cloud.message.repository.MessagePage;
import com.xueyifang.cloud.message.repository.MessageRepository;
import com.xueyifang.cloud.message.repository.NotificationCreateCommand;
import com.xueyifang.cloud.message.repository.NotificationItem;
import com.xueyifang.cloud.message.repository.UserSummary;
import com.xueyifang.cloud.message.service.MessageConstants;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InMemoryMessageRepository implements MessageRepository {

    private final Map<Long, UserSummary> users = new LinkedHashMap<>();

    private final Map<Long, ChatMessageItem> chats = new LinkedHashMap<>();

    private final Map<Long, NotificationItem> notifications = new LinkedHashMap<>();

    private final AtomicLong chatId = new AtomicLong(1);

    private final AtomicLong notificationId = new AtomicLong(1);

    public void clear() {
        users.clear();
        chats.clear();
        notifications.clear();
        chatId.set(1);
        notificationId.set(1);
    }

    public void putUser(Long userId, String realName, String avatar, Integer accountStatus) {
        users.put(userId, new UserSummary(userId, realName, avatar, accountStatus));
    }

    public Long putNotification(Long recipientId, Integer notificationType, String title, String content,
                                Long relatedId, Integer isRead) {
        Long id = notificationId.getAndIncrement();
        LocalDateTime now = now(id);
        notifications.put(id, new NotificationItem(
                id,
                recipientId,
                notificationType,
                title,
                content,
                relatedId,
                isRead,
                now,
                now));
        return id;
    }

    @Override
    public Optional<UserSummary> findUserById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Map<Long, UserSummary> findUsersByIds(Set<Long> userIds) {
        return users.entrySet().stream()
                .filter(entry -> userIds.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Long createChatMessage(ChatCreateCommand command) {
        Long id = chatId.getAndIncrement();
        LocalDateTime now = now(id);
        chats.put(id, new ChatMessageItem(
                id,
                command.senderId(),
                command.receiverId(),
                command.content(),
                command.messageType(),
                command.isRead(),
                command.relatedServiceId(),
                command.relatedOrderId(),
                now,
                now));
        return id;
    }

    @Override
    public Optional<ChatMessageItem> findChatMessageById(Long messageId) {
        return Optional.ofNullable(chats.get(messageId));
    }

    @Override
    public MessagePage<ChatMessageItem> findChatMessages(Long userId, Long targetUserId, int offset, int limit) {
        Predicate<ChatMessageItem> conversation = item ->
                (item.senderId().equals(userId) && item.receiverId().equals(targetUserId))
                        || (item.senderId().equals(targetUserId) && item.receiverId().equals(userId));
        List<ChatMessageItem> matched = chats.values().stream()
                .filter(conversation)
                .sorted(Comparator.comparing(ChatMessageItem::createTime).thenComparing(ChatMessageItem::id))
                .toList();
        return new MessagePage<>(page(matched, offset, limit), matched.size());
    }

    @Override
    public int markConversationRead(Long userId, Long targetUserId) {
        int updated = 0;
        for (ChatMessageItem item : new ArrayList<>(chats.values())) {
            if (item.receiverId().equals(userId)
                    && item.senderId().equals(targetUserId)
                    && Integer.valueOf(MessageConstants.READ_STATUS_UNREAD).equals(item.isRead())) {
                chats.put(item.id(), new ChatMessageItem(
                        item.id(),
                        item.senderId(),
                        item.receiverId(),
                        item.content(),
                        item.messageType(),
                        MessageConstants.READ_STATUS_READ,
                        item.relatedServiceId(),
                        item.relatedOrderId(),
                        item.createTime(),
                        item.updateTime().plusSeconds(1)));
                updated++;
            }
        }
        return updated;
    }

    @Override
    public List<ChatConversationItem> findConversations(Long userId) {
        Map<Long, List<ChatMessageItem>> grouped = chats.values().stream()
                .filter(item -> item.senderId().equals(userId) || item.receiverId().equals(userId))
                .collect(Collectors.groupingBy(item -> item.senderId().equals(userId)
                        ? item.receiverId()
                        : item.senderId()));

        List<ChatConversationItem> conversations = new ArrayList<>();
        for (Map.Entry<Long, List<ChatMessageItem>> entry : grouped.entrySet()) {
            UserSummary other = users.get(entry.getKey());
            if (other == null) {
                continue;
            }
            ChatMessageItem latest = entry.getValue().stream()
                    .max(Comparator.comparing(ChatMessageItem::createTime).thenComparing(ChatMessageItem::id))
                    .orElseThrow();
            int unreadCount = (int) entry.getValue().stream()
                    .filter(item -> item.receiverId().equals(userId))
                    .filter(item -> Integer.valueOf(MessageConstants.READ_STATUS_UNREAD).equals(item.isRead()))
                    .count();
            conversations.add(new ChatConversationItem(
                    other.userId(),
                    other.realName(),
                    other.avatar(),
                    latest.content(),
                    unreadCount,
                    latest.createTime()));
        }
        conversations.sort(Comparator.comparing(ChatConversationItem::lastMessageTime).reversed());
        return conversations;
    }

    @Override
    public Long createNotification(NotificationCreateCommand command) {
        return putNotification(
                command.recipientId(),
                command.notificationType(),
                command.title(),
                command.content(),
                command.relatedId(),
                command.isRead());
    }

    @Override
    public Optional<NotificationItem> findNotificationById(Long notificationId) {
        return Optional.ofNullable(notifications.get(notificationId));
    }

    @Override
    public MessagePage<NotificationItem> findNotifications(Long recipientId, Integer notificationType,
                                                           int offset, int limit) {
        List<NotificationItem> matched = notifications.values().stream()
                .filter(item -> item.recipientId().equals(recipientId))
                .filter(item -> notificationType == null || notificationType.equals(item.notificationType()))
                .sorted(Comparator.comparing(NotificationItem::createTime).reversed()
                        .thenComparing(NotificationItem::id, Comparator.reverseOrder()))
                .toList();
        return new MessagePage<>(page(matched, offset, limit), matched.size());
    }

    @Override
    public long countUnreadNotifications(Long recipientId) {
        return notifications.values().stream()
                .filter(item -> item.recipientId().equals(recipientId))
                .filter(item -> Integer.valueOf(MessageConstants.READ_STATUS_UNREAD).equals(item.isRead()))
                .count();
    }

    @Override
    public boolean markNotificationRead(Long notificationId, Long recipientId) {
        NotificationItem item = notifications.get(notificationId);
        if (item == null || !item.recipientId().equals(recipientId)) {
            return false;
        }
        notifications.put(notificationId, new NotificationItem(
                item.id(),
                item.recipientId(),
                item.notificationType(),
                item.title(),
                item.content(),
                item.relatedId(),
                MessageConstants.READ_STATUS_READ,
                item.createTime(),
                item.updateTime().plusSeconds(1)));
        return true;
    }

    @Override
    public int markAllNotificationsRead(Long recipientId) {
        int updated = 0;
        for (NotificationItem item : new ArrayList<>(notifications.values())) {
            if (item.recipientId().equals(recipientId)
                    && Integer.valueOf(MessageConstants.READ_STATUS_UNREAD).equals(item.isRead())) {
                markNotificationRead(item.id(), recipientId);
                updated++;
            }
        }
        return updated;
    }

    private <T> List<T> page(List<T> items, int offset, int limit) {
        if (offset >= items.size()) {
            return List.of();
        }
        return new ArrayList<>(items.subList(offset, Math.min(offset + limit, items.size())));
    }

    private LocalDateTime now(Long id) {
        return LocalDateTime.parse("2026-06-14T00:00:00").plusSeconds(id);
    }
}
