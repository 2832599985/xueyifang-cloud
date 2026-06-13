package com.xueyifang.cloud.trade.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcTradeOrderRepository implements TradeOrderRepository {

    private static final String ORDER_SELECT = """
            SELECT o.id, o.order_number, o.service_id, o.buyer_id, o.seller_id, o.quantity,
                   o.unit_price, o.total_amount, o.trade_type, o.trade_location_id,
                   o.payment_status, o.order_status, o.frozen_amount, o.payment_method,
                   o.payment_time, o.seller_ship_time, o.buyer_confirm_time,
                   o.refund_status, o.refund_reason, o.refund_request_time, o.remark,
                   o.create_time, o.update_time,
                   s.title AS service_title, s.description AS service_description,
                   s.cover_image AS service_image,
                   COALESCE(NULLIF(b.real_name, ''), NULLIF(b.nickname, ''), NULLIF(b.username, ''), CONCAT('用户', b.id)) AS buyer_name,
                   b.avatar AS buyer_avatar,
                   COALESCE(NULLIF(se.real_name, ''), NULLIF(se.nickname, ''), NULLIF(se.username, ''), CONCAT('用户', se.id)) AS seller_name,
                   se.avatar AS seller_avatar,
                   EXISTS(
                       SELECT 1
                       FROM service_review r
                       WHERE r.order_id = o.id AND r.is_deleted = 0
                   ) AS is_reviewed
            FROM service_order o
            LEFT JOIN `service` s ON s.id = o.service_id AND s.is_deleted = 0
            LEFT JOIN `user` b ON b.id = o.buyer_id AND b.is_deleted = 0
            LEFT JOIN `user` se ON se.id = o.seller_id AND se.is_deleted = 0
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcTradeOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<TradeServiceSnapshot> findServiceForOrder(Long serviceId) {
        return jdbcTemplate.query("""
                        SELECT id, publisher_id, title, description, price, cover_image, status
                        FROM `service`
                        WHERE id = ? AND is_deleted = 0
                        LIMIT 1
                        """,
                ps -> ps.setLong(1, serviceId),
                rs -> rs.next() ? Optional.of(mapService(rs)) : Optional.empty());
    }

    @Override
    public Optional<TradeUserWallet> findUserById(Long userId) {
        return findUser(userId, false);
    }

    @Override
    public Optional<TradeUserWallet> findUserForUpdate(Long userId) {
        return findUser(userId, true);
    }

    @Override
    public boolean updateUserWallet(Long userId, BigDecimal walletBalance, BigDecimal frozenAmount) {
        int updated = jdbcTemplate.update("""
                        UPDATE `user`
                        SET wallet_balance = ?, frozen_amount = ?
                        WHERE id = ? AND is_deleted = 0
                        """,
                walletBalance,
                frozenAmount,
                userId);
        return updated > 0;
    }

    @Override
    public Long createOrder(OrderCreateCommand command) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                            INSERT INTO service_order
                                (order_number, service_id, buyer_id, seller_id, quantity, unit_price,
                                 total_amount, trade_type, trade_location_id, payment_status, order_status,
                                 frozen_amount, payment_method, refund_status, remark)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, command.orderNumber());
            statement.setLong(2, command.serviceId());
            statement.setLong(3, command.buyerId());
            statement.setLong(4, command.sellerId());
            statement.setInt(5, command.quantity());
            statement.setBigDecimal(6, command.unitPrice());
            statement.setBigDecimal(7, command.totalAmount());
            statement.setInt(8, command.tradeType());
            setNullableLong(statement, 9, command.tradeLocationId());
            statement.setInt(10, command.paymentStatus());
            statement.setInt(11, command.orderStatus());
            statement.setBigDecimal(12, command.frozenAmount());
            statement.setInt(13, command.paymentMethod());
            statement.setInt(14, command.refundStatus());
            statement.setString(15, command.remark());
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("order id was not generated");
        }
        return key.longValue();
    }

    @Override
    public Optional<TradeOrder> findOrderById(Long orderId) {
        return jdbcTemplate.query(
                ORDER_SELECT + " WHERE o.id = ? AND o.is_deleted = 0 LIMIT 1",
                ps -> ps.setLong(1, orderId),
                rs -> rs.next() ? Optional.of(mapOrder(rs)) : Optional.empty());
    }

    @Override
    public Optional<TradeOrder> findOrderForUpdate(Long orderId) {
        return jdbcTemplate.query(
                ORDER_SELECT + " WHERE o.id = ? AND o.is_deleted = 0 LIMIT 1 FOR UPDATE",
                ps -> ps.setLong(1, orderId),
                rs -> rs.next() ? Optional.of(mapOrder(rs)) : Optional.empty());
    }

    @Override
    public OrderPage findOrders(OrderListQuery query) {
        List<Object> parameters = new ArrayList<>();
        String whereClause = buildOrderWhere(query, parameters);

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM service_order o " + whereClause,
                Long.class,
                parameters.toArray());

        List<Object> listParameters = new ArrayList<>(parameters);
        listParameters.add(query.limit());
        listParameters.add(query.offset());
        List<TradeOrder> records = jdbcTemplate.query(
                ORDER_SELECT + " " + whereClause + " ORDER BY o.create_time DESC, o.id DESC LIMIT ? OFFSET ?",
                (rs, rowNum) -> mapOrder(rs),
                listParameters.toArray());

        return new OrderPage(records, total != null ? total : 0L);
    }

    @Override
    public boolean markOrderPaid(Long orderId, BigDecimal frozenAmount, Integer paymentMethod, LocalDateTime paymentTime) {
        int updated = jdbcTemplate.update("""
                        UPDATE service_order
                        SET order_status = 2,
                            payment_status = 2,
                            frozen_amount = ?,
                            payment_method = ?,
                            payment_time = ?
                        WHERE id = ? AND order_status = 1 AND payment_status = 1 AND is_deleted = 0
                        """,
                frozenAmount,
                paymentMethod,
                paymentTime,
                orderId);
        return updated > 0;
    }

    @Override
    public boolean cancelOrder(Long orderId) {
        int updated = jdbcTemplate.update("""
                        UPDATE service_order
                        SET order_status = 5
                        WHERE id = ? AND order_status = 1 AND is_deleted = 0
                        """,
                orderId);
        return updated > 0;
    }

    @Override
    public boolean shipOrder(Long orderId, LocalDateTime sellerShipTime) {
        int updated = jdbcTemplate.update("""
                        UPDATE service_order
                        SET order_status = 3, seller_ship_time = ?
                        WHERE id = ? AND order_status = 2 AND COALESCE(refund_status, 0) <> 1 AND is_deleted = 0
                        """,
                sellerShipTime,
                orderId);
        return updated > 0;
    }

    @Override
    public boolean completeOrder(Long orderId, LocalDateTime buyerConfirmTime) {
        int updated = jdbcTemplate.update("""
                        UPDATE service_order
                        SET order_status = 4,
                            buyer_confirm_time = ?,
                            frozen_amount = 0,
                            refund_status = 0
                        WHERE id = ? AND order_status = 3 AND COALESCE(refund_status, 0) <> 1 AND is_deleted = 0
                        """,
                buyerConfirmTime,
                orderId);
        return updated > 0;
    }

    @Override
    public boolean incrementServiceOrderCount(Long serviceId, Integer quantity) {
        int updated = jdbcTemplate.update("""
                        UPDATE `service`
                        SET order_count = order_count + ?
                        WHERE id = ? AND is_deleted = 0
                        """,
                quantity,
                serviceId);
        return updated > 0;
    }

    @Override
    public void insertOrderLog(OrderLogCommand command) {
        jdbcTemplate.update("""
                        INSERT INTO service_order_log
                            (order_id, old_status, new_status, operator_id, operator_role, action_type, remark)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                command.orderId(),
                command.oldStatus(),
                command.newStatus(),
                command.operatorId(),
                command.operatorRole(),
                command.actionType(),
                command.remark());
    }

    @Override
    public void insertWalletTransaction(WalletTransactionCommand command) {
        jdbcTemplate.update("""
                        INSERT INTO wallet_transaction
                            (user_id, transaction_type, amount, balance_before, balance_after,
                             frozen_before, frozen_after, related_order_id, transaction_no, remark)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                command.userId(),
                command.transactionType(),
                command.amount(),
                command.balanceBefore(),
                command.balanceAfter(),
                command.frozenBefore(),
                command.frozenAfter(),
                command.relatedOrderId(),
                command.transactionNo(),
                command.remark());
    }

    private Optional<TradeUserWallet> findUser(Long userId, boolean forUpdate) {
        String sql = """
                SELECT id, role,
                       COALESCE(NULLIF(real_name, ''), NULLIF(nickname, ''), NULLIF(username, ''), CONCAT('用户', id)) AS display_name,
                       avatar, wallet_balance, frozen_amount
                FROM `user`
                WHERE id = ? AND is_deleted = 0
                LIMIT 1
                """ + (forUpdate ? " FOR UPDATE" : "");
        return jdbcTemplate.query(
                sql,
                ps -> ps.setLong(1, userId),
                rs -> rs.next() ? Optional.of(mapUser(rs)) : Optional.empty());
    }

    private String buildOrderWhere(OrderListQuery query, List<Object> parameters) {
        StringBuilder where = new StringBuilder("WHERE o.is_deleted = 0");
        if (query.buyerId() != null) {
            where.append(" AND o.buyer_id = ?");
            parameters.add(query.buyerId());
        }
        if (query.sellerId() != null) {
            where.append(" AND o.seller_id = ?");
            parameters.add(query.sellerId());
        }
        if (query.orderStatus() != null) {
            where.append(" AND o.order_status = ?");
            parameters.add(query.orderStatus());
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

    private TradeServiceSnapshot mapService(ResultSet rs) throws SQLException {
        return new TradeServiceSnapshot(
                rs.getLong("id"),
                rs.getObject("publisher_id", Long.class),
                rs.getString("title"),
                rs.getString("description"),
                rs.getBigDecimal("price"),
                rs.getString("cover_image"),
                rs.getObject("status", Integer.class));
    }

    private TradeUserWallet mapUser(ResultSet rs) throws SQLException {
        return new TradeUserWallet(
                rs.getLong("id"),
                rs.getObject("role", Integer.class),
                rs.getString("display_name"),
                rs.getString("avatar"),
                rs.getBigDecimal("wallet_balance"),
                rs.getBigDecimal("frozen_amount"));
    }

    private TradeOrder mapOrder(ResultSet rs) throws SQLException {
        return new TradeOrder(
                rs.getLong("id"),
                rs.getString("order_number"),
                rs.getObject("service_id", Long.class),
                rs.getObject("buyer_id", Long.class),
                rs.getObject("seller_id", Long.class),
                rs.getObject("quantity", Integer.class),
                rs.getBigDecimal("unit_price"),
                rs.getBigDecimal("total_amount"),
                rs.getObject("trade_type", Integer.class),
                rs.getObject("trade_location_id", Long.class),
                rs.getObject("payment_status", Integer.class),
                rs.getObject("order_status", Integer.class),
                rs.getBigDecimal("frozen_amount"),
                rs.getObject("payment_method", Integer.class),
                rs.getObject("payment_time", LocalDateTime.class),
                rs.getObject("seller_ship_time", LocalDateTime.class),
                rs.getObject("buyer_confirm_time", LocalDateTime.class),
                rs.getObject("refund_status", Integer.class),
                rs.getString("refund_reason"),
                rs.getObject("refund_request_time", LocalDateTime.class),
                rs.getString("remark"),
                rs.getObject("create_time", LocalDateTime.class),
                rs.getObject("update_time", LocalDateTime.class),
                rs.getString("service_title"),
                rs.getString("service_description"),
                rs.getString("service_image"),
                rs.getString("buyer_name"),
                rs.getString("buyer_avatar"),
                rs.getString("seller_name"),
                rs.getString("seller_avatar"),
                rs.getBoolean("is_reviewed"));
    }
}
