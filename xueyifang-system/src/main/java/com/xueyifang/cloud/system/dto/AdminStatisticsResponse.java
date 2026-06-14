package com.xueyifang.cloud.system.dto;

import java.math.BigDecimal;

public record AdminStatisticsResponse(
        Integer totalUsers,
        Integer activeUsers,
        Integer totalServices,
        Integer totalOrders,
        Integer completedOrders,
        Integer pendingDisputes,
        BigDecimal totalTransactionAmount,
        Integer todayNewUsers,
        Integer todayNewServices,
        Integer todayNewOrders) {
}
