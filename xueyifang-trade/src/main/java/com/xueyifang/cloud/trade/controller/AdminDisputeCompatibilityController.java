package com.xueyifang.cloud.trade.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
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
@RequestMapping("/admin/dispute")
public class AdminDisputeCompatibilityController {

    private final TradeDisputeService tradeDisputeService;

    public AdminDisputeCompatibilityController(TradeDisputeService tradeDisputeService) {
        this.tradeDisputeService = tradeDisputeService;
    }

    @GetMapping("/list")
    public BaseResponse<DisputeListResponse> listAdminDisputes(
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "disputeStatus", required = false) Integer disputeStatus) {
        return ResultUtils.success(tradeDisputeService.listAdminDisputes(
                firstNonNull(pageNum, current), pageSize, toCurrentStatus(firstNonNull(disputeStatus, status))));
    }

    @GetMapping("/by-order/{orderId}")
    public BaseResponse<DisputeResponse> getDisputeByOrderId(@PathVariable Long orderId) {
        return ResultUtils.success(tradeDisputeService.getDisputeDetailByOrderIdForAdmin(orderId));
    }

    @GetMapping("/{disputeId}")
    public BaseResponse<DisputeResponse> getAdminDisputeDetail(@PathVariable Long disputeId) {
        return ResultUtils.success(tradeDisputeService.getDisputeDetail(disputeId));
    }

    @PostMapping("/{disputeId}/handle")
    public BaseResponse<Void> handleDispute(@PathVariable Long disputeId,
                                            @Valid @RequestBody DisputeHandleRequest request) {
        tradeDisputeService.handleDispute(disputeId, request);
        return ResultUtils.success();
    }

    private Integer firstNonNull(Integer primary, Integer fallback) {
        return primary != null ? primary : fallback;
    }

    private Integer toCurrentStatus(Integer legacyStatus) {
        if (legacyStatus == null) {
            return null;
        }
        return switch (legacyStatus) {
            case 2 -> 1;
            case 3 -> 2;
            case 4 -> 3;
            default -> legacyStatus;
        };
    }
}
