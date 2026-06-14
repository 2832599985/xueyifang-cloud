package com.xueyifang.cloud.trade.repository;

public record DisputeListQuery(
        Long userId,
        Integer status,
        boolean admin,
        int offset,
        int limit) {
}
