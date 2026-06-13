package com.xueyifang.cloud.trade.dto;

import com.xueyifang.cloud.trade.repository.OrderPage;

import java.util.List;

public record OrderListResponse(
        List<OrderListItemResponse> records,
        long total,
        int size,
        int current,
        int pageNum,
        int pageSize,
        int pages) {

    public static OrderListResponse from(OrderPage page, int pageNum, int pageSize) {
        int pages = page.total() == 0 ? 0 : (int) Math.ceil((double) page.total() / pageSize);
        List<OrderListItemResponse> records = page.records().stream()
                .map(OrderListItemResponse::from)
                .toList();
        return new OrderListResponse(records, page.total(), pageSize, pageNum, pageNum, pageSize, pages);
    }
}
