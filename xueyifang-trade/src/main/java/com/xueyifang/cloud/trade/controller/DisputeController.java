package com.xueyifang.cloud.trade.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.trade.dto.DisputeCreateRequest;
import com.xueyifang.cloud.trade.dto.DisputeHandleRequest;
import com.xueyifang.cloud.trade.dto.DisputeListResponse;
import com.xueyifang.cloud.trade.dto.DisputeResponse;
import com.xueyifang.cloud.trade.service.TradeDisputeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dispute")
public class DisputeController {

    private final TradeDisputeService tradeDisputeService;

    public DisputeController(TradeDisputeService tradeDisputeService) {
        this.tradeDisputeService = tradeDisputeService;
    }

    @PostMapping({"", "/create"})
    public BaseResponse<Long> createDispute(@Valid @RequestBody DisputeCreateRequest request) {
        return ResultUtils.success(tradeDisputeService.createDispute(request));
    }

    @GetMapping({"/my", "/myDisputes", "/list"})
    public BaseResponse<DisputeListResponse> listMyDisputes(
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "status", required = false) Integer status) {
        return ResultUtils.success(tradeDisputeService.listMyDisputes(
                firstNonNull(pageNum, current), pageSize, status));
    }

    @GetMapping("/admin/list")
    public BaseResponse<DisputeListResponse> listAdminDisputes(
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "status", required = false) Integer status) {
        return ResultUtils.success(tradeDisputeService.listAdminDisputes(
                firstNonNull(pageNum, current), pageSize, status));
    }

    @GetMapping("/{disputeId}")
    public BaseResponse<DisputeResponse> getDisputeDetail(@PathVariable Long disputeId) {
        return ResultUtils.success(tradeDisputeService.getDisputeDetail(disputeId));
    }

    @PostMapping({"/{disputeId}/handle", "/admin/{disputeId}/handle"})
    public BaseResponse<Void> handleDispute(@PathVariable Long disputeId,
                                            @Valid @RequestBody DisputeHandleRequest request) {
        tradeDisputeService.handleDispute(disputeId, request);
        return ResultUtils.success();
    }

    private Integer firstNonNull(Integer primary, Integer fallback) {
        return primary != null ? primary : fallback;
    }
}
