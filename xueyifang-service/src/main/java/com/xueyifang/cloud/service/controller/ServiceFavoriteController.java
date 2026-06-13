package com.xueyifang.cloud.service.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.service.dto.FavoriteCollectRequest;
import com.xueyifang.cloud.service.dto.FavoriteListResponse;
import com.xueyifang.cloud.service.service.ServiceFavoriteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/favorite")
public class ServiceFavoriteController {

    private final ServiceFavoriteService serviceFavoriteService;

    public ServiceFavoriteController(ServiceFavoriteService serviceFavoriteService) {
        this.serviceFavoriteService = serviceFavoriteService;
    }

    @PostMapping("/collect")
    public BaseResponse<Void> collectService(@Valid @RequestBody FavoriteCollectRequest request) {
        serviceFavoriteService.collectService(request);
        return ResultUtils.success();
    }

    @DeleteMapping("/collect/{serviceId}")
    public BaseResponse<Void> uncollectService(@PathVariable Long serviceId) {
        serviceFavoriteService.uncollectService(serviceId);
        return ResultUtils.success();
    }

    @GetMapping("/myCollections")
    public BaseResponse<FavoriteListResponse> listMyCollections(
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return ResultUtils.success(serviceFavoriteService.listMyCollections(firstNonNull(pageNum, current), pageSize));
    }

    private Integer firstNonNull(Integer primary, Integer fallback) {
        return primary != null ? primary : fallback;
    }
}
