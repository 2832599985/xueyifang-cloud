package com.xueyifang.cloud.auth.repository;

public record AuthUserCreateCommand(
        String username,
        String password,
        String nickname,
        String phone,
        String email,
        String role,
        Integer publishPermission,
        Integer status) {
}
