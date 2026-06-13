package com.xueyifang.cloud.user.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserAccount(
        Long id,
        String username,
        String password,
        String studentId,
        String realName,
        String nickname,
        String phone,
        String email,
        String dormitory,
        String grade,
        Long professionalId,
        String avatar,
        String bio,
        String role,
        Integer publishPermission,
        Integer permissionReviewStatus,
        BigDecimal walletBalance,
        BigDecimal frozenAmount,
        Integer status,
        Integer accountStatus,
        LocalDateTime createTime,
        LocalDateTime updateTime) {
}
