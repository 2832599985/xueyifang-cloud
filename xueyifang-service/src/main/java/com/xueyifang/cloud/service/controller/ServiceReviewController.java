package com.xueyifang.cloud.service.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.service.dto.ServiceReviewCreateRequest;
import com.xueyifang.cloud.service.dto.ServiceReviewListResponse;
import com.xueyifang.cloud.service.service.ServiceReviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/review")
public class ServiceReviewController {

    private final ServiceReviewService serviceReviewService;

    public ServiceReviewController(ServiceReviewService serviceReviewService) {
        this.serviceReviewService = serviceReviewService;
    }

    @PostMapping("/create")
    public BaseResponse<Long> createReview(@Valid @RequestBody ServiceReviewCreateRequest request) {
        return ResultUtils.success(serviceReviewService.createReview(request));
    }

    @GetMapping("/service/{serviceId}")
    public BaseResponse<ServiceReviewListResponse> listServiceReviews(
            @PathVariable Long serviceId,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return ResultUtils.success(serviceReviewService.listServiceReviews(
                serviceId,
                firstNonNull(pageNum, current),
                pageSize));
    }

    @GetMapping("/order/{orderId}/status")
    public BaseResponse<Boolean> isOrderReviewed(@PathVariable Long orderId) {
        return ResultUtils.success(serviceReviewService.isOrderReviewed(orderId));
    }

    private Integer firstNonNull(Integer primary, Integer fallback) {
        return primary != null ? primary : fallback;
    }
}
