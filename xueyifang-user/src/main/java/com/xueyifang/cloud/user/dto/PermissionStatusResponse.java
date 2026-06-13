package com.xueyifang.cloud.user.dto;

public record PermissionStatusResponse(
        Long userId,
        String studentId,
        Boolean hasPermission,
        Integer publishPermission,
        Integer permissionReviewStatus,
        String status,
        String statusText,
        String lastReviewResult,
        String lastReviewReason,
        java.time.LocalDateTime reviewedAt) {
}
