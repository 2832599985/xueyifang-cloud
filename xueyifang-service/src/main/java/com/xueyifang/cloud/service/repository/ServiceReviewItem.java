package com.xueyifang.cloud.service.repository;

import java.time.LocalDateTime;

public record ServiceReviewItem(
        Long id,
        Long serviceId,
        Long orderId,
        Long buyerId,
        Long sellerId,
        Integer rating,
        String content,
        Boolean anonymous,
        LocalDateTime createTime,
        String reviewerName,
        String reviewerAvatar) {
}
