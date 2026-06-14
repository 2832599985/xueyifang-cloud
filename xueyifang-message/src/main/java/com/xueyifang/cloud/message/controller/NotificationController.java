package com.xueyifang.cloud.message.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.message.dto.NotificationResponse;
import com.xueyifang.cloud.message.dto.PageResponse;
import com.xueyifang.cloud.message.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/my-notifications")
    public BaseResponse<PageResponse<NotificationResponse>> listMyNotifications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer notificationType) {
        return ResultUtils.success(notificationService.listMyNotifications(pageNum, pageSize, notificationType));
    }

    @GetMapping("/unreadCount")
    public BaseResponse<Long> getUnreadCount() {
        return ResultUtils.success(notificationService.getUnreadCount());
    }

    @PostMapping("/{id}/read")
    public BaseResponse<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResultUtils.success();
    }

    @PostMapping("/readAll")
    public BaseResponse<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResultUtils.success();
    }
}
