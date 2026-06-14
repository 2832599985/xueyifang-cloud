package com.xueyifang.cloud.system.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.system.dto.PageResponse;
import com.xueyifang.cloud.system.dto.TradeLocationRequest;
import com.xueyifang.cloud.system.dto.TradeLocationResponse;
import com.xueyifang.cloud.system.service.SystemDictionaryService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/trade-location")
public class AdminTradeLocationController {

    private final SystemDictionaryService systemDictionaryService;

    public AdminTradeLocationController(SystemDictionaryService systemDictionaryService) {
        this.systemDictionaryService = systemDictionaryService;
    }

    @GetMapping("/list")
    public BaseResponse<PageResponse<TradeLocationResponse>> listTradeLocations(
            @RequestParam(value = "isAvailable", required = false) Integer isAvailable,
            @RequestParam(value = "nameLike", required = false) String nameLike,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return ResultUtils.success(systemDictionaryService.pageTradeLocations(
                isAvailable,
                nameLike,
                firstNonNull(pageNum, current),
                pageSize,
                true));
    }

    @PostMapping("/add")
    public BaseResponse<Boolean> addTradeLocation(@RequestBody TradeLocationRequest request) {
        systemDictionaryService.createTradeLocation(request);
        return ResultUtils.success(true);
    }

    @PutMapping("/update")
    public BaseResponse<Boolean> updateTradeLocation(@RequestBody TradeLocationRequest request) {
        systemDictionaryService.updateTradeLocation(request);
        return ResultUtils.success(true);
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> deleteTradeLocation(@PathVariable Long id) {
        systemDictionaryService.deleteTradeLocation(id);
        return ResultUtils.success(true);
    }

    private Integer firstNonNull(Integer primary, Integer fallback) {
        return primary != null ? primary : fallback;
    }
}
