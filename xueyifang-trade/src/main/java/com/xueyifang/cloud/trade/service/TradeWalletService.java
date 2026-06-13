package com.xueyifang.cloud.trade.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.trade.dto.WalletBalanceResponse;
import com.xueyifang.cloud.trade.dto.WalletRechargeRequest;
import com.xueyifang.cloud.trade.dto.WalletTransactionListResponse;
import com.xueyifang.cloud.trade.dto.WalletWithdrawRequest;
import com.xueyifang.cloud.trade.repository.TradeOrderRepository;
import com.xueyifang.cloud.trade.repository.TradeUserWallet;
import com.xueyifang.cloud.trade.repository.WalletTransactionCommand;
import com.xueyifang.cloud.trade.repository.WalletTransactionQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TradeWalletService {

    private static final int TRANSACTION_RECHARGE = 1;

    private static final int TRANSACTION_WITHDRAW = 2;

    private static final int MIN_TRANSACTION_TYPE = 1;

    private static final int MAX_TRANSACTION_TYPE = 7;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final int MAX_PAGE_SIZE = 100;

    private static final DateTimeFormatter TRANSACTION_NUMBER_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final TradeOrderRepository tradeOrderRepository;

    public TradeWalletService(TradeOrderRepository tradeOrderRepository) {
        this.tradeOrderRepository = tradeOrderRepository;
    }

    public WalletBalanceResponse getBalance() {
        LoginUserContext user = requireCurrentUser();
        TradeUserWallet wallet = tradeOrderRepository.findUserById(user.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        return WalletBalanceResponse.from(wallet);
    }

    public WalletTransactionListResponse listTransactions(Integer pageNum, Integer pageSize,
                                                          Integer transactionType,
                                                          LocalDateTime startTime,
                                                          LocalDateTime endTime) {
        LoginUserContext user = requireCurrentUser();
        int normalizedPageNum = normalizePageNum(pageNum);
        int normalizedPageSize = normalizePageSize(pageSize);
        Integer normalizedType = normalizeTransactionTypeOrNull(transactionType);
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "startTime must be before endTime");
        }
        return WalletTransactionListResponse.from(tradeOrderRepository.findWalletTransactions(
                        new WalletTransactionQuery(
                                user.userId(),
                                normalizedType,
                                startTime,
                                endTime,
                                (normalizedPageNum - 1) * normalizedPageSize,
                                normalizedPageSize)),
                normalizedPageNum,
                normalizedPageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public WalletBalanceResponse recharge(WalletRechargeRequest request) {
        LoginUserContext user = requireCurrentUser();
        BigDecimal amount = requirePositiveAmount(request == null ? null : request.amount());
        TradeUserWallet wallet = tradeOrderRepository.findUserForUpdate(user.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        BigDecimal balanceBefore = zeroIfNull(wallet.walletBalance());
        BigDecimal frozenBefore = zeroIfNull(wallet.frozenAmount());
        BigDecimal balanceAfter = balanceBefore.add(amount);

        if (!tradeOrderRepository.updateUserWallet(wallet.userId(), balanceAfter, frozenBefore)) {
            throw new BusinessException(ErrorCode.WALLET_RECHARGE_FAILED);
        }
        recordWalletTransaction(wallet.userId(), TRANSACTION_RECHARGE, amount,
                balanceBefore, balanceAfter, frozenBefore, frozenBefore, "用户充值");
        return WalletBalanceResponse.of(balanceAfter, frozenBefore);
    }

    @Transactional(rollbackFor = Exception.class)
    public WalletBalanceResponse withdraw(WalletWithdrawRequest request) {
        LoginUserContext user = requireCurrentUser();
        BigDecimal amount = requirePositiveAmount(request == null ? null : request.amount());
        TradeUserWallet wallet = tradeOrderRepository.findUserForUpdate(user.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        BigDecimal balanceBefore = zeroIfNull(wallet.walletBalance());
        BigDecimal frozenBefore = zeroIfNull(wallet.frozenAmount());
        if (balanceBefore.compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.WALLET_BALANCE_NOT_ENOUGH);
        }
        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        if (!tradeOrderRepository.updateUserWallet(wallet.userId(), balanceAfter, frozenBefore)) {
            throw new BusinessException(ErrorCode.WALLET_WITHDRAW_FAILED);
        }
        recordWalletTransaction(wallet.userId(), TRANSACTION_WITHDRAW, amount,
                balanceBefore, balanceAfter, frozenBefore, frozenBefore, "用户提现");
        return WalletBalanceResponse.of(balanceAfter, frozenBefore);
    }

    private void recordWalletTransaction(Long userId, Integer transactionType, BigDecimal amount,
                                         BigDecimal balanceBefore, BigDecimal balanceAfter,
                                         BigDecimal frozenBefore, BigDecimal frozenAfter,
                                         String remark) {
        tradeOrderRepository.insertWalletTransaction(new WalletTransactionCommand(
                userId,
                transactionType,
                amount,
                balanceBefore,
                balanceAfter,
                frozenBefore,
                frozenAfter,
                null,
                generateTransactionNumber(userId),
                remark));
    }

    private LoginUserContext requireCurrentUser() {
        return UserContextHolder.get()
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_LOGIN, "login required"));
    }

    private BigDecimal requirePositiveAmount(BigDecimal value) {
        if (value == null || value.signum() <= 0) {
            throw new BusinessException(ErrorCode.WALLET_AMOUNT_INVALID, "amount must be positive");
        }
        return value;
    }

    private Integer normalizeTransactionTypeOrNull(Integer transactionType) {
        if (transactionType == null) {
            return null;
        }
        if (transactionType >= MIN_TRANSACTION_TYPE && transactionType <= MAX_TRANSACTION_TYPE) {
            return transactionType;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "transactionType is invalid");
    }

    private int normalizePageNum(Integer pageNum) {
        if (pageNum == null) {
            return DEFAULT_PAGE_NUM;
        }
        if (pageNum < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "pageNum must be positive");
        }
        return pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null) {
            return DEFAULT_PAGE_SIZE;
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "pageSize must be between 1 and 100");
        }
        return pageSize;
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String generateTransactionNumber(Long userId) {
        int random = ThreadLocalRandom.current().nextInt(100, 1000);
        return "WT" + LocalDateTime.now().format(TRANSACTION_NUMBER_FORMAT) + userId + random;
    }
}
