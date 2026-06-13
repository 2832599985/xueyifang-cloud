package com.xueyifang.cloud.trade.repository;

public record OrderListQuery(
        Long buyerId,
        Long sellerId,
        Integer orderStatus,
        int offset,
        int limit) {
}
