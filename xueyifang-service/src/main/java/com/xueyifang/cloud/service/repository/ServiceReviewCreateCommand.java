package com.xueyifang.cloud.service.repository;

public record ServiceReviewCreateCommand(
        Long serviceId,
        Long orderId,
        Long buyerId,
        Long sellerId,
        Integer rating,
        String content,
        boolean anonymous) {
}
