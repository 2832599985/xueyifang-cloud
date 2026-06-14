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

import java.util.List;

@RestController
@RequestMapping("/trade-location")
public class TradeLocationController {

    private final SystemDictionaryService systemDictionaryService;

    public TradeLocationController(SystemDictionaryService systemDictionaryService) {
        this.systemDictionaryService = systemDictionaryService;
    }

    @GetMapping("/list")
    public BaseResponse<List<TradeLocationResponse>> listTradeLocations() {
        return ResultUtils.success(systemDictionaryService.listTradeLocations(true));
    }

    @GetMapping("/list/all")
    public BaseResponse<List<TradeLocationResponse>> listAllTradeLocations() {
        return ResultUtils.success(systemDictionaryService.listTradeLocations(false));
    }

    @GetMapping("/page")
    public BaseResponse<PageResponse<TradeLocationResponse>> pageTradeLocations(
            @RequestParam(value = "isAvailable", required = false) Integer isAvailable,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return ResultUtils.success(systemDictionaryService.pageTradeLocations(
                isAvailable,
                null,
                firstNonNull(pageNum, current),
                pageSize,
                false));
    }

    @GetMapping("/{id}")
    public BaseResponse<TradeLocationResponse> getTradeLocation(@PathVariable Long id) {
        return ResultUtils.success(systemDictionaryService.getTradeLocation(id));
    }

    @PostMapping("/add")
    public BaseResponse<Void> addTradeLocation(@RequestBody TradeLocationRequest request) {
        systemDictionaryService.createTradeLocation(request);
        return ResultUtils.success();
    }

    @PutMapping("/update")
    public BaseResponse<Void> updateTradeLocation(@RequestBody TradeLocationRequest request) {
        systemDictionaryService.updateTradeLocation(request);
        return ResultUtils.success();
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Void> deleteTradeLocation(@PathVariable Long id) {
        systemDictionaryService.deleteTradeLocation(id);
        return ResultUtils.success();
    }

    @DeleteMapping("/batch")
    public BaseResponse<Integer> deleteTradeLocations(@RequestBody List<Long> ids) {
        return ResultUtils.success(systemDictionaryService.deleteTradeLocations(ids));
    }

    private Integer firstNonNull(Integer primary, Integer fallback) {
        return primary != null ? primary : fallback;
    }
}
