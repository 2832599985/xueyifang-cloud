package com.xueyifang.cloud.message.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.message.dto.ChatConversationResponse;
import com.xueyifang.cloud.message.dto.ChatMessageResponse;
import com.xueyifang.cloud.message.dto.ChatSendRequest;
import com.xueyifang.cloud.message.dto.PageResponse;
import com.xueyifang.cloud.message.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    public BaseResponse<Void> sendMessage(@Valid @RequestBody ChatSendRequest request) {
        chatService.sendMessage(request);
        return ResultUtils.success();
    }

    @GetMapping("/messages/{userId}")
    public BaseResponse<PageResponse<ChatMessageResponse>> listMessages(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "50") Integer pageSize) {
        return ResultUtils.success(chatService.listMessages(userId, pageNum, pageSize));
    }

    @GetMapping("/conversations")
    public BaseResponse<List<ChatConversationResponse>> listConversations() {
        return ResultUtils.success(chatService.listConversations());
    }
}
