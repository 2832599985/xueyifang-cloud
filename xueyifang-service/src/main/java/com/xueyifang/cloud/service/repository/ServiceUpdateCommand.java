package com.xueyifang.cloud.service.repository;

import java.math.BigDecimal;

public record ServiceUpdateCommand(
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
        String coverImage) {

    public boolean hasChanges() {
        return title != null
                || description != null
                || tagId != null
                || tagName != null
                || categoryId != null
                || categoryName != null
                || professionalId != null
                || professionalName != null
                || price != null
                || unit != null
                || location != null
                || coverImage != null;
    }
}
