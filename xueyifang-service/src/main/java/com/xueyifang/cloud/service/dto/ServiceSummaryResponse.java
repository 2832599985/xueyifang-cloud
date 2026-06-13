package com.xueyifang.cloud.service.dto;

import com.xueyifang.cloud.service.repository.ServiceItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServiceSummaryResponse(
        Long id,
        Long serviceId,
        Long publisherId,
        String title,
        String description,
        Long tagId,
        String tagName,
        Long categoryId,
        String categoryName,
        Long professionalId,
        String professionalName,
        BigDecimal price,
        String unit,
        String location,
        Integer status,
        Integer reviewStatus,
        Integer favoriteCount,
        Integer orderCount,
        BigDecimal rating,
        String coverImage,
        LocalDateTime createTime,
        LocalDateTime updateTime) {

    public static ServiceSummaryResponse from(ServiceItem service) {
        return new ServiceSummaryResponse(
                service.id(),
                service.id(),
                service.publisherId(),
                service.title(),
                service.description(),
                service.tagId(),
                service.tagName(),
                service.categoryId(),
                service.categoryName(),
                service.professionalId(),
                service.professionalName(),
                valueOrZero(service.price()),
                service.unit(),
                service.location(),
                service.status(),
                service.reviewStatus(),
                valueOrZero(service.favoriteCount()),
                valueOrZero(service.orderCount()),
                valueOrZero(service.rating()),
                service.coverImage(),
                service.createTime(),
                service.updateTime());
    }

    private static Integer valueOrZero(Integer value) {
        return value != null ? value : 0;
    }

    private static BigDecimal valueOrZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
