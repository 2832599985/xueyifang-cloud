package com.xueyifang.cloud.service.repository;

public record ReviewableOrder(
        Long orderId,
        Long serviceId,
        Long buyerId,
        Long sellerId,
        Integer orderStatus) {
}
