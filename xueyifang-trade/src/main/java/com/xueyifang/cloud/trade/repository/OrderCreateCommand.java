package com.xueyifang.cloud.trade.repository;

import java.math.BigDecimal;

public record OrderCreateCommand(
        String orderNumber,
        Long serviceId,
        Long buyerId,
        Long sellerId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        Integer tradeType,
        Long tradeLocationId,
        Integer paymentStatus,
        Integer orderStatus,
        BigDecimal frozenAmount,
        Integer paymentMethod,
        Integer refundStatus,
        String remark) {
}
