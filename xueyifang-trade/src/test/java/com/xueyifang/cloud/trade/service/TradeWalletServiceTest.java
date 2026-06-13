package com.xueyifang.cloud.trade.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.trade.dto.WalletBalanceResponse;
import com.xueyifang.cloud.trade.dto.WalletRechargeRequest;
import com.xueyifang.cloud.trade.dto.WalletTransactionListResponse;
import com.xueyifang.cloud.trade.dto.WalletWithdrawRequest;
import com.xueyifang.cloud.trade.repository.TradeUserWallet;
import com.xueyifang.cloud.trade.support.InMemoryTradeOrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TradeWalletServiceTest {

    private final InMemoryTradeOrderRepository repository = new InMemoryTradeOrderRepository();

    private final TradeWalletService tradeWalletService = new TradeWalletService(repository);

    @BeforeEach
    void setUp() {
        repository.putUser(new TradeUserWallet(
                20L,
                1,
                "Buyer",
                "buyer.jpg",
                new BigDecimal("100.00"),
                new BigDecimal("8.00")));
        UserContextHolder.set(new LoginUserContext(20L, 1, 1));
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void getsWalletBalance() {
        WalletBalanceResponse response = tradeWalletService.getBalance();

        assertThat(response.walletBalance()).isEqualByComparingTo("100.00");
        assertThat(response.frozenAmount()).isEqualByComparingTo("8.00");
        assertThat(response.totalAmount()).isEqualByComparingTo("108.00");
    }

    @Test
    void rechargesWithdrawsAndListsTransactions() {
        WalletBalanceResponse afterRecharge = tradeWalletService.recharge(new WalletRechargeRequest(
                new BigDecimal("15.00"), "mock"));
        assertThat(afterRecharge.walletBalance()).isEqualByComparingTo("115.00");
        assertThat(afterRecharge.frozenAmount()).isEqualByComparingTo("8.00");

        WalletBalanceResponse afterWithdraw = tradeWalletService.withdraw(new WalletWithdrawRequest(
                new BigDecimal("20.00"), "acct", "Buyer"));
        assertThat(afterWithdraw.walletBalance()).isEqualByComparingTo("95.00");
        assertThat(afterWithdraw.totalAmount()).isEqualByComparingTo("103.00");

        WalletTransactionListResponse all = tradeWalletService.listTransactions(1, 10, null, null, null);
        assertThat(all.total()).isEqualTo(2);
        assertThat(all.records()).extracting("transactionType").containsExactly(2, 1);

        WalletTransactionListResponse rechargeOnly = tradeWalletService.listTransactions(1, 10, 1, null, null);
        assertThat(rechargeOnly.total()).isEqualTo(1);
        assertThat(rechargeOnly.records().getFirst().typeName()).isEqualTo("充值");
    }

    @Test
    void rejectsWithdrawWhenBalanceIsInsufficient() {
        assertThatThrownBy(() -> tradeWalletService.withdraw(new WalletWithdrawRequest(
                new BigDecimal("101.00"), null, null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.WALLET_BALANCE_NOT_ENOUGH.getCode()));
    }
}
