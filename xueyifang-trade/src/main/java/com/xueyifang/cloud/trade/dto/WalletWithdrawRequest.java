package com.xueyifang.cloud.trade.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WalletWithdrawRequest(
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        String accountNumber,
        String accountName) {
}
