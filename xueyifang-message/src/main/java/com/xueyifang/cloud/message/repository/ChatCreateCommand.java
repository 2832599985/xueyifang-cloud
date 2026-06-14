package com.xueyifang.cloud.message.repository;

public record ChatCreateCommand(
        Long senderId,
        Long receiverId,
        String content,
        Integer messageType,
        Integer isRead,
        Long relatedServiceId,
        Long relatedOrderId) {
}
