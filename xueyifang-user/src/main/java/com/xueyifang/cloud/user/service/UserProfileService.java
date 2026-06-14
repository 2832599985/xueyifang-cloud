package com.xueyifang.cloud.user.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.user.dto.ChangePasswordRequest;
import com.xueyifang.cloud.user.dto.PermissionApplyRequest;
import com.xueyifang.cloud.user.dto.PermissionApplyResponse;
import com.xueyifang.cloud.user.dto.PermissionStatusResponse;
import com.xueyifang.cloud.user.dto.UpdateProfileRequest;
import com.xueyifang.cloud.user.dto.UserProfileResponse;
import com.xueyifang.cloud.user.repository.UserAccount;
import com.xueyifang.cloud.user.repository.UserAccountRepository;
import com.xueyifang.cloud.user.repository.UserProfileUpdateCommand;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private static final int NO_PUBLISH_PERMISSION = 0;

    private static final int HAS_PUBLISH_PERMISSION = 1;

    private static final int PERMISSION_PENDING = 0;

    private static final int PERMISSION_APPROVED = 1;

    private static final int PERMISSION_REJECTED = 2;

    private final UserAccountRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserProfileService(UserAccountRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserProfileResponse getCurrentUser() {
        return UserProfileResponse.from(getCurrentUserAccount());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCurrentUserProfile(UpdateProfileRequest request) {
        Long userId = requireCurrentUserId();
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "request must not be null");
        }

        UserProfileUpdateCommand command = toCommand(request);
        validateUpdateCommand(command, userId);

        boolean updated = userRepository.updateProfile(userId, command);
        if (!updated) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void changeCurrentUserPassword(ChangePasswordRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "request must not be null");
        }

        UserAccount user = getCurrentUserAccount();
        if (!passwordEncoder.matches(request.oldPassword(), user.password())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "old password is incorrect");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "new password confirmation does not match");
        }
        if (request.newPassword().equals(request.oldPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "new password must be different");
        }

        boolean updated = userRepository.updatePassword(user.id(), passwordEncoder.encode(request.newPassword()));
        if (!updated) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "password update failed");
        }
    }

    public PermissionStatusResponse getPermissionStatus() {
        return buildPermissionStatus(getCurrentUserAccount());
    }

    @Transactional(rollbackFor = Exception.class)
    public PermissionApplyResponse applyPermission(PermissionApplyRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "request must not be null");
        }
        UserAccount user = getCurrentUserAccount();
        if (effectivePublishPermission(user) == HAS_PUBLISH_PERMISSION
                || effectivePermissionReviewStatus(user) == PERMISSION_APPROVED) {
            return new PermissionApplyResponse("approved", "已拥有发布权限，无需申请");
        }
        if (effectivePermissionReviewStatus(user) == PERMISSION_PENDING) {
            throw new BusinessException(ErrorCode.PERMISSION_ALREADY_APPLIED, "已提交申请，正在审核中");
        }

        boolean updated = userRepository.updatePermissionApplication(
                user.id(), NO_PUBLISH_PERMISSION, PERMISSION_PENDING, normalizeOptional(request.reason()));
        if (!updated) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "permission application failed");
        }
        return new PermissionApplyResponse("pending", "申请已提交");
    }

    private PermissionStatusResponse buildPermissionStatus(UserAccount user) {
        int publishPermission = effectivePublishPermission(user);
        int reviewStatus = effectivePermissionReviewStatus(user);
        boolean hasPermission = publishPermission == HAS_PUBLISH_PERMISSION || reviewStatus == PERMISSION_APPROVED;

        String status;
        String statusText;
        if (hasPermission) {
            status = "approved";
            statusText = "已批准";
        } else if (reviewStatus == PERMISSION_REJECTED) {
            status = "rejected";
            statusText = "已拒绝";
        } else if (reviewStatus == PERMISSION_PENDING) {
            status = "pending";
            statusText = "审核中";
        } else {
            status = "unknown";
            statusText = "未知";
        }

        return new PermissionStatusResponse(
                user.id(),
                firstNonBlank(user.studentId(), user.username()),
                hasPermission,
                publishPermission,
                reviewStatus,
                status,
                statusText,
                status,
                user.permissionReviewReason(),
                user.permissionReviewedAt());
    }

    private UserAccount getCurrentUserAccount() {
        Long userId = requireCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
    }

    private Long requireCurrentUserId() {
        return UserContextHolder.currentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_LOGIN, "请先登录"));
    }

    private UserProfileUpdateCommand toCommand(UpdateProfileRequest request) {
        Long professionalId = request.professionalId();
        if (professionalId != null && professionalId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "professionalId must be positive");
        }

        return new UserProfileUpdateCommand(
                normalizeOptional(request.realName()),
                normalizeOptional(request.nickname()),
                normalizeOptional(request.phone()),
                normalizeOptional(request.email()),
                normalizeOptional(request.dormitory()),
                normalizeOptional(request.grade()),
                professionalId,
                normalizeOptional(request.avatar()),
                normalizeOptional(request.bio()));
    }

    private void validateUpdateCommand(UserProfileUpdateCommand command, Long userId) {
        if (command.nickname() != null && command.nickname().length() < 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "nickname length must be at least 2");
        }
        if (command.email() != null && userRepository.existsByEmailExcludingUser(command.email(), userId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "email already exists");
        }
        if (command.phone() != null && userRepository.existsByPhoneExcludingUser(command.phone(), userId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "phone already exists");
        }
    }

    private int effectivePublishPermission(UserAccount user) {
        return user.publishPermission() != null ? user.publishPermission() : NO_PUBLISH_PERMISSION;
    }

    private int effectivePermissionReviewStatus(UserAccount user) {
        if (user.permissionReviewStatus() != null) {
            return user.permissionReviewStatus();
        }
        return effectivePublishPermission(user) == HAS_PUBLISH_PERMISSION ? PERMISSION_APPROVED : PERMISSION_PENDING;
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback;
    }
}
