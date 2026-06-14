package com.xueyifang.cloud.message.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.message.dto.NotificationCreateRequest;
import com.xueyifang.cloud.message.dto.NotificationResponse;
import com.xueyifang.cloud.message.dto.PageResponse;
import com.xueyifang.cloud.message.repository.MessagePage;
import com.xueyifang.cloud.message.repository.MessageRepository;
import com.xueyifang.cloud.message.repository.NotificationCreateCommand;
import com.xueyifang.cloud.message.repository.NotificationItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class NotificationService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final int MAX_PAGE_SIZE = 100;

    private final MessageRepository repository;

    private final MessagePushService pushService;

    public NotificationService(MessageRepository repository, MessagePushService pushService) {
        this.repository = repository;
        this.pushService = pushService;
    }

    public PageResponse<NotificationResponse> listMyNotifications(Integer pageNum, Integer pageSize,
                                                                   Integer notificationType) {
        Long userId = currentUserId();
        Integer normalizedType = normalizeNotificationType(notificationType, true);
        int normalizedPageNum = normalizePageNum(pageNum);
        int normalizedPageSize = normalizePageSize(pageSize, DEFAULT_PAGE_SIZE);
        int offset = (normalizedPageNum - 1) * normalizedPageSize;
        MessagePage<NotificationItem> page = repository.findNotifications(
                userId,
                normalizedType,
                offset,
                normalizedPageSize);
        return PageResponse.of(
                page.records().stream().map(this::toNotificationResponse).toList(),
                page.total(),
                normalizedPageNum,
                normalizedPageSize);
    }

    public long getUnreadCount() {
        return repository.countUnreadNotifications(currentUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId) {
        if (notificationId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = currentUserId();
        NotificationItem notification = repository.findNotificationById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "通知不存在"));
        if (!userId.equals(notification.recipientId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作此通知");
        }
        repository.markNotificationRead(notificationId, userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead() {
        repository.markAllNotificationsRead(currentUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createNotification(NotificationCreateRequest request) {
        Long recipientId = request.recipientId();
        repository.findUserById(recipientId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        int notificationType = normalizeNotificationType(request.notificationType(), false);
        String title = normalizeText(request.title(), "通知标题不能为空");
        String content = normalizeText(request.content(), "通知内容不能为空");

        Long notificationId = repository.createNotification(new NotificationCreateCommand(
                recipientId,
                notificationType,
                title,
                content,
                request.relatedId(),
                MessageConstants.READ_STATUS_UNREAD));
        repository.findNotificationById(notificationId)
                .map(this::toNotificationResponse)
                .ifPresent(response -> pushService.pushNotification(recipientId, response));
        return notificationId;
    }

    private NotificationResponse toNotificationResponse(NotificationItem item) {
        return new NotificationResponse(
                item.id(),
                item.notificationType(),
                item.title(),
                item.content(),
                item.relatedId(),
                item.isRead(),
                item.createTime());
    }

    private Long currentUserId() {
        return UserContextHolder.get()
                .map(user -> {
                    if (user.userId() == null) {
                        throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
                    }
                    return user.userId();
                })
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_LOGIN));
    }

    private Integer normalizeNotificationType(Integer notificationType, boolean nullable) {
        if (notificationType == null) {
            if (nullable) {
                return null;
            }
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "通知类型不能为空");
        }
        if (notificationType < MessageConstants.NOTIFICATION_TYPE_PERMISSION_APPROVAL
                || notificationType > MessageConstants.NOTIFICATION_TYPE_SERVICE_REVIEW) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "通知类型不支持");
        }
        return notificationType;
    }

    private String normalizeText(String value, String errorMessage) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
        }
        return value.trim();
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int normalizePageSize(Integer pageSize, int defaultValue) {
        if (pageSize == null || pageSize < 1) {
            return defaultValue;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
