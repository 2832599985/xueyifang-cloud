package com.xueyifang.cloud.trade.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeOrder(
        Long id,
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
        LocalDateTime paymentTime,
        LocalDateTime sellerShipTime,
        LocalDateTime buyerConfirmTime,
        Integer refundStatus,
        String refundReason,
        LocalDateTime refundRequestTime,
        String remark,
        LocalDateTime createTime,
        LocalDateTime updateTime,
        String serviceTitle,
        String serviceDescription,
        String serviceImage,
        String buyerName,
        String buyerAvatar,
        String sellerName,
        String sellerAvatar,
        Boolean isReviewed) {
}
