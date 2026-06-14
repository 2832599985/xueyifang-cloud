package com.xueyifang.cloud.system.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.system.dto.SysConfigRegisterStatusResponse;
import com.xueyifang.cloud.system.service.SystemDictionaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys-config")
public class SysConfigController {

    private final SystemDictionaryService systemDictionaryService;

    public SysConfigController(SystemDictionaryService systemDictionaryService) {
        this.systemDictionaryService = systemDictionaryService;
    }

    @GetMapping("/register-status")
    public BaseResponse<SysConfigRegisterStatusResponse> getRegisterStatus() {
        return ResultUtils.success(systemDictionaryService.getRegisterStatus());
    }
}
