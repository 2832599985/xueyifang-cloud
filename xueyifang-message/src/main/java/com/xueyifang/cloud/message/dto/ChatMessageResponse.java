package com.xueyifang.cloud.message.dto;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long messageId,
        UserSimpleResponse sender,
        UserSimpleResponse receiver,
        String content,
        Integer messageType,
        Integer isRead,
        LocalDateTime createTime) {
}
