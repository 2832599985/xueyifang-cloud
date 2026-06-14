package com.xueyifang.cloud.message.repository;

public record UserSummary(
        Long userId,
        String realName,
        String avatar,
        Integer accountStatus) {
}
