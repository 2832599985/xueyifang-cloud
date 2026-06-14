package com.xueyifang.cloud.message.repository;

import java.time.LocalDateTime;

public record ChatMessageItem(
        Long id,
        Long senderId,
        Long receiverId,
        String content,
        Integer messageType,
        Integer isRead,
        Long relatedServiceId,
        Long relatedOrderId,
        LocalDateTime createTime,
        LocalDateTime updateTime) {
}
