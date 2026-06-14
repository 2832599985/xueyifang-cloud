package com.xueyifang.cloud.user.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.user.dto.AdminPermissionReviewRequest;
import com.xueyifang.cloud.user.dto.PageResponse;
import com.xueyifang.cloud.user.dto.PendingPermissionUserResponse;
import com.xueyifang.cloud.user.dto.PermissionReviewResponse;
import com.xueyifang.cloud.user.notification.UserNotificationPublisher;
import com.xueyifang.cloud.user.repository.UserAccount;
import com.xueyifang.cloud.user.repository.UserAccountPage;
import com.xueyifang.cloud.user.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminPermissionService {

    private static final int NO_PUBLISH_PERMISSION = 0;

    private static final int HAS_PUBLISH_PERMISSION = 1;

    private static final int PERMISSION_PENDING = 0;

    private static final int PERMISSION_APPROVED = 1;

    private static final int PERMISSION_REJECTED = 2;

    private static final int ADMIN_ROLE = 2;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final int MAX_PAGE_SIZE = 100;

    private final UserAccountRepository userRepository;

    private final UserNotificationPublisher notificationPublisher;

    public AdminPermissionService(UserAccountRepository userRepository,
                                  UserNotificationPublisher notificationPublisher) {
        this.userRepository = userRepository;
        this.notificationPublisher = notificationPublisher;
    }

    public PageResponse<PendingPermissionUserResponse> listPendingUsers(Integer pageNum, Integer pageSize) {
        requireAdmin();
        int normalizedPageNum = normalizePageNum(pageNum);
        int normalizedPageSize = normalizePageSize(pageSize);
        UserAccountPage page = userRepository.findPendingPermissionUsers(
                (normalizedPageNum - 1) * normalizedPageSize,
                normalizedPageSize);
        List<PendingPermissionUserResponse> records = page.records().stream()
                .map(PendingPermissionUserResponse::from)
                .toList();
        return PageResponse.of(records, page.total(), normalizedPageNum, normalizedPageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public PermissionReviewResponse reviewPermission(AdminPermissionReviewRequest request) {
        LoginUserContext admin = requireAdmin();
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "request must not be null");
        }
        if (request.userId() == null || request.userId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "userId must be positive");
        }
        if (request.approved() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "approved must not be null");
        }

        UserAccount user = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        if (!Integer.valueOf(PERMISSION_PENDING).equals(effectivePermissionReviewStatus(user))) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "permission application already reviewed");
        }

        boolean approved = request.approved();
        int publishPermission = approved ? HAS_PUBLISH_PERMISSION : NO_PUBLISH_PERMISSION;
        int reviewStatus = approved ? PERMISSION_APPROVED : PERMISSION_REJECTED;
        String reason = normalizeOptional(request.reason());
        boolean updated = userRepository.updatePermissionReview(
                user.id(),
                publishPermission,
                reviewStatus,
                reason,
                admin.userId());
        if (!updated) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "permission review failed");
        }

        publishReviewNotification(user.id(), approved, reason);
        return new PermissionReviewResponse(
                user.id(),
                approved ? "approved" : "rejected",
                publishPermission,
                reviewStatus);
    }

    private LoginUserContext requireAdmin() {
        LoginUserContext user = UserContextHolder.get()
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_LOGIN, "login required"));
        if (!Integer.valueOf(ADMIN_ROLE).equals(user.role())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "admin role required");
        }
        return user;
    }

    private int effectivePermissionReviewStatus(UserAccount user) {
        if (user.permissionReviewStatus() != null) {
            return user.permissionReviewStatus();
        }
        return Integer.valueOf(HAS_PUBLISH_PERMISSION).equals(user.publishPermission())
                ? PERMISSION_APPROVED
                : PERMISSION_PENDING;
    }

    private void publishReviewNotification(Long userId, boolean approved, String reason) {
        if (approved) {
            notificationPublisher.publishPermissionReviewNotification(
                    userId,
                    "发布权限审核通过",
                    "你的发布权限申请已通过，可以发布服务。");
            return;
        }

        String suffix = reason == null ? "" : "原因：" + reason;
        notificationPublisher.publishPermissionReviewNotification(
                userId,
                "发布权限审核未通过",
                "你的发布权限申请未通过。" + suffix);
    }

    private int normalizePageNum(Integer pageNum) {
        if (pageNum == null) {
            return DEFAULT_PAGE_NUM;
        }
        if (pageNum < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "pageNum must be positive");
        }
        return pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null) {
            return DEFAULT_PAGE_SIZE;
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "pageSize must be between 1 and 100");
        }
        return pageSize;
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
