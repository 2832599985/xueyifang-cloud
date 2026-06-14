package com.xueyifang.cloud.user.dto;

public record PermissionReviewResponse(
        Long userId,
        String status,
        Integer publishPermission,
        Integer permissionReviewStatus) {
}
