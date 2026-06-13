package com.xueyifang.cloud.trade.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderCreateRequest(
        @NotNull @Positive Long serviceId,
        @NotNull @Positive Integer quantity,
        @NotNull Integer tradeType,
        Long tradeLocationId,
        String remark) {
}
