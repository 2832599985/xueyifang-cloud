package com.xueyifang.cloud.trade.dto;

import com.xueyifang.cloud.trade.repository.TradeDispute;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DisputeResponse(
        Long id,
        Long orderId,
        String orderNumber,
        Long serviceId,
        String serviceTitle,
        String serviceImage,
        BigDecimal totalAmount,
        Integer orderStatus,
        Integer paymentStatus,
        Integer refundStatus,
        Long complainantId,
        Long respondentId,
        Integer status,
        String reason,
        String evidence,
        String handleResult,
        String handleRemark,
        Long handlerId,
        LocalDateTime handleTime,
        Long buyerId,
        String buyerName,
        String buyerAvatar,
        Long sellerId,
        String sellerName,
        String sellerAvatar,
        LocalDateTime createTime,
        LocalDateTime updateTime) {

    public static DisputeResponse from(TradeDispute dispute) {
        return new DisputeResponse(
                dispute.id(),
                dispute.orderId(),
                dispute.orderNumber(),
                dispute.serviceId(),
                dispute.serviceTitle(),
                dispute.serviceImage(),
                dispute.totalAmount(),
                dispute.orderStatus(),
                dispute.paymentStatus(),
                dispute.refundStatus(),
                dispute.complainantId(),
                dispute.respondentId(),
                dispute.status(),
                dispute.reason(),
                dispute.evidence(),
                dispute.handleResult(),
                dispute.handleRemark(),
                dispute.handlerId(),
                dispute.handleTime(),
                dispute.buyerId(),
                dispute.buyerName(),
                dispute.buyerAvatar(),
                dispute.sellerId(),
                dispute.sellerName(),
                dispute.sellerAvatar(),
                dispute.createTime(),
                dispute.updateTime());
    }
}
