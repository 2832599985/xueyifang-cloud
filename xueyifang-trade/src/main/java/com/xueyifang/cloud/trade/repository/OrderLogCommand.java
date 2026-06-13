package com.xueyifang.cloud.trade.repository;

public record OrderLogCommand(
        Long orderId,
        Integer oldStatus,
        Integer newStatus,
        Long operatorId,
        Integer operatorRole,
        String actionType,
        String remark) {
}
