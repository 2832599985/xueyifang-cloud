package com.xueyifang.cloud.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 100, message = "realName length must not exceed 100")
        String realName,
        @Size(max = 64, message = "nickname length must not exceed 64")
        String nickname,
        @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "phone format is invalid")
        String phone,
        @Pattern(regexp = "^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                message = "email format is invalid")
        String email,
        @Size(max = 100, message = "dormitory length must not exceed 100")
        String dormitory,
        @Size(max = 20, message = "grade length must not exceed 20")
        String grade,
        Long professionalId,
        @Size(max = 255, message = "avatar length must not exceed 255")
        String avatar,
        @Size(max = 500, message = "bio length must not exceed 500")
        String bio) {
}
