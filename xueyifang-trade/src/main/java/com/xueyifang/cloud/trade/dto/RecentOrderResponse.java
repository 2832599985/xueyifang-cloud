package com.xueyifang.cloud.trade.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecentOrderResponse(
        Long orderId,
        String orderNumber,
        BigDecimal totalAmount,
        LocalDateTime createTime) {
}
