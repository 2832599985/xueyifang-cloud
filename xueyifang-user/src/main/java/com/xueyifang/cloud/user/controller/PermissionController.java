package com.xueyifang.cloud.user.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.user.dto.PermissionApplyRequest;
import com.xueyifang.cloud.user.dto.PermissionApplyResponse;
import com.xueyifang.cloud.user.dto.PermissionStatusResponse;
import com.xueyifang.cloud.user.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/permission")
public class PermissionController {

    private final UserProfileService userProfileService;

    public PermissionController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/status")
    public BaseResponse<PermissionStatusResponse> status() {
        return ResultUtils.success(userProfileService.getPermissionStatus());
    }

    @PostMapping("/apply")
    public BaseResponse<PermissionApplyResponse> apply(@Valid @RequestBody PermissionApplyRequest request) {
        return ResultUtils.success(userProfileService.applyPermission(request));
    }
}
