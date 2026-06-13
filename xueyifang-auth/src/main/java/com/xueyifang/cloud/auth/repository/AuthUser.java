package com.xueyifang.cloud.auth.repository;

public record AuthUser(
        Long id,
        String username,
        String password,
        String nickname,
        String role,
        Integer status) {
}
