package com.xueyifang.cloud.message.dto;

import java.time.LocalDateTime;

public record ChatConversationResponse(
        Long userId,
        String realName,
        String avatar,
        String lastMessage,
        Integer unreadCount,
        LocalDateTime lastMessageTime) {
}
