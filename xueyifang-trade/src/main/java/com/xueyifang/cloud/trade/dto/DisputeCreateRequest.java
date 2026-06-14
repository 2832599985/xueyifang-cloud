package com.xueyifang.cloud.trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DisputeCreateRequest(
        @NotNull Long orderId,
        @NotBlank @Size(max = 500) String reason,
        @Size(max = 1000) String evidence) {
}
