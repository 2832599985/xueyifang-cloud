package com.xueyifang.cloud.trade.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletTransactionItem(
        Long transactionId,
        Long userId,
        Integer transactionType,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        BigDecimal frozenBefore,
        BigDecimal frozenAfter,
        Long relatedOrderId,
        String relatedOrderNumber,
        String transactionNo,
        String remark,
        LocalDateTime createTime) {
}
