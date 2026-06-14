package com.xueyifang.cloud.trade.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecentSalesOrder(
        Long orderId,
        String orderNumber,
        BigDecimal totalAmount,
        LocalDateTime createTime) {
}
