package com.xueyifang.cloud.trade.repository;

import java.math.BigDecimal;

public record TradeUserWallet(
        Long userId,
        Integer role,
        String displayName,
        String avatar,
        BigDecimal walletBalance,
        BigDecimal frozenAmount) {
}
