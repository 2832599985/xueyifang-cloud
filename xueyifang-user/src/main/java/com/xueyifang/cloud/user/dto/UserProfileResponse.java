package com.xueyifang.cloud.user.dto;

import com.xueyifang.cloud.user.repository.UserAccount;
import com.xueyifang.cloud.user.service.UserRole;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        Long userId,
        String username,
        String studentId,
        String realName,
        String nickname,
        String phone,
        String email,
        String dormitory,
        String grade,
        Long professionalId,
        String professionalName,
        String avatar,
        String bio,
        String role,
        Integer roleCode,
        Integer publishPermission,
        Integer permissionReviewStatus,
        BigDecimal walletBalance,
        BigDecimal frozenAmount,
        Integer status,
        Integer accountStatus,
        LocalDateTime createTime,
        LocalDateTime updateTime) {

    public static UserProfileResponse from(UserAccount user) {
        UserRole role = UserRole.fromDatabaseValue(user.role());
        Integer publishPermission = user.publishPermission() != null ? user.publishPermission() : 0;
        Integer permissionReviewStatus = user.permissionReviewStatus() != null
                ? user.permissionReviewStatus()
                : (publishPermission == 1 ? 1 : 0);
        Integer accountStatus = user.accountStatus() != null
                ? user.accountStatus()
                : (Integer.valueOf(0).equals(user.status()) ? 2 : 1);

        return new UserProfileResponse(
                user.id(),
                user.id(),
                user.username(),
                firstNonBlank(user.studentId(), user.username()),
                user.realName(),
                user.nickname(),
                user.phone(),
                user.email(),
                user.dormitory(),
                user.grade(),
                user.professionalId(),
                null,
                user.avatar(),
                user.bio(),
                user.role(),
                role.code(),
                publishPermission,
                permissionReviewStatus,
                valueOrZero(user.walletBalance()),
                valueOrZero(user.frozenAmount()),
                user.status(),
                accountStatus,
                user.createTime(),
                user.updateTime());
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback;
    }

    private static BigDecimal valueOrZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
