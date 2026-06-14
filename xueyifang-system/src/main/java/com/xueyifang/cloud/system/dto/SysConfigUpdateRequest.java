package com.xueyifang.cloud.system.dto;

import jakarta.validation.constraints.NotNull;

public record SysConfigUpdateRequest(
        @NotNull(message = "id must not be null")
        Long id,
        String configValue,
        String description,
        Integer isEnabled) {
}
