package com.xueyifang.cloud.service.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServiceItem(
        Long id,
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
}
