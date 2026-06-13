package com.xueyifang.cloud.trade.dto;

import com.xueyifang.cloud.trade.repository.TradeOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderListItemResponse(
        Long id,
        String orderNumber,
        Long serviceId,
        String serviceTitle,
        String serviceImage,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal totalAmount,
        Integer orderStatus,
        Integer paymentStatus,
        Long sellerId,
        String sellerName,
        String sellerAvatar,
        Long buyerId,
        String buyerName,
        String buyerAvatar,
        Integer refundStatus,
        String remark,
        LocalDateTime createTime) {

    public static OrderListItemResponse from(TradeOrder order) {
        return new OrderListItemResponse(
                order.id(),
                order.orderNumber(),
                order.serviceId(),
                order.serviceTitle(),
                order.serviceImage(),
                order.unitPrice(),
                order.quantity(),
                order.totalAmount(),
                order.orderStatus(),
                order.paymentStatus(),
                order.sellerId(),
                order.sellerName(),
                order.sellerAvatar(),
                order.buyerId(),
                order.buyerName(),
                order.buyerAvatar(),
                order.refundStatus(),
                order.remark(),
                order.createTime());
    }
}
