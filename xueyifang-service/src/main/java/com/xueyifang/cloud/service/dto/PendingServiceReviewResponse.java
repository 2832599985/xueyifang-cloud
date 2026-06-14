package com.xueyifang.cloud.service.dto;

import com.xueyifang.cloud.service.repository.ServiceItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PendingServiceReviewResponse(
        Long serviceId,
        String serviceTitle,
        String title,
        String coverImage,
        String description,
        Long publisherId,
        String sellerName,
        String tagName,
        BigDecimal price,
        Integer status,
        Integer reviewStatus,
        LocalDateTime createTime) {

    public static PendingServiceReviewResponse from(ServiceItem service) {
        return new PendingServiceReviewResponse(
                service.id(),
                service.title(),
                service.title(),
                service.coverImage(),
                service.description(),
                service.publisherId(),
                null,
                service.tagName(),
                service.price(),
                service.status(),
                service.reviewStatus(),
                service.createTime());
    }
}
