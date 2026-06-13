package com.xueyifang.cloud.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "oldPassword must not be blank")
        String oldPassword,
        @NotBlank(message = "newPassword must not be blank")
        @Size(min = 6, max = 32, message = "newPassword length must be between 6 and 32")
        String newPassword,
        @NotBlank(message = "confirmPassword must not be blank")
        String confirmPassword) {
}
