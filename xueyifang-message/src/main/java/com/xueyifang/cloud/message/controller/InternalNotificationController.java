package com.xueyifang.cloud.message.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.message.dto.NotificationCreateRequest;
import com.xueyifang.cloud.message.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/notifications")
public class InternalNotificationController {

    private final NotificationService notificationService;

    public InternalNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public BaseResponse<Long> createNotification(@Valid @RequestBody NotificationCreateRequest request) {
        return ResultUtils.success(notificationService.createNotification(request));
    }
}
