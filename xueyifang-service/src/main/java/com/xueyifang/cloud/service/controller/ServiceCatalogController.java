package com.xueyifang.cloud.service.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.service.dto.ServiceDetailResponse;
import com.xueyifang.cloud.service.dto.ServiceListResponse;
import com.xueyifang.cloud.service.dto.ServicePublishRequest;
import com.xueyifang.cloud.service.dto.ServiceTagResponse;
import com.xueyifang.cloud.service.dto.ServiceUpdateRequest;
import com.xueyifang.cloud.service.service.ServiceCatalogService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/service")
public class ServiceCatalogController {

    private final ServiceCatalogService serviceCatalogService;

    public ServiceCatalogController(ServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    @GetMapping("/list")
    public BaseResponse<ServiceListResponse> listServices(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "tagId", required = false) Long tagId,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "professionalId", required = false) Long professionalId,
            @RequestParam(value = "publisherId", required = false) Long publisherId,
            @RequestParam(value = "sellerId", required = false) Long sellerId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return ResultUtils.success(serviceCatalogService.listServices(
                keyword,
                tagId,
                categoryId,
                professionalId,
                firstNonNull(publisherId, sellerId),
                status,
                firstNonNull(pageNum, current),
                pageSize));
    }

    @GetMapping("/myServices")
    public BaseResponse<ServiceListResponse> listMyServices(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return ResultUtils.success(serviceCatalogService.listMyServices(
                status,
                firstNonNull(pageNum, current),
                pageSize));
    }

    @GetMapping("/{serviceId}")
    public BaseResponse<ServiceDetailResponse> getServiceDetail(@PathVariable Long serviceId) {
        return ResultUtils.success(serviceCatalogService.getServiceDetail(serviceId));
    }

    @GetMapping("/tags")
    public BaseResponse<List<ServiceTagResponse>> listTags() {
        return ResultUtils.success(serviceCatalogService.listTags());
    }

    @PostMapping("/publish")
    public BaseResponse<Long> publishService(@Valid @RequestBody ServicePublishRequest request) {
        return ResultUtils.success(serviceCatalogService.publishService(request));
    }

    @PutMapping("/{serviceId}")
    public BaseResponse<Void> updateService(@PathVariable Long serviceId,
                                            @Valid @RequestBody ServiceUpdateRequest request) {
        serviceCatalogService.updateService(serviceId, request);
        return ResultUtils.success();
    }

    @PutMapping("/{serviceId}/offline")
    public BaseResponse<Void> offlineService(@PathVariable Long serviceId) {
        serviceCatalogService.offlineService(serviceId);
        return ResultUtils.success();
    }

    @PutMapping("/{serviceId}/online")
    public BaseResponse<Void> onlineService(@PathVariable Long serviceId) {
        serviceCatalogService.onlineService(serviceId);
        return ResultUtils.success();
    }

    @DeleteMapping("/{serviceId}")
    public BaseResponse<Void> deleteService(@PathVariable Long serviceId) {
        serviceCatalogService.deleteService(serviceId);
        return ResultUtils.success();
    }

    private Integer firstNonNull(Integer primary, Integer fallback) {
        return primary != null ? primary : fallback;
    }

    private Long firstNonNull(Long primary, Long fallback) {
        return primary != null ? primary : fallback;
    }
}
