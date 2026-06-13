package com.xueyifang.cloud.user.dto;

import jakarta.validation.constraints.Size;

public record PermissionApplyRequest(
        @Size(max = 200, message = "reason length must not exceed 200")
        String reason) {
}
