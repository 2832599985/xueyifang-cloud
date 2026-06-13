package com.xueyifang.cloud.user.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.user.dto.ChangePasswordRequest;
import com.xueyifang.cloud.user.dto.UpdateProfileRequest;
import com.xueyifang.cloud.user.dto.UserProfileResponse;
import com.xueyifang.cloud.user.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    public BaseResponse<UserProfileResponse> getCurrentUser() {
        return ResultUtils.success(userProfileService.getCurrentUser());
    }

    @PutMapping("/me/profile")
    public BaseResponse<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        userProfileService.updateCurrentUserProfile(request);
        return ResultUtils.success("个人信息更新成功");
    }

    @PutMapping("/me/password")
    public BaseResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userProfileService.changeCurrentUserPassword(request);
        return ResultUtils.success("密码修改成功");
    }
}
