package com.xueyifang.cloud.service.dto;

public record ServiceReviewDecisionResponse(
        Long serviceId,
        String status,
        Integer serviceStatus,
        Integer reviewStatus) {
}
