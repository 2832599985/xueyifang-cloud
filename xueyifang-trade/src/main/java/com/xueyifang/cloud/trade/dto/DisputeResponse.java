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
        LocalDateTime updateTime,
        Long disputeId,
        Long disputeInitiatorId,
        Integer disputeType,
        String description,
        Integer disputeStatus,
        String adminReply,
        String resolution) {

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
                dispute.updateTime(),
                dispute.id(),
                dispute.complainantId(),
                4,
                dispute.reason(),
                toLegacyStatus(dispute.status()),
                dispute.handleRemark(),
                toLegacyResolution(dispute.handleResult()));
    }

    private static Integer toLegacyStatus(Integer status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case 2 -> 3;
            case 3 -> 4;
            default -> status;
        };
    }

    private static String toLegacyResolution(String handleResult) {
        if (handleResult == null || handleResult.isBlank()) {
            return null;
        }
        return switch (handleResult) {
            case "REFUND_APPROVED" -> "管理员支持买家退款";
            case "REJECTED" -> "管理员驳回纠纷";
            default -> handleResult;
        };
    }
}
