package com.xueyifang.cloud.service.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.service.dto.ServiceDetailResponse;
import com.xueyifang.cloud.service.dto.ServiceListResponse;
import com.xueyifang.cloud.service.dto.ServiceTagResponse;
import com.xueyifang.cloud.service.service.ServiceCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return ResultUtils.success(serviceCatalogService.listServices(
                keyword,
                tagId,
                categoryId,
                professionalId,
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

    private Integer firstNonNull(Integer primary, Integer fallback) {
        return primary != null ? primary : fallback;
    }
}
