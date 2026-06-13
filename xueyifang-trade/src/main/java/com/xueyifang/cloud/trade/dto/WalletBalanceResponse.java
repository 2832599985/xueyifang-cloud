package com.xueyifang.cloud.trade.dto;

import com.xueyifang.cloud.trade.repository.TradeUserWallet;

import java.math.BigDecimal;

public record WalletBalanceResponse(
        BigDecimal walletBalance,
        BigDecimal frozenAmount,
        BigDecimal totalAmount) {

    public static WalletBalanceResponse from(TradeUserWallet wallet) {
        BigDecimal walletBalance = wallet.walletBalance() == null ? BigDecimal.ZERO : wallet.walletBalance();
        BigDecimal frozenAmount = wallet.frozenAmount() == null ? BigDecimal.ZERO : wallet.frozenAmount();
        return new WalletBalanceResponse(walletBalance, frozenAmount, walletBalance.add(frozenAmount));
    }

    public static WalletBalanceResponse of(BigDecimal walletBalance, BigDecimal frozenAmount) {
        BigDecimal normalizedBalance = walletBalance == null ? BigDecimal.ZERO : walletBalance;
        BigDecimal normalizedFrozen = frozenAmount == null ? BigDecimal.ZERO : frozenAmount;
        return new WalletBalanceResponse(normalizedBalance, normalizedFrozen, normalizedBalance.add(normalizedFrozen));
    }
}
