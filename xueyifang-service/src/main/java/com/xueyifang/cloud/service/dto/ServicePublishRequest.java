package com.xueyifang.cloud.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ServicePublishRequest(
        @Size(max = 100, message = "title length must not exceed 100")
        String title,
        @Size(max = 100, message = "serviceTitle length must not exceed 100")
        String serviceTitle,
        @Size(max = 5000, message = "description length must not exceed 5000")
        String description,
        @Size(max = 5000, message = "serviceDescription length must not exceed 5000")
        String serviceDescription,
        Long tagId,
        @Size(max = 64, message = "tagName length must not exceed 64")
        String tagName,
        Long categoryId,
        @Size(max = 64, message = "categoryName length must not exceed 64")
        String categoryName,
        Long professionalId,
        @Size(max = 100, message = "professionalName length must not exceed 100")
        String professionalName,
        @DecimalMin(value = "0.01", message = "price must be greater than 0")
        BigDecimal price,
        @Size(max = 20, message = "unit length must not exceed 20")
        String unit,
        @Size(max = 100, message = "location length must not exceed 100")
        String location,
        @Size(max = 255, message = "coverImage length must not exceed 255")
        String coverImage,
        List<@Size(max = 255, message = "image url length must not exceed 255") String> images,
        Integer tradeType,
        Long tradeLocationId,
        Integer maxPurchases,
        Integer isPhysical) {
}
