package com.xueyifang.cloud.trade.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.trade.dto.OrderCreateRequest;
import com.xueyifang.cloud.trade.dto.OrderDetailResponse;
import com.xueyifang.cloud.trade.dto.OrderListResponse;
import com.xueyifang.cloud.trade.dto.OrderPayRequest;
import com.xueyifang.cloud.trade.dto.OrderRefundRequest;
import com.xueyifang.cloud.trade.dto.SellerHandleRefundRequest;
import com.xueyifang.cloud.trade.service.TradeOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final TradeOrderService tradeOrderService;

    public OrderController(TradeOrderService tradeOrderService) {
        this.tradeOrderService = tradeOrderService;
    }

    @PostMapping("/create")
    public BaseResponse<Long> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        return ResultUtils.success(tradeOrderService.createOrder(request));
    }

    @PostMapping("/{orderId}/pay")
    public BaseResponse<Void> payOrder(@PathVariable Long orderId,
                                       @RequestBody(required = false) OrderPayRequest request) {
        tradeOrderService.payOrder(orderId, request);
        return ResultUtils.success();
    }

    @PostMapping("/{orderId}/cancel")
    public BaseResponse<Void> cancelOrder(@PathVariable Long orderId) {
        tradeOrderService.cancelOrder(orderId);
        return ResultUtils.success();
    }

    @PostMapping("/{orderId}/ship")
    public BaseResponse<Void> shipOrder(@PathVariable Long orderId) {
        tradeOrderService.shipOrder(orderId);
        return ResultUtils.success();
    }

    @PostMapping("/{orderId}/confirm")
    public BaseResponse<Void> confirmOrder(@PathVariable Long orderId) {
        tradeOrderService.confirmOrder(orderId);
        return ResultUtils.success();
    }

    @PostMapping("/{orderId}/refund")
    public BaseResponse<Void> requestRefund(@PathVariable Long orderId,
                                            @Valid @RequestBody OrderRefundRequest request) {
        tradeOrderService.requestRefund(orderId, request);
        return ResultUtils.success();
    }

    @PostMapping("/{orderId}/handleRefund")
    public BaseResponse<Void> sellerHandleRefund(@PathVariable Long orderId,
                                                 @Valid @RequestBody SellerHandleRefundRequest request) {
        tradeOrderService.sellerHandleRefund(orderId, request);
        return ResultUtils.success();
    }

    @GetMapping("/myOrders")
    public BaseResponse<OrderListResponse> listMyOrders(
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "orderStatus", required = false) Integer orderStatus) {
        return ResultUtils.success(tradeOrderService.listMyOrders(firstNonNull(pageNum, current), pageSize, orderStatus));
    }

    @GetMapping("/mySellingOrders")
    public BaseResponse<OrderListResponse> listMySellingOrders(
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "orderStatus", required = false) Integer orderStatus) {
        return ResultUtils.success(
                tradeOrderService.listMySellingOrders(firstNonNull(pageNum, current), pageSize, orderStatus));
    }

    @GetMapping("/{orderId}")
    public BaseResponse<OrderDetailResponse> getOrderDetail(@PathVariable Long orderId) {
        return ResultUtils.success(tradeOrderService.getOrderDetail(orderId));
    }

    @PostMapping("/{orderId}/sellerConfirm")
    public BaseResponse<Void> sellerConfirmOrder(@PathVariable Long orderId) {
        tradeOrderService.shipOrder(orderId);
        return ResultUtils.success();
    }

    private Integer firstNonNull(Integer primary, Integer fallback) {
        return primary != null ? primary : fallback;
    }
}
