package com.xueyifang.cloud.service.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.service.dto.AdminServiceReviewRequest;
import com.xueyifang.cloud.service.dto.PageResponse;
import com.xueyifang.cloud.service.dto.PendingServiceReviewResponse;
import com.xueyifang.cloud.service.dto.ServiceReviewDecisionResponse;
import com.xueyifang.cloud.service.service.ServiceCatalogService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/services")
public class AdminServiceReviewController {

    private final ServiceCatalogService serviceCatalogService;

    public AdminServiceReviewController(ServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    @GetMapping("/pending")
    public BaseResponse<PageResponse<PendingServiceReviewResponse>> listPendingServices(
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return ResultUtils.success(serviceCatalogService.listPendingReviewServices(pageNum, pageSize));
    }

    @PutMapping("/service/review")
    public BaseResponse<ServiceReviewDecisionResponse> reviewService(
            @Valid @RequestBody AdminServiceReviewRequest request) {
        return ResultUtils.success(serviceCatalogService.reviewService(request));
    }
}
