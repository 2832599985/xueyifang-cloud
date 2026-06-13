package com.xueyifang.cloud.trade.dto;

public record OrderPayRequest(
        Long orderId,
        Integer paymentMethod) {
}
