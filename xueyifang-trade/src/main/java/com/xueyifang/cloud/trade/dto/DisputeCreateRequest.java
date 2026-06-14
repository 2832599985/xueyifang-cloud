package com.xueyifang.cloud.trade.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DisputeCreateRequest(
        @NotNull Long orderId,
        @Size(max = 500) String reason,
        @Size(max = 1000) String evidence,
        Integer disputeType,
        @Size(max = 500) String description) {

    public DisputeCreateRequest(Long orderId, String reason, String evidence) {
        this(orderId, reason, evidence, null, null);
    }

    public String effectiveReason() {
        if (reason != null && !reason.isBlank()) {
            return reason;
        }
        if (description != null && !description.isBlank()) {
            return description;
        }
        return null;
    }
}
