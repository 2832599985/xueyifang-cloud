package com.xueyifang.cloud.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminServiceReviewRequest(
        @NotNull(message = "serviceId must not be null")
        Long serviceId,
        @NotNull(message = "approved must not be null")
        Boolean approved,
        @Size(max = 200, message = "reason length must not exceed 200")
        String reason) {
}
