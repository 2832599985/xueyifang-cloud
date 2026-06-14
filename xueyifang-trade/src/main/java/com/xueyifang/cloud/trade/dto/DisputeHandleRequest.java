package com.xueyifang.cloud.trade.dto;

import jakarta.validation.constraints.Size;

public record DisputeHandleRequest(
        Boolean approveRefund,
        @Size(max = 500) String handleRemark,
        Integer actionType,
        @Size(max = 500) String adminReply,
        @Size(max = 500) String resolution,
        Boolean needRefund) {

    public DisputeHandleRequest(Boolean approveRefund, String handleRemark) {
        this(approveRefund, handleRemark, null, null, null, null);
    }

    public Boolean effectiveApproveRefund() {
        if (approveRefund != null) {
            return approveRefund;
        }
        if (actionType != null && actionType == 1) {
            return Boolean.TRUE.equals(needRefund);
        }
        if (actionType != null && actionType == 2) {
            return false;
        }
        return null;
    }

    public String effectiveHandleRemark() {
        if (handleRemark != null && !handleRemark.isBlank()) {
            return handleRemark;
        }
        if (resolution != null && !resolution.isBlank()) {
            return resolution;
        }
        if (adminReply != null && !adminReply.isBlank()) {
            return adminReply;
        }
        return null;
    }
}
