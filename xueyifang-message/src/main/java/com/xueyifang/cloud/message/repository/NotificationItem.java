package com.xueyifang.cloud.message.repository;

import java.time.LocalDateTime;

public record NotificationItem(
        Long id,
        Long recipientId,
        Integer notificationType,
        String title,
        String content,
        Long relatedId,
        Integer isRead,
        LocalDateTime createTime,
        LocalDateTime updateTime) {
}
