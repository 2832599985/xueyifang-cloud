package com.xueyifang.cloud.service.repository;

import java.math.BigDecimal;

public record ServiceCreateCommand(
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
        String coverImage) {
}
