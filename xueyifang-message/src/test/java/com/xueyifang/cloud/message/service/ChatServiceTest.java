package com.xueyifang.cloud.message.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.message.dto.ChatConversationResponse;
import com.xueyifang.cloud.message.dto.ChatMessageResponse;
import com.xueyifang.cloud.message.dto.ChatSendRequest;
import com.xueyifang.cloud.message.dto.PageResponse;
import com.xueyifang.cloud.message.support.InMemoryMessageRepository;
import com.xueyifang.cloud.message.support.RecordingMessagePushService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatServiceTest {

    private final InMemoryMessageRepository repository = new InMemoryMessageRepository();

    private final RecordingMessagePushService pushService = new RecordingMessagePushService();

    private final ChatService chatService = new ChatService(repository, pushService);

    @BeforeEach
    void setUp() {
        repository.clear();
        repository.putUser(1L, "Sender", "/avatar/1.png", 1);
        repository.putUser(2L, "Receiver", "/avatar/2.png", 1);
        repository.putUser(3L, "Disabled", "/avatar/3.png", 2);
        UserContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void sendsMessageAndPushesToReceiver() {
        UserContextHolder.set(new LoginUserContext(1L, 1, 1));

        chatService.sendMessage(new ChatSendRequest(2L, "  hello  ", null, 100L, null));

        assertThat(pushService.chatPushes()).hasSize(1);
        assertThat(pushService.chatPushes().getFirst().receiverId()).isEqualTo(2L);
        assertThat(pushService.chatPushes().getFirst().message().content()).isEqualTo("hello");
        assertThat(pushService.chatPushes().getFirst().message().messageType())
                .isEqualTo(MessageConstants.MESSAGE_TYPE_TEXT);
    }

    @Test
    void listsConversationAndMarksUnreadMessagesAsRead() {
        UserContextHolder.set(new LoginUserContext(1L, 1, 1));
        chatService.sendMessage(new ChatSendRequest(2L, "first", null, null, null));
        UserContextHolder.set(new LoginUserContext(2L, 1, 1));

        List<ChatConversationResponse> conversations = chatService.listConversations();
        PageResponse<ChatMessageResponse> messages = chatService.listMessages(1L, 1, 20);
        List<ChatConversationResponse> refreshedConversations = chatService.listConversations();

        assertThat(conversations).hasSize(1);
        assertThat(conversations.getFirst().unreadCount()).isEqualTo(1);
        assertThat(messages.total()).isEqualTo(1);
        assertThat(messages.records().getFirst().sender().userId()).isEqualTo(1L);
        assertThat(refreshedConversations.getFirst().unreadCount()).isZero();
    }

    @Test
    void rejectsSendingMessageToSelf() {
        UserContextHolder.set(new LoginUserContext(1L, 1, 1));

        assertThatThrownBy(() -> chatService.sendMessage(new ChatSendRequest(1L, "self", null, null, null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.CHAT_CANNOT_SEND_TO_SELF.getCode()));
    }

    @Test
    void rejectsDisabledReceiver() {
        UserContextHolder.set(new LoginUserContext(1L, 1, 1));

        assertThatThrownBy(() -> chatService.sendMessage(new ChatSendRequest(3L, "hello", null, null, null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(
                                ErrorCode.CHAT_RECEIVER_ACCOUNT_DISABLED.getCode()));
    }

    @Test
    void requiresLoginForChatOperations() {
        assertThatThrownBy(() -> chatService.listConversations())
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.USER_NOT_LOGIN.getCode()));
    }
}
