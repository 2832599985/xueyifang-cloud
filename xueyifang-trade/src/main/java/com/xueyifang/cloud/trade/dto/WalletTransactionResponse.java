package com.xueyifang.cloud.trade.dto;

import com.xueyifang.cloud.trade.repository.WalletTransactionItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletTransactionResponse(
        Long transactionId,
        Integer transactionType,
        String typeName,
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

    public static WalletTransactionResponse from(WalletTransactionItem item) {
        return new WalletTransactionResponse(
                item.transactionId(),
                item.transactionType(),
                transactionTypeName(item.transactionType()),
                item.amount(),
                item.balanceBefore(),
                item.balanceAfter(),
                item.frozenBefore(),
                item.frozenAfter(),
                item.relatedOrderId(),
                item.relatedOrderNumber(),
                item.transactionNo(),
                item.remark(),
                item.createTime());
    }

    private static String transactionTypeName(Integer type) {
        return switch (type == null ? 0 : type) {
            case 1 -> "充值";
            case 2 -> "提现";
            case 3 -> "支付";
            case 4 -> "退款";
            case 5 -> "收入";
            case 6 -> "冻结";
            case 7 -> "解冻";
            default -> "未知";
        };
    }
}
