package com.xueyifang.cloud.trade.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcTradeDisputeRepository implements TradeDisputeRepository {

    private static final int DISPUTE_PENDING = 1;

    private static final String DISPUTE_SELECT = """
            SELECT d.id, d.order_id, d.complainant_id, d.respondent_id, d.status,
                   d.reason, d.evidence, d.handle_result, d.handle_remark,
                   d.handler_id, d.handle_time, d.create_time, d.update_time,
                   o.order_number, o.service_id, o.total_amount, o.order_status,
                   o.payment_status, o.refund_status,
                   s.title AS service_title, s.cover_image AS service_image,
                   o.buyer_id, o.seller_id,
                   COALESCE(NULLIF(b.real_name, ''), NULLIF(b.nickname, ''), NULLIF(b.username, ''), CONCAT('用户', b.id)) AS buyer_name,
                   b.avatar AS buyer_avatar,
                   COALESCE(NULLIF(se.real_name, ''), NULLIF(se.nickname, ''), NULLIF(se.username, ''), CONCAT('用户', se.id)) AS seller_name,
                   se.avatar AS seller_avatar
            FROM service_dispute d
            JOIN service_order o ON o.id = d.order_id AND o.is_deleted = 0
            LEFT JOIN `service` s ON s.id = o.service_id AND s.is_deleted = 0
            LEFT JOIN `user` b ON b.id = o.buyer_id AND b.is_deleted = 0
            LEFT JOIN `user` se ON se.id = o.seller_id AND se.is_deleted = 0
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcTradeDisputeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long createDispute(DisputeCreateCommand command) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                            INSERT INTO service_dispute
                                (order_id, complainant_id, respondent_id, status, reason, evidence)
                            VALUES (?, ?, ?, 1, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, command.orderId());
            statement.setLong(2, command.complainantId());
            statement.setLong(3, command.respondentId());
            statement.setString(4, command.reason());
            statement.setString(5, command.evidence());
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("dispute id was not generated");
        }
        return key.longValue();
    }

    @Override
    public Optional<TradeDispute> findById(Long disputeId) {
        return jdbcTemplate.query(
                DISPUTE_SELECT + " WHERE d.id = ? AND d.is_deleted = 0 LIMIT 1",
                ps -> ps.setLong(1, disputeId),
                rs -> rs.next() ? Optional.of(mapDispute(rs)) : Optional.empty());
    }

    @Override
    public Optional<TradeDispute> findByIdForUpdate(Long disputeId) {
        return jdbcTemplate.query(
                DISPUTE_SELECT + " WHERE d.id = ? AND d.is_deleted = 0 LIMIT 1 FOR UPDATE",
                ps -> ps.setLong(1, disputeId),
                rs -> rs.next() ? Optional.of(mapDispute(rs)) : Optional.empty());
    }

    @Override
    public Optional<TradeDispute> findByOrderId(Long orderId) {
        return jdbcTemplate.query(
                DISPUTE_SELECT + " WHERE d.order_id = ? AND d.is_deleted = 0 LIMIT 1",
                ps -> ps.setLong(1, orderId),
                rs -> rs.next() ? Optional.of(mapDispute(rs)) : Optional.empty());
    }

    @Override
    public DisputePage findDisputes(DisputeListQuery query) {
        List<Object> parameters = new ArrayList<>();
        String whereClause = buildDisputeWhere(query, parameters);

        Long total = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM service_dispute d
                        JOIN service_order o ON o.id = d.order_id AND o.is_deleted = 0
                        """ + whereClause,
                Long.class,
                parameters.toArray());

        List<Object> listParameters = new ArrayList<>(parameters);
        listParameters.add(query.limit());
        listParameters.add(query.offset());
        List<TradeDispute> records = jdbcTemplate.query(
                DISPUTE_SELECT + " " + whereClause + " ORDER BY d.create_time DESC, d.id DESC LIMIT ? OFFSET ?",
                (rs, rowNum) -> mapDispute(rs),
                listParameters.toArray());

        return new DisputePage(records, total != null ? total : 0L);
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        Long count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM service_dispute
                        WHERE order_id = ? AND is_deleted = 0
                        """,
                Long.class,
                orderId);
        return count != null && count > 0;
    }

    @Override
    public boolean existsActiveByOrderId(Long orderId) {
        Long count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM service_dispute
                        WHERE order_id = ? AND status = ? AND is_deleted = 0
                        """,
                Long.class,
                orderId,
                DISPUTE_PENDING);
        return count != null && count > 0;
    }

    @Override
    public boolean handleDispute(DisputeHandleCommand command) {
        int updated = jdbcTemplate.update("""
                        UPDATE service_dispute
                        SET status = ?,
                            handle_result = ?,
                            handle_remark = ?,
                            handler_id = ?,
                            handle_time = ?
                        WHERE id = ? AND status = 1 AND is_deleted = 0
                        """,
                command.status(),
                command.handleResult(),
                command.handleRemark(),
                command.handlerId(),
                command.handleTime(),
                command.disputeId());
        return updated > 0;
    }

    private String buildDisputeWhere(DisputeListQuery query, List<Object> parameters) {
        StringBuilder where = new StringBuilder("WHERE d.is_deleted = 0");
        if (!query.admin()) {
            where.append(" AND (d.complainant_id = ? OR d.respondent_id = ?)");
            parameters.add(query.userId());
            parameters.add(query.userId());
        }
        if (query.status() != null) {
            where.append(" AND d.status = ?");
            parameters.add(query.status());
        }
        return where.toString();
    }

    private TradeDispute mapDispute(ResultSet rs) throws SQLException {
        return new TradeDispute(
                rs.getLong("id"),
                rs.getObject("order_id", Long.class),
                rs.getObject("complainant_id", Long.class),
                rs.getObject("respondent_id", Long.class),
                rs.getObject("status", Integer.class),
                rs.getString("reason"),
                rs.getString("evidence"),
                rs.getString("handle_result"),
                rs.getString("handle_remark"),
                rs.getObject("handler_id", Long.class),
                rs.getObject("handle_time", java.time.LocalDateTime.class),
                rs.getObject("create_time", java.time.LocalDateTime.class),
                rs.getObject("update_time", java.time.LocalDateTime.class),
                rs.getString("order_number"),
                rs.getObject("service_id", Long.class),
                rs.getString("service_title"),
                rs.getString("service_image"),
                rs.getBigDecimal("total_amount"),
                rs.getObject("order_status", Integer.class),
                rs.getObject("payment_status", Integer.class),
                rs.getObject("refund_status", Integer.class),
                rs.getObject("buyer_id", Long.class),
                rs.getString("buyer_name"),
                rs.getString("buyer_avatar"),
                rs.getObject("seller_id", Long.class),
                rs.getString("seller_name"),
                rs.getString("seller_avatar"));
    }
}
