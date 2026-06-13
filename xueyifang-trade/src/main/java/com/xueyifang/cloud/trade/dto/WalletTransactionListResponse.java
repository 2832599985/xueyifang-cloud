package com.xueyifang.cloud.trade.dto;

import com.xueyifang.cloud.trade.repository.WalletTransactionPage;

import java.util.List;

public record WalletTransactionListResponse(
        List<WalletTransactionResponse> records,
        long total,
        int size,
        int current,
        int pageNum,
        int pageSize,
        int pages) {

    public static WalletTransactionListResponse from(WalletTransactionPage page, int pageNum, int pageSize) {
        int pages = page.total() == 0 ? 0 : (int) Math.ceil((double) page.total() / pageSize);
        List<WalletTransactionResponse> records = page.records().stream()
                .map(WalletTransactionResponse::from)
                .toList();
        return new WalletTransactionListResponse(records, page.total(), pageSize, pageNum, pageNum, pageSize, pages);
    }
}
