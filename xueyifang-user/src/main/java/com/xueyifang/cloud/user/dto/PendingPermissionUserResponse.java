package com.xueyifang.cloud.user.dto;

import com.xueyifang.cloud.user.repository.UserAccount;

import java.time.LocalDateTime;

public record PendingPermissionUserResponse(
        Long userId,
        String username,
        String studentId,
        String realName,
        String nickname,
        String phone,
        String email,
        String grade,
        Long professionalId,
        Integer permissionReviewStatus,
        String permissionApplyReason,
        LocalDateTime applyTime,
        LocalDateTime createTime) {

    public static PendingPermissionUserResponse from(UserAccount user) {
        return new PendingPermissionUserResponse(
                user.id(),
                user.username(),
                user.studentId(),
                user.realName(),
                user.nickname(),
                user.phone(),
                user.email(),
                user.grade(),
                user.professionalId(),
                user.permissionReviewStatus(),
                user.permissionApplyReason(),
                user.updateTime(),
                user.createTime());
    }
}
