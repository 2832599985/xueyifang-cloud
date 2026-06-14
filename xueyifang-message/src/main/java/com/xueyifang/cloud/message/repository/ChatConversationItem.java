package com.xueyifang.cloud.message.repository;

import java.time.LocalDateTime;

public record ChatConversationItem(
        Long userId,
        String realName,
        String avatar,
        String lastMessage,
        Integer unreadCount,
        LocalDateTime lastMessageTime) {
}
