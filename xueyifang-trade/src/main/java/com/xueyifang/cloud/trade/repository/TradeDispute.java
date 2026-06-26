package com.xueyifang.cloud.trade.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeDispute(
        Long id,
        Long orderId,
        Long complainantId,
        Long respondentId,
        Integer status,
        String reason,
        String evidence,
        String handleResult,
        String handleRemark,
        Long handlerId,
        LocalDateTime handleTime,
        LocalDateTime createTime,
        LocalDateTime updateTime,
        String orderNumber,
        Long serviceId,
        String serviceTitle,
        String serviceImage,
        BigDecimal totalAmount,
        Integer orderStatus,
        Integer paymentStatus,
        Integer refundStatus,
        Long buyerId,
        String buyerName,
        String buyerAvatar,
        Long sellerId,
        String sellerName,
        String sellerAvatar,
        Integer disputeType) {
}
