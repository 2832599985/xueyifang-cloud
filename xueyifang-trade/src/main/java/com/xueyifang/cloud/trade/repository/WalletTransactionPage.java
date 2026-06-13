package com.xueyifang.cloud.trade.repository;

import java.util.List;

public record WalletTransactionPage(
        List<WalletTransactionItem> records,
        long total) {
}
