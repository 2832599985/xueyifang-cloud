package com.xueyifang.cloud.system.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.system.dto.PageResponse;
import com.xueyifang.cloud.system.dto.SysConfigResponse;
import com.xueyifang.cloud.system.dto.SysConfigUpdateRequest;
import com.xueyifang.cloud.system.service.SystemDictionaryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/sys-config")
public class AdminSysConfigController {

    private final SystemDictionaryService systemDictionaryService;

    public AdminSysConfigController(SystemDictionaryService systemDictionaryService) {
        this.systemDictionaryService = systemDictionaryService;
    }

    @GetMapping("/list")
    public BaseResponse<PageResponse<SysConfigResponse>> listSysConfigs(
            @RequestParam(value = "keyLike", required = false) String keyLike,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return ResultUtils.success(systemDictionaryService.pageSysConfigs(
                keyLike,
                firstNonNull(pageNum, current),
                pageSize));
    }

    @GetMapping("/{id}")
    public BaseResponse<SysConfigResponse> getSysConfig(@PathVariable Long id) {
        return ResultUtils.success(systemDictionaryService.getSysConfig(id));
    }

    @GetMapping("/key-values")
    public BaseResponse<Map<String, String>> getConfigValues(@RequestParam List<String> keys) {
        return ResultUtils.success(systemDictionaryService.getEnabledConfigValues(keys));
    }

    @PutMapping("/update")
    public BaseResponse<Boolean> updateSysConfig(@Valid @RequestBody SysConfigUpdateRequest request) {
        systemDictionaryService.updateSysConfig(request);
        return ResultUtils.success(true);
    }

    private Integer firstNonNull(Integer primary, Integer fallback) {
        return primary != null ? primary : fallback;
    }
}
