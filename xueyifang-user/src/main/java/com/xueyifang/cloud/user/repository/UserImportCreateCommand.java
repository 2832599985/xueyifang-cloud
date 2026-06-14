package com.xueyifang.cloud.user.repository;

import java.math.BigDecimal;

public record UserImportCreateCommand(
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
        String role,
        Integer publishPermission,
        Integer permissionReviewStatus,
        BigDecimal walletBalance,
        BigDecimal frozenAmount,
        Integer status,
        Integer accountStatus) {
}
