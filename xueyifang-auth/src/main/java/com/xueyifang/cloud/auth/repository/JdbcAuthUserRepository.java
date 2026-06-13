package com.xueyifang.cloud.auth.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;

@Repository
public class JdbcAuthUserRepository implements AuthUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAuthUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<AuthUser> findByUsername(String username) {
        return jdbcTemplate.query("""
                        SELECT id, username, password, nickname, role, publish_permission, status
                        FROM `user`
                        WHERE username = ? AND is_deleted = 0
                        LIMIT 1
                        """,
                ps -> ps.setString(1, username),
                rs -> rs.next() ? Optional.of(mapUser(rs)) : Optional.empty());
    }

    @Override
    public boolean existsByUsername(String username) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM `user`
                        WHERE username = ? AND is_deleted = 0
                        """,
                Integer.class,
                username);
        return count != null && count > 0;
    }

    @Override
    public AuthUser create(AuthUserCreateCommand command) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                            INSERT INTO `user`
                            (username, password, nickname, phone, email, role, publish_permission, status)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, command.username());
            statement.setString(2, command.password());
            statement.setString(3, command.nickname());
            statement.setString(4, command.phone());
            statement.setString(5, command.email());
            statement.setString(6, command.role());
            statement.setInt(7, command.publishPermission());
            statement.setInt(8, command.status());
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            return findByUsername(command.username())
                    .orElseThrow(() -> new IllegalStateException("created user key is missing"));
        }

        return findById(key.longValue())
                .orElseThrow(() -> new IllegalStateException("created user is missing"));
    }

    private Optional<AuthUser> findById(Long id) {
        return jdbcTemplate.query("""
                        SELECT id, username, password, nickname, role, publish_permission, status
                        FROM `user`
                        WHERE id = ? AND is_deleted = 0
                        LIMIT 1
                        """,
                ps -> ps.setLong(1, id),
                rs -> rs.next() ? Optional.of(mapUser(rs)) : Optional.empty());
    }

    private AuthUser mapUser(ResultSet rs) throws java.sql.SQLException {
        return new AuthUser(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("nickname"),
                rs.getString("role"),
                rs.getObject("publish_permission", Integer.class),
                rs.getInt("status"));
    }
}
