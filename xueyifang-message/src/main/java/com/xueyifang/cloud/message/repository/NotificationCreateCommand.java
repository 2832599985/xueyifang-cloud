package com.xueyifang.cloud.message.repository;

public record NotificationCreateCommand(
        Long recipientId,
        Integer notificationType,
        String title,
        String content,
        Long relatedId,
        Integer isRead) {
}
