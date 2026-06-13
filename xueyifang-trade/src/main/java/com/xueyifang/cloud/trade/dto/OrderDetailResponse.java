package com.xueyifang.cloud.trade.dto;

import com.xueyifang.cloud.trade.repository.TradeOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderDetailResponse(
        Long id,
        String orderNumber,
        Long serviceId,
        String serviceTitle,
        String serviceDescription,
        String serviceImage,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal totalAmount,
        Integer tradeType,
        Long tradeLocationId,
        String tradeLocationName,
        Integer orderStatus,
        Integer paymentStatus,
        Integer paymentMethod,
        BigDecimal frozenAmount,
        Long buyerId,
        String buyerName,
        String buyerAvatar,
        String buyerProfessional,
        Long sellerId,
        String sellerName,
        String sellerAvatar,
        String sellerProfessional,
        LocalDateTime paymentTime,
        LocalDateTime sellerShipTime,
        LocalDateTime buyerConfirmTime,
        Integer refundStatus,
        String refundReason,
        LocalDateTime refundRequestTime,
        LocalDateTime createTime,
        String remark,
        Boolean isReviewed) {

    public static OrderDetailResponse from(TradeOrder order) {
        return new OrderDetailResponse(
                order.id(),
                order.orderNumber(),
                order.serviceId(),
                order.serviceTitle(),
                order.serviceDescription(),
                order.serviceImage(),
                order.unitPrice(),
                order.quantity(),
                order.totalAmount(),
                order.tradeType(),
                order.tradeLocationId(),
                null,
                order.orderStatus(),
                order.paymentStatus(),
                order.paymentMethod(),
                order.frozenAmount(),
                order.buyerId(),
                order.buyerName(),
                order.buyerAvatar(),
                null,
                order.sellerId(),
                order.sellerName(),
                order.sellerAvatar(),
                null,
                order.paymentTime(),
                order.sellerShipTime(),
                order.buyerConfirmTime(),
                order.refundStatus(),
                order.refundReason(),
                order.refundRequestTime(),
                order.createTime(),
                order.remark(),
                order.isReviewed());
    }
}
