package com.xueyifang.cloud.service.dto;

import com.xueyifang.cloud.service.repository.ServiceImage;
import com.xueyifang.cloud.service.repository.ServiceItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ServiceDetailResponse(
        Long id,
        Long serviceId,
        Long publisherId,
        Long sellerId,
        String serviceTitle,
        String serviceDescription,
        String requirements,
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
        Integer tradeType,
        Integer isPhysical,
        Integer maxPurchases,
        Integer currentPurchaseCount,
        Integer favoriteCount,
        Integer collectionCount,
        Integer orderCount,
        Integer viewCount,
        Integer reviewCount,
        BigDecimal rating,
        String coverImage,
        List<String> images,
        Boolean isCollected,
        Object tradeLocation,
        LocalDateTime createTime,
        LocalDateTime updateTime) {

    public static ServiceDetailResponse from(ServiceItem service, List<ServiceImage> images) {
        return from(service, images, null);
    }

    public static ServiceDetailResponse from(ServiceItem service, List<ServiceImage> images, Boolean isCollected) {
        List<String> imageResponses = images.stream()
                .map(ServiceImage::imageUrl)
                .toList();
        return new ServiceDetailResponse(
                service.id(),
                service.id(),
                service.publisherId(),
                service.publisherId(),
                service.title(),
                service.description(),
                null,
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
                2,
                0,
                -1,
                0,
                valueOrZero(service.favoriteCount()),
                valueOrZero(service.favoriteCount()),
                valueOrZero(service.orderCount()),
                0,
                0,
                valueOrZero(service.rating()),
                service.coverImage(),
                imageResponses,
                isCollected,
                null,
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