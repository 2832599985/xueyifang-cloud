package com.xueyifang.cloud.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationCreateRequest(
        @NotNull(message = "接收者ID不能为空")
        Long recipientId,

        @NotNull(message = "通知类型不能为空")
        Integer notificationType,

        @NotBlank(message = "通知标题不能为空")
        String title,

        @NotBlank(message = "通知内容不能为空")
        String content,

        Long relatedId) {
}
