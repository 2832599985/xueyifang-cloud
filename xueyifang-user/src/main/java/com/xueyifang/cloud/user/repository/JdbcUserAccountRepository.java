package com.xueyifang.cloud.user.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

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
                               permission_review_status, wallet_balance, frozen_amount, status,
                               account_status, create_time, update_time
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
    public boolean updatePermissionApplication(Long userId, int publishPermission, int permissionReviewStatus) {
        int updated = jdbcTemplate.update("""
                        UPDATE `user`
                        SET publish_permission = ?, permission_review_status = ?
                        WHERE id = ? AND is_deleted = 0
                        """,
                publishPermission,
                permissionReviewStatus,
                userId);
        return updated > 0;
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
                rs.getBigDecimal("wallet_balance"),
                rs.getBigDecimal("frozen_amount"),
                rs.getObject("status", Integer.class),
                rs.getObject("account_status", Integer.class),
                rs.getObject("create_time", java.time.LocalDateTime.class),
                rs.getObject("update_time", java.time.LocalDateTime.class));
    }
}
