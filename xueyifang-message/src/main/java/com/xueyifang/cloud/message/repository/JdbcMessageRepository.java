package com.xueyifang.cloud.message.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class JdbcMessageRepository implements MessageRepository {

    private static final String USER_COLUMNS = "id, real_name, avatar, account_status";

    private static final String CHAT_COLUMNS = """
            id, sender_id, receiver_id, content, message_type, is_read,
            related_service_id, related_order_id, create_time, update_time
            """;

    private static final String NOTIFICATION_COLUMNS = """
            id, recipient_id, notification_type, title, content, related_id,
            is_read, create_time, update_time
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcMessageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UserSummary> findUserById(Long userId) {
        return jdbcTemplate.query("""
                        SELECT %s
                        FROM user
                        WHERE id = ? AND is_deleted = 0
                        LIMIT 1
                        """.formatted(USER_COLUMNS),
                ps -> ps.setLong(1, userId),
                rs -> rs.next() ? Optional.of(mapUser(rs)) : Optional.empty());
    }

    @Override
    public Map<Long, UserSummary> findUsersByIds(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = String.join(", ", Collections.nCopies(userIds.size(), "?"));
        List<UserSummary> users = jdbcTemplate.query(
                "SELECT " + USER_COLUMNS + " FROM user WHERE is_deleted = 0 AND id IN (" + placeholders + ")",
                (rs, rowNum) -> mapUser(rs),
                userIds.toArray());
        Map<Long, UserSummary> userMap = new LinkedHashMap<>();
        for (UserSummary user : users) {
            userMap.put(user.userId(), user);
        }
        return userMap;
    }

    @Override
    public Long createChatMessage(ChatCreateCommand command) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                            INSERT INTO user_chat
                                (sender_id, receiver_id, content, message_type, is_read,
                                 related_service_id, related_order_id)
                            VALUES (?, ?, ?, ?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, command.senderId());
            statement.setLong(2, command.receiverId());
            statement.setString(3, command.content());
            statement.setInt(4, command.messageType());
            statement.setInt(5, command.isRead());
            setNullableLong(statement, 6, command.relatedServiceId());
            setNullableLong(statement, 7, command.relatedOrderId());
            return statement;
        }, keyHolder);
        return generatedId(keyHolder, "chat message id was not generated");
    }

    @Override
    public Optional<ChatMessageItem> findChatMessageById(Long messageId) {
        return jdbcTemplate.query("""
                        SELECT %s
                        FROM user_chat
                        WHERE id = ? AND is_deleted = 0
                        LIMIT 1
                        """.formatted(CHAT_COLUMNS),
                ps -> ps.setLong(1, messageId),
                rs -> rs.next() ? Optional.of(mapChatMessage(rs)) : Optional.empty());
    }

    @Override
    public MessagePage<ChatMessageItem> findChatMessages(Long userId, Long targetUserId, int offset, int limit) {
        Object[] baseParameters = {userId, targetUserId, targetUserId, userId};
        Long total = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM user_chat
                        WHERE is_deleted = 0
                          AND ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?))
                        """,
                Long.class,
                baseParameters);

        List<Object> parameters = new ArrayList<>(List.of(baseParameters));
        parameters.add(limit);
        parameters.add(offset);
        List<ChatMessageItem> records = jdbcTemplate.query("""
                        SELECT %s
                        FROM user_chat
                        WHERE is_deleted = 0
                          AND ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?))
                        ORDER BY create_time ASC, id ASC
                        LIMIT ? OFFSET ?
                        """.formatted(CHAT_COLUMNS),
                (rs, rowNum) -> mapChatMessage(rs),
                parameters.toArray());
        return new MessagePage<>(records, total != null ? total : 0L);
    }

    @Override
    public int markConversationRead(Long userId, Long targetUserId) {
        return jdbcTemplate.update("""
                        UPDATE user_chat
                        SET is_read = 1
                        WHERE receiver_id = ? AND sender_id = ? AND is_read = 0 AND is_deleted = 0
                        """,
                userId,
                targetUserId);
    }

    @Override
    public List<ChatConversationItem> findConversations(Long userId) {
        return jdbcTemplate.query("""
                        SELECT
                            conv.other_user_id AS user_id,
                            u.real_name,
                            u.avatar,
                            msg.content AS last_message,
                            conv.unread_count,
                            msg.create_time AS last_message_time
                        FROM (
                            SELECT
                                CASE WHEN c.sender_id = ? THEN c.receiver_id ELSE c.sender_id END AS other_user_id,
                                MAX(c.id) AS last_msg_id,
                                SUM(CASE WHEN c.receiver_id = ? AND c.is_read = 0 THEN 1 ELSE 0 END) AS unread_count
                            FROM user_chat c
                            WHERE c.is_deleted = 0
                              AND (c.sender_id = ? OR c.receiver_id = ?)
                            GROUP BY CASE WHEN c.sender_id = ? THEN c.receiver_id ELSE c.sender_id END
                        ) conv
                        INNER JOIN user_chat msg ON msg.id = conv.last_msg_id
                        INNER JOIN user u ON u.id = conv.other_user_id AND u.is_deleted = 0
                        ORDER BY msg.create_time DESC, msg.id DESC
                        """,
                (rs, rowNum) -> new ChatConversationItem(
                        rs.getLong("user_id"),
                        rs.getString("real_name"),
                        rs.getString("avatar"),
                        rs.getString("last_message"),
                        rs.getObject("unread_count", Integer.class),
                        rs.getObject("last_message_time", java.time.LocalDateTime.class)),
                userId,
                userId,
                userId,
                userId,
                userId);
    }

    @Override
    public Long createNotification(NotificationCreateCommand command) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                            INSERT INTO notification
                                (recipient_id, notification_type, title, content, related_id, is_read)
                            VALUES (?, ?, ?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, command.recipientId());
            statement.setInt(2, command.notificationType());
            statement.setString(3, command.title());
            statement.setString(4, command.content());
            setNullableLong(statement, 5, command.relatedId());
            statement.setInt(6, command.isRead());
            return statement;
        }, keyHolder);
        return generatedId(keyHolder, "notification id was not generated");
    }

    @Override
    public Optional<NotificationItem> findNotificationById(Long notificationId) {
        return jdbcTemplate.query("""
                        SELECT %s
                        FROM notification
                        WHERE id = ?
                        LIMIT 1
                        """.formatted(NOTIFICATION_COLUMNS),
                ps -> ps.setLong(1, notificationId),
                rs -> rs.next() ? Optional.of(mapNotification(rs)) : Optional.empty());
    }

    @Override
    public MessagePage<NotificationItem> findNotifications(Long recipientId, Integer notificationType,
                                                           int offset, int limit) {
        List<Object> parameters = new ArrayList<>();
        String whereClause = buildNotificationWhere(recipientId, notificationType, parameters);
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM notification " + whereClause,
                Long.class,
                parameters.toArray());

        List<Object> listParameters = new ArrayList<>(parameters);
        listParameters.add(limit);
        listParameters.add(offset);
        List<NotificationItem> records = jdbcTemplate.query(
                "SELECT " + NOTIFICATION_COLUMNS + " FROM notification " + whereClause
                        + " ORDER BY create_time DESC, id DESC LIMIT ? OFFSET ?",
                (rs, rowNum) -> mapNotification(rs),
                listParameters.toArray());
        return new MessagePage<>(records, total != null ? total : 0L);
    }

    @Override
    public long countUnreadNotifications(Long recipientId) {
        Long count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM notification
                        WHERE recipient_id = ? AND is_read = 0
                        """,
                Long.class,
                recipientId);
        return count != null ? count : 0L;
    }

    @Override
    public boolean markNotificationRead(Long notificationId, Long recipientId) {
        int updated = jdbcTemplate.update("""
                        UPDATE notification
                        SET is_read = 1
                        WHERE id = ? AND recipient_id = ?
                        """,
                notificationId,
                recipientId);
        return updated > 0;
    }

    @Override
    public int markAllNotificationsRead(Long recipientId) {
        return jdbcTemplate.update("""
                        UPDATE notification
                        SET is_read = 1
                        WHERE recipient_id = ? AND is_read = 0
                        """,
                recipientId);
    }

    private String buildNotificationWhere(Long recipientId, Integer notificationType, List<Object> parameters) {
        StringBuilder where = new StringBuilder("WHERE recipient_id = ?");
        parameters.add(recipientId);
        if (notificationType != null) {
            where.append(" AND notification_type = ?");
            parameters.add(notificationType);
        }
        return where.toString();
    }

    private void setNullableLong(PreparedStatement statement, int parameterIndex, Long value) throws SQLException {
        if (value == null) {
            statement.setObject(parameterIndex, null);
            return;
        }
        statement.setLong(parameterIndex, value);
    }

    private Long generatedId(KeyHolder keyHolder, String errorMessage) {
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException(errorMessage);
        }
        return key.longValue();
    }

    private UserSummary mapUser(ResultSet rs) throws SQLException {
        return new UserSummary(
                rs.getLong("id"),
                rs.getString("real_name"),
                rs.getString("avatar"),
                rs.getObject("account_status", Integer.class));
    }

    private ChatMessageItem mapChatMessage(ResultSet rs) throws SQLException {
        return new ChatMessageItem(
                rs.getLong("id"),
                rs.getLong("sender_id"),
                rs.getLong("receiver_id"),
                rs.getString("content"),
                rs.getObject("message_type", Integer.class),
                rs.getObject("is_read", Integer.class),
                rs.getObject("related_service_id", Long.class),
                rs.getObject("related_order_id", Long.class),
                rs.getObject("create_time", java.time.LocalDateTime.class),
                rs.getObject("update_time", java.time.LocalDateTime.class));
    }

    private NotificationItem mapNotification(ResultSet rs) throws SQLException {
        return new NotificationItem(
                rs.getLong("id"),
                rs.getLong("recipient_id"),
                rs.getObject("notification_type", Integer.class),
                rs.getString("title"),
                rs.getString("content"),
                rs.getObject("related_id", Long.class),
                rs.getObject("is_read", Integer.class),
                rs.getObject("create_time", java.time.LocalDateTime.class),
                rs.getObject("update_time", java.time.LocalDateTime.class));
    }
}
