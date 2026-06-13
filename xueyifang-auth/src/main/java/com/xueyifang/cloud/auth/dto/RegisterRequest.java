package com.xueyifang.cloud.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "username must not be blank")
        @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$",
                message = "username must be 4-20 letters, numbers, or underscores")
        String username,
        @NotBlank(message = "password must not be blank")
        @Size(min = 6, max = 32, message = "password length must be between 6 and 32")
        String password,
        @Size(max = 32, message = "nickname length must not exceed 32")
        String nickname,
        @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "phone format is invalid")
        String phone,
        @Pattern(regexp = "^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                message = "email format is invalid")
        String email) {
}
