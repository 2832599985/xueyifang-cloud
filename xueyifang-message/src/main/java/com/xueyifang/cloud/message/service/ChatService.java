package com.xueyifang.cloud.message.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.message.dto.ChatConversationResponse;
import com.xueyifang.cloud.message.dto.ChatMessageResponse;
import com.xueyifang.cloud.message.dto.ChatSendRequest;
import com.xueyifang.cloud.message.dto.PageResponse;
import com.xueyifang.cloud.message.dto.UserSimpleResponse;
import com.xueyifang.cloud.message.repository.ChatCreateCommand;
import com.xueyifang.cloud.message.repository.ChatMessageItem;
import com.xueyifang.cloud.message.repository.MessagePage;
import com.xueyifang.cloud.message.repository.MessageRepository;
import com.xueyifang.cloud.message.repository.UserSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ChatService {

    private static final int DEFAULT_PAGE_SIZE = 50;

    private static final int MAX_PAGE_SIZE = 100;

    private final MessageRepository repository;

    private final MessagePushService pushService;

    public ChatService(MessageRepository repository, MessagePushService pushService) {
        this.repository = repository;
        this.pushService = pushService;
    }

    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(ChatSendRequest request) {
        Long senderId = currentUserId();
        Long receiverId = request.receiverId();
        if (senderId.equals(receiverId)) {
            throw new BusinessException(ErrorCode.CHAT_CANNOT_SEND_TO_SELF);
        }

        String content = normalizeContent(request.content());
        int messageType = normalizeMessageType(request.messageType());
        UserSummary receiver = repository.findUserById(receiverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_RECEIVER_NOT_EXIST));
        if (Integer.valueOf(2).equals(receiver.accountStatus())) {
            throw new BusinessException(ErrorCode.CHAT_RECEIVER_ACCOUNT_DISABLED);
        }

        Long messageId = repository.createChatMessage(new ChatCreateCommand(
                senderId,
                receiverId,
                content,
                messageType,
                MessageConstants.READ_STATUS_UNREAD,
                request.relatedServiceId(),
                request.relatedOrderId()));

        repository.findChatMessageById(messageId)
                .map(this::toChatMessageResponse)
                .ifPresent(response -> pushService.pushChatMessage(receiverId, response));
    }

    @Transactional(rollbackFor = Exception.class)
    public PageResponse<ChatMessageResponse> listMessages(Long targetUserId, Integer pageNum, Integer pageSize) {
        Long userId = currentUserId();
        if (targetUserId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }
        repository.findUserById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));

        int normalizedPageNum = normalizePageNum(pageNum);
        int normalizedPageSize = normalizePageSize(pageSize, DEFAULT_PAGE_SIZE);
        int offset = (normalizedPageNum - 1) * normalizedPageSize;
        MessagePage<ChatMessageItem> page = repository.findChatMessages(
                userId,
                targetUserId,
                offset,
                normalizedPageSize);
        List<ChatMessageResponse> records = toChatMessageResponses(page.records());
        repository.markConversationRead(userId, targetUserId);
        return PageResponse.of(records, page.total(), normalizedPageNum, normalizedPageSize);
    }

    public List<ChatConversationResponse> listConversations() {
        Long userId = currentUserId();
        return repository.findConversations(userId).stream()
                .map(item -> new ChatConversationResponse(
                        item.userId(),
                        item.realName(),
                        item.avatar(),
                        item.lastMessage(),
                        item.unreadCount() != null ? item.unreadCount() : 0,
                        item.lastMessageTime()))
                .toList();
    }

    private List<ChatMessageResponse> toChatMessageResponses(List<ChatMessageItem> items) {
        Set<Long> userIds = new HashSet<>();
        for (ChatMessageItem item : items) {
            userIds.add(item.senderId());
            userIds.add(item.receiverId());
        }
        Map<Long, UserSummary> users = repository.findUsersByIds(userIds);
        return items.stream()
                .map(item -> toChatMessageResponse(item, users))
                .toList();
    }

    private ChatMessageResponse toChatMessageResponse(ChatMessageItem item) {
        Map<Long, UserSummary> users = repository.findUsersByIds(Set.of(item.senderId(), item.receiverId()));
        return toChatMessageResponse(item, users);
    }

    private ChatMessageResponse toChatMessageResponse(ChatMessageItem item, Map<Long, UserSummary> users) {
        return new ChatMessageResponse(
                item.id(),
                toUserResponse(item.senderId(), users.get(item.senderId())),
                toUserResponse(item.receiverId(), users.get(item.receiverId())),
                item.content(),
                item.messageType(),
                item.isRead(),
                item.createTime());
    }

    private UserSimpleResponse toUserResponse(Long userId, UserSummary user) {
        if (user == null) {
            return new UserSimpleResponse(userId, null, null);
        }
        return new UserSimpleResponse(user.userId(), user.realName(), user.avatar());
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

    private String normalizeContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(ErrorCode.CHAT_CONTENT_EMPTY);
        }
        return content.trim();
    }

    private int normalizeMessageType(Integer messageType) {
        int normalized = messageType != null ? messageType : MessageConstants.MESSAGE_TYPE_TEXT;
        if (normalized < MessageConstants.MESSAGE_TYPE_TEXT || normalized > MessageConstants.MESSAGE_TYPE_FILE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息类型不支持");
        }
        return normalized;
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
