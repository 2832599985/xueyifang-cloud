package com.xueyifang.cloud.message.dto;

public record UserSimpleResponse(
        Long userId,
        String realName,
        String avatar) {
}
