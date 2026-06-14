package com.xueyifang.cloud.user.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.user.dto.AdminPermissionReviewRequest;
import com.xueyifang.cloud.user.dto.PageResponse;
import com.xueyifang.cloud.user.dto.PendingPermissionUserResponse;
import com.xueyifang.cloud.user.dto.PermissionReviewResponse;
import com.xueyifang.cloud.user.service.AdminPermissionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminPermissionController {

    private final AdminPermissionService adminPermissionService;

    public AdminPermissionController(AdminPermissionService adminPermissionService) {
        this.adminPermissionService = adminPermissionService;
    }

    @GetMapping("/admin/users/pending")
    public BaseResponse<PageResponse<PendingPermissionUserResponse>> listPendingUsers(
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return ResultUtils.success(adminPermissionService.listPendingUsers(pageNum, pageSize));
    }

    @PutMapping("/admin/permission/review")
    public BaseResponse<PermissionReviewResponse> reviewPermission(
            @Valid @RequestBody AdminPermissionReviewRequest request) {
        return ResultUtils.success(adminPermissionService.reviewPermission(request));
    }
}
