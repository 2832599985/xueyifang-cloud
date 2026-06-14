package com.xueyifang.cloud.system.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public class JdbcSystemStatisticsRepository implements SystemStatisticsRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSystemStatisticsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long countUsers() {
        return count("SELECT COUNT(1) FROM `user` WHERE is_deleted = 0");
    }

    @Override
    public long countActiveUsersSince(LocalDateTime since) {
        Long count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(DISTINCT active_user.user_id)
                        FROM (
                            SELECT buyer_id AS user_id
                            FROM service_order
                            WHERE create_time >= ? AND is_deleted = 0
                            UNION
                            SELECT seller_id AS user_id
                            FROM service_order
                            WHERE create_time >= ? AND is_deleted = 0
                        ) active_user
                        """,
                Long.class,
                since,
                since);
        return count != null ? count : 0L;
    }

    @Override
    public long countServices() {
        return count("SELECT COUNT(1) FROM `service` WHERE is_deleted = 0");
    }

    @Override
    public long countOrders() {
        return count("SELECT COUNT(1) FROM service_order WHERE is_deleted = 0");
    }

    @Override
    public long countCompletedOrders() {
        return count("SELECT COUNT(1) FROM service_order WHERE order_status = 4 AND is_deleted = 0");
    }

    @Override
    public long countPendingDisputes() {
        return count("SELECT COUNT(1) FROM service_dispute WHERE status = 1 AND is_deleted = 0");
    }

    @Override
    public BigDecimal sumCompletedOrderAmount() {
        return sum("""
                SELECT COALESCE(SUM(total_amount), 0)
                FROM service_order
                WHERE order_status = 4 AND is_deleted = 0
                """);
    }

    @Override
    public long countUsersCreatedBetween(LocalDateTime start, LocalDateTime end) {
        return countBetween("`user`", "is_deleted", start, end);
    }

    @Override
    public long countServicesCreatedBetween(LocalDateTime start, LocalDateTime end) {
        return countBetween("`service`", "is_deleted", start, end);
    }

    @Override
    public long countOrdersCreatedBetween(LocalDateTime start, LocalDateTime end) {
        Long count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM service_order
                        WHERE create_time >= ? AND create_time < ? AND is_deleted = 0
                        """,
                Long.class,
                start,
                end);
        return count != null ? count : 0L;
    }

    @Override
    public BigDecimal sumCompletedOrderAmountBetween(LocalDateTime start, LocalDateTime end) {
        BigDecimal amount = jdbcTemplate.queryForObject("""
                        SELECT COALESCE(SUM(total_amount), 0)
                        FROM service_order
                        WHERE create_time >= ?
                          AND create_time < ?
                          AND order_status = 4
                          AND is_deleted = 0
                        """,
                BigDecimal.class,
                start,
                end);
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private long count(String sql) {
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    private long countBetween(String table, String deleteColumn, LocalDateTime start, LocalDateTime end) {
        Long count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM %s
                        WHERE create_time >= ? AND create_time < ? AND %s = 0
                        """.formatted(table, deleteColumn),
                Long.class,
                start,
                end);
        return count != null ? count : 0L;
    }

    private BigDecimal sum(String sql) {
        BigDecimal amount = jdbcTemplate.queryForObject(sql, BigDecimal.class);
        return amount != null ? amount : BigDecimal.ZERO;
    }
}
