package com.xueyifang.cloud.user.repository;

public record UserProfileUpdateCommand(
        String realName,
        String nickname,
        String phone,
        String email,
        String dormitory,
        String grade,
        Long professionalId,
        String avatar,
        String bio) {
}
