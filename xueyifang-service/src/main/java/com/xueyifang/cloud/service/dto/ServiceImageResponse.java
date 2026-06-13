package com.xueyifang.cloud.service.dto;

import com.xueyifang.cloud.service.repository.ServiceImage;

public record ServiceImageResponse(
        Long id,
        Long imageId,
        Long serviceId,
        String imageUrl,
        Integer sortOrder,
        Boolean cover) {

    public static ServiceImageResponse from(ServiceImage image) {
        return new ServiceImageResponse(
                image.id(),
                image.id(),
                image.serviceId(),
                image.imageUrl(),
                image.sortOrder(),
                Boolean.TRUE.equals(image.cover()));
    }
}
