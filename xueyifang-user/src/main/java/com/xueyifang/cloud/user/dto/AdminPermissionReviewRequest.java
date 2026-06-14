package com.xueyifang.cloud.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminPermissionReviewRequest(
        @NotNull(message = "userId must not be null")
        Long userId,
        @NotNull(message = "approved must not be null")
        Boolean approved,
        @Size(max = 200, message = "reason length must not exceed 200")
        String reason) {
}
