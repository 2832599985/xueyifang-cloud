package com.xueyifang.cloud.message.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Integer notificationType,
        String title,
        String content,
        Long relatedId,
        Integer isRead,
        LocalDateTime createTime) {
}
