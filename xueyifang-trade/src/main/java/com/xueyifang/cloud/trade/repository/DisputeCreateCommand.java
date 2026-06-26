package com.xueyifang.cloud.trade.repository;

public record DisputeCreateCommand(
        Long orderId,
        Long complainantId,
        Long respondentId,
        String reason,
        String evidence,
        Integer disputeType) {
}
