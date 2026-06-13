package com.xueyifang.cloud.service.dto;

import com.xueyifang.cloud.service.repository.ServiceReviewItem;

import java.time.LocalDateTime;

public record ServiceReviewResponse(
        Long id,
        Long orderId,
        Integer rating,
        String content,
        LocalDateTime createTime,
        Long reviewerId,
        String reviewerName,
        String reviewerAvatar,
        Boolean anonymous) {

    public static ServiceReviewResponse from(ServiceReviewItem item) {
        boolean anonymous = Boolean.TRUE.equals(item.anonymous());
        return new ServiceReviewResponse(
                item.id(),
                item.orderId(),
                item.rating(),
                item.content(),
                item.createTime(),
                anonymous ? null : item.buyerId(),
                anonymous ? "匿名用户" : item.reviewerName(),
                anonymous ? null : item.reviewerAvatar(),
                anonymous);
    }
}
