package com.xueyifang.cloud.service.repository;

public record ServiceImage(
        Long id,
        Long serviceId,
        String imageUrl,
        Integer sortOrder,
        Boolean cover) {
}
