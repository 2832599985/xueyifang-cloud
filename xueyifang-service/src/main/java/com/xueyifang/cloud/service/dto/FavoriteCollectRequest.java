package com.xueyifang.cloud.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FavoriteCollectRequest(
        @NotNull(message = "serviceId must not be null")
        @Positive(message = "serviceId must be positive")
        Long serviceId) {
}
