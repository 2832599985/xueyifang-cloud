package com.xueyifang.cloud.user.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class JdbcUserAccountRepository implements UserAccountRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserAccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UserAccount> findById(Long id) {
        return jdbcTemplate.query("""
                        SELECT id, username, password, student_id, real_name, nickname, phone, email,
                               dormitory, grade, professional_id, avatar, bio, role, publish_permission,
                               permission_review_status, permission_apply_reason, permission_review_reason,
                               permission_reviewed_by, permission_reviewed_at, wallet_balance, frozen_amount,
                               status, account_status, create_time, update_time
                        FROM `user`
                        WHERE id = ? AND is_deleted = 0
                        LIMIT 1
                        """,
                ps -> ps.setLong(1, id),
                rs -> rs.next() ? Optional.of(mapUser(rs)) : Optional.empty());
    }

    @Override
    public boolean existsByEmailExcludingUser(String email, Long userId) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM `user`
                        WHERE email = ? AND id <> ? AND is_deleted = 0
                        """,
                Integer.class,
                email,
                userId);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByPhoneExcludingUser(String phone, Long userId) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM `user`
                        WHERE phone = ? AND id <> ? AND is_deleted = 0
                        """,
                Integer.class,
                phone,
                userId);
        return count != null && count > 0;
    }

    @Override
    public boolean updateProfile(Long userId, UserProfileUpdateCommand command) {
        int updated = jdbcTemplate.update("""
                        UPDATE `user`
                        SET real_name = COALESCE(?, real_name),
                            nickname = COALESCE(?, nickname),
                            phone = COALESCE(?, phone),
                            email = COALESCE(?, email),
                            dormitory = COALESCE(?, dormitory),
                            grade = COALESCE(?, grade),
                            professional_id = COALESCE(?, professional_id),
                            avatar = COALESCE(?, avatar),
                            bio = COALESCE(?, bio)
                        WHERE id = ? AND is_deleted = 0
                        """,
                command.realName(),
                command.nickname(),
                command.phone(),
                command.email(),
                command.dormitory(),
                command.grade(),
                command.professionalId(),
                command.avatar(),
                command.bio(),
                userId);
        return updated > 0;
    }

    @Override
    public boolean updatePassword(Long userId, String password) {
        int updated = jdbcTemplate.update("""
                        UPDATE `user`
                        SET password = ?
                        WHERE id = ? AND is_deleted = 0
                        """,
                password,
                userId);
        return updated > 0;
    }

    @Override
    public boolean updatePermissionApplication(Long userId, int publishPermission, int permissionReviewStatus,
                                               String applyReason) {
        int updated = jdbcTemplate.update("""
                        UPDATE `user`
                        SET publish_permission = ?,
                            permission_review_status = ?,
                            permission_apply_reason = ?,
                            permission_review_reason = NULL,
                            permission_reviewed_by = NULL,
                            permission_reviewed_at = NULL
                        WHERE id = ? AND is_deleted = 0
                        """,
                publishPermission,
                permissionReviewStatus,
                applyReason,
                userId);
        return updated > 0;
    }

    @Override
    public UserAccountPage findPendingPermissionUsers(int offset, int limit) {
        Long total = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM `user`
                        WHERE permission_review_status = 0 AND is_deleted = 0
                        """,
                Long.class);

        var records = jdbcTemplate.query("""
                        SELECT id, username, password, student_id, real_name, nickname, phone, email,
                               dormitory, grade, professional_id, avatar, bio, role, publish_permission,
                               permission_review_status, permission_apply_reason, permission_review_reason,
                               permission_reviewed_by, permission_reviewed_at, wallet_balance, frozen_amount,
                               status, account_status, create_time, update_time
                        FROM `user`
                        WHERE permission_review_status = 0 AND is_deleted = 0
                        ORDER BY update_time ASC, id ASC
                        LIMIT ? OFFSET ?
                        """,
                (rs, rowNum) -> mapUser(rs),
                limit,
                offset);

        return new UserAccountPage(records, total != null ? total : 0L);
    }

    @Override
    public boolean updatePermissionReview(Long userId, int publishPermission, int permissionReviewStatus,
                                          String reviewReason, Long reviewedBy) {
        int updated = jdbcTemplate.update("""
                        UPDATE `user`
                        SET publish_permission = ?,
                            permission_review_status = ?,
                            permission_review_reason = ?,
                            permission_reviewed_by = ?,
                            permission_reviewed_at = CURRENT_TIMESTAMP(3)
                        WHERE id = ? AND permission_review_status = 0 AND is_deleted = 0
                        """,
                publishPermission,
                permissionReviewStatus,
                reviewReason,
                reviewedBy,
                userId);
        return updated > 0;
    }

    @Override
    public Set<String> findExistingStudentIds() {
        return jdbcTemplate.queryForList("""
                        SELECT student_id
                        FROM `user`
                        WHERE student_id IS NOT NULL
                          AND student_id <> ''
                          AND is_deleted = 0
                        """,
                String.class)
                .stream()
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> findExistingUsernames() {
        return jdbcTemplate.queryForList("""
                        SELECT username
                        FROM `user`
                        WHERE username IS NOT NULL
                          AND username <> ''
                          AND is_deleted = 0
                        """,
                String.class)
                .stream()
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Long> findActiveProfessionalIds() {
        return jdbcTemplate.queryForList("""
                        SELECT id
                        FROM professional
                        WHERE is_delete = 0
                        """,
                Long.class)
                .stream()
                .collect(Collectors.toSet());
    }

    @Override
    public Long createImportedUser(UserImportCreateCommand command) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                            INSERT INTO `user`
                                (username, password, student_id, real_name, nickname, phone, email,
                                 dormitory, grade, professional_id, role, publish_permission,
                                 permission_review_status, wallet_balance, frozen_amount, status, account_status)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, command.username());
            statement.setString(2, command.password());
            statement.setString(3, command.studentId());
            statement.setString(4, command.realName());
            statement.setString(5, command.nickname());
            statement.setString(6, command.phone());
            statement.setString(7, command.email());
            statement.setString(8, command.dormitory());
            statement.setString(9, command.grade());
            statement.setObject(10, command.professionalId());
            statement.setString(11, command.role());
            statement.setInt(12, command.publishPermission());
            statement.setInt(13, command.permissionReviewStatus());
            statement.setBigDecimal(14, command.walletBalance());
            statement.setBigDecimal(15, command.frozenAmount());
            statement.setInt(16, command.status());
            statement.setInt(17, command.accountStatus());
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("imported user id was not generated");
        }
        return key.longValue();
    }

    private UserAccount mapUser(ResultSet rs) throws SQLException {
        return new UserAccount(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("student_id"),
                rs.getString("real_name"),
                rs.getString("nickname"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("dormitory"),
                rs.getString("grade"),
                rs.getObject("professional_id", Long.class),
                rs.getString("avatar"),
                rs.getString("bio"),
                rs.getString("role"),
                rs.getObject("publish_permission", Integer.class),
                rs.getObject("permission_review_status", Integer.class),
                rs.getString("permission_apply_reason"),
                rs.getString("permission_review_reason"),
                rs.getObject("permission_reviewed_by", Long.class),
                rs.getObject("permission_reviewed_at", java.time.LocalDateTime.class),
                rs.getBigDecimal("wallet_balance"),
                rs.getBigDecimal("frozen_amount"),
                rs.getObject("status", Integer.class),
                rs.getObject("account_status", Integer.class),
                rs.getObject("create_time", java.time.LocalDateTime.class),
                rs.getObject("update_time", java.time.LocalDateTime.class));
    }
}
