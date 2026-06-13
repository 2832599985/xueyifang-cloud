package com.xueyifang.cloud.trade.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.trade.dto.WalletBalanceResponse;
import com.xueyifang.cloud.trade.dto.WalletRechargeRequest;
import com.xueyifang.cloud.trade.dto.WalletTransactionListResponse;
import com.xueyifang.cloud.trade.dto.WalletWithdrawRequest;
import com.xueyifang.cloud.trade.service.TradeWalletService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final TradeWalletService tradeWalletService;

    public WalletController(TradeWalletService tradeWalletService) {
        this.tradeWalletService = tradeWalletService;
    }

    @GetMapping("/balance")
    public BaseResponse<WalletBalanceResponse> getBalance() {
        return ResultUtils.success(tradeWalletService.getBalance());
    }

    @GetMapping("/transactions")
    public BaseResponse<WalletTransactionListResponse> listTransactions(
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "transactionType", required = false) Integer transactionType,
            @RequestParam(value = "startTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResultUtils.success(tradeWalletService.listTransactions(
                firstNonNull(pageNum, current),
                pageSize,
                transactionType,
                startTime,
                endTime));
    }

    @PostMapping("/recharge")
    public BaseResponse<WalletBalanceResponse> recharge(@Valid @RequestBody WalletRechargeRequest request) {
        return ResultUtils.success(tradeWalletService.recharge(request));
    }

    @PostMapping("/withdraw")
    public BaseResponse<WalletBalanceResponse> withdraw(@Valid @RequestBody WalletWithdrawRequest request) {
        return ResultUtils.success(tradeWalletService.withdraw(request));
    }

    private Integer firstNonNull(Integer primary, Integer fallback) {
        return primary != null ? primary : fallback;
    }
}
