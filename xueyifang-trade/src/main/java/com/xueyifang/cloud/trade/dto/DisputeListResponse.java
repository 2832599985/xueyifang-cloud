package com.xueyifang.cloud.trade.dto;

import com.xueyifang.cloud.trade.repository.DisputePage;

import java.util.List;

public record DisputeListResponse(
        List<DisputeResponse> records,
        long total,
        int current,
        int pageNum,
        int pageSize,
        int pages) {

    public static DisputeListResponse from(DisputePage page, int pageNum, int pageSize) {
        int pages = page.total() == 0 ? 0 : (int) Math.ceil((double) page.total() / pageSize);
        List<DisputeResponse> records = page.records().stream()
                .map(DisputeResponse::from)
                .toList();
        return new DisputeListResponse(records, page.total(), pageNum, pageNum, pageSize, pages);
    }
}
