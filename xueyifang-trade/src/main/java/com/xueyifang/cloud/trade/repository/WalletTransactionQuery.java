package com.xueyifang.cloud.trade.repository;

import java.time.LocalDateTime;

public record WalletTransactionQuery(
        Long userId,
        Integer transactionType,
        LocalDateTime startTime,
        LocalDateTime endTime,
        int offset,
        int limit) {
}
