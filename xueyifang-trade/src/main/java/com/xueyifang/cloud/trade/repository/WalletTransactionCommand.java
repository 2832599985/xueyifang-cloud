package com.xueyifang.cloud.trade.repository;

import java.math.BigDecimal;

public record WalletTransactionCommand(
        Long userId,
        Integer transactionType,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        BigDecimal frozenBefore,
        BigDecimal frozenAfter,
        Long relatedOrderId,
        String transactionNo,
        String remark) {
}
