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
import java.util.Locale;
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
    public SellerSalesSummary summarizeCompletedSalesBySeller(Long sellerId) {
        return jdbcTemplate.query("""
                        SELECT COUNT(1) AS total_sales,
                               COALESCE(SUM(total_amount), 0) AS total_revenue
                        FROM service_order
                        WHERE seller_id = ?
                          AND order_status = 4
                          AND is_deleted = 0
                        """,
                ps -> ps.setLong(1, sellerId),
                rs -> {
                    if (!rs.next()) {
                        return new SellerSalesSummary(0, BigDecimal.ZERO);
                    }
                    return new SellerSalesSummary(
                            rs.getInt("total_sales"),
                            rs.getBigDecimal("total_revenue"));
                });
    }

    @Override
    public Optional<BestSellingService> findBestSellingServiceBySeller(Long sellerId) {
        return jdbcTemplate.query("""
                        SELECT o.service_id,
                               COALESCE(NULLIF(s.title, ''), CONCAT('服务', o.service_id)) AS service_title,
                               COUNT(1) AS sales,
                               COALESCE(SUM(o.total_amount), 0) AS revenue
                        FROM service_order o
                        LEFT JOIN `service` s ON s.id = o.service_id AND s.is_deleted = 0
                        WHERE o.seller_id = ?
                          AND o.order_status = 4
                          AND o.is_deleted = 0
                        GROUP BY o.service_id, s.title
                        ORDER BY sales DESC, revenue DESC, o.service_id ASC
                        LIMIT 1
                        """,
                ps -> ps.setLong(1, sellerId),
                rs -> rs.next() ? Optional.of(new BestSellingService(
                        rs.getObject("service_id", Long.class),
                        rs.getString("service_title"),
                        rs.getInt("sales"),
                        rs.getBigDecimal("revenue"))) : Optional.empty());
    }

    @Override
    public List<RecentSalesOrder> findRecentCompletedSalesBySeller(Long sellerId, int limit) {
        return jdbcTemplate.query("""
                        SELECT id, order_number, total_amount, create_time
                        FROM service_order
                        WHERE seller_id = ?
                          AND order_status = 4
                          AND is_deleted = 0
                        ORDER BY create_time DESC, id DESC
                        LIMIT ?
                        """,
                (rs, rowNum) -> new RecentSalesOrder(
                        rs.getLong("id"),
                        rs.getString("order_number"),
                        rs.getBigDecimal("total_amount"),
                        rs.getObject("create_time", LocalDateTime.class)),
                sellerId,
                limit);
    }

    @Override
    public List<Long> findUnpaidOrderIdsCreatedAtOrBefore(LocalDateTime deadline, int limit) {
        return jdbcTemplate.queryForList("""
                        SELECT id
                        FROM service_order
                        WHERE order_status = 1
                          AND payment_status = 1
                          AND create_time <= ?
                          AND is_deleted = 0
                        ORDER BY create_time ASC, id ASC
                        LIMIT ?
                        """,
                Long.class,
                deadline,
                limit);
    }

    @Override
    public List<Long> findPendingReceiptOrderIdsShippedAtOrBefore(LocalDateTime deadline, int limit) {
        return jdbcTemplate.queryForList("""
                        SELECT o.id
                        FROM service_order o
                        WHERE o.order_status = 3
                          AND o.payment_status = 2
                          AND o.seller_ship_time IS NOT NULL
                          AND o.seller_ship_time <= ?
                          AND COALESCE(o.refund_status, 0) <> 1
                          AND o.is_deleted = 0
                          AND NOT EXISTS (
                              SELECT 1
                              FROM service_dispute d
                              WHERE d.order_id = o.id
                                AND d.status = 1
                                AND d.is_deleted = 0
                          )
                        ORDER BY o.seller_ship_time ASC, o.id ASC
                        LIMIT ?
                        """,
                Long.class,
                deadline,
                limit);
    }

    @Override
    public List<Long> findPendingRefundOrderIdsRequestedAtOrBefore(LocalDateTime deadline, int limit) {
        return jdbcTemplate.queryForList("""
                        SELECT o.id
                        FROM service_order o
                        WHERE o.refund_status = 1
                          AND o.payment_status = 2
                          AND o.order_status IN (2, 3)
                          AND o.refund_request_time IS NOT NULL
                          AND o.refund_request_time <= ?
                          AND o.is_deleted = 0
                          AND NOT EXISTS (
                              SELECT 1
                              FROM service_dispute d
                              WHERE d.order_id = o.id
                                AND d.status = 1
                                AND d.is_deleted = 0
                          )
                        ORDER BY o.refund_request_time ASC, o.id ASC
                        LIMIT ?
                        """,
                Long.class,
                deadline,
                limit);
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
    public boolean requestRefund(Long orderId, String reason, LocalDateTime refundRequestTime) {
        int updated = jdbcTemplate.update("""
                        UPDATE service_order
                        SET refund_status = 1,
                            refund_reason = ?,
                            refund_request_time = ?
                        WHERE id = ?
                          AND order_status = 3
                          AND payment_status = 2
                          AND COALESCE(refund_status, 0) IN (0, 3)
                          AND is_deleted = 0
                        """,
                reason,
                refundRequestTime,
                orderId);
        return updated > 0;
    }

    @Override
    public boolean rejectRefund(Long orderId) {
        int updated = jdbcTemplate.update("""
                        UPDATE service_order
                        SET refund_status = 3
                        WHERE id = ? AND refund_status = 1 AND is_deleted = 0
                        """,
                orderId);
        return updated > 0;
    }

    @Override
    public boolean markOrderRefunded(Long orderId, String reason, LocalDateTime refundTime) {
        int updated = jdbcTemplate.update("""
                        UPDATE service_order
                        SET payment_status = 3,
                            order_status = 6,
                            frozen_amount = 0,
                            refund_status = 4,
                            refund_reason = ?,
                            refund_request_time = COALESCE(refund_request_time, ?)
                        WHERE id = ?
                          AND order_status IN (2, 3)
                          AND payment_status = 2
                          AND frozen_amount > 0
                          AND COALESCE(refund_status, 0) <> 4
                          AND is_deleted = 0
                        """,
                reason,
                refundTime,
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
    public WalletTransactionPage findWalletTransactions(WalletTransactionQuery query) {
        List<Object> parameters = new ArrayList<>();
        String whereClause = buildWalletTransactionWhere(query, parameters);

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM wallet_transaction wt " + whereClause,
                Long.class,
                parameters.toArray());

        List<Object> listParameters = new ArrayList<>(parameters);
        listParameters.add(query.limit());
        listParameters.add(query.offset());
        List<WalletTransactionItem> records = jdbcTemplate.query("""
                        SELECT wt.id AS transaction_id, wt.user_id, wt.transaction_type, wt.amount,
                               wt.balance_before, wt.balance_after, wt.frozen_before, wt.frozen_after,
                               wt.related_order_id, o.order_number AS related_order_number,
                               wt.transaction_no, wt.remark, wt.create_time
                        FROM wallet_transaction wt
                        LEFT JOIN service_order o ON o.id = wt.related_order_id AND o.is_deleted = 0
                        """ + whereClause + " ORDER BY wt.create_time DESC, wt.id DESC LIMIT ? OFFSET ?",
                (rs, rowNum) -> mapWalletTransaction(rs),
                listParameters.toArray());

        return new WalletTransactionPage(records, total != null ? total : 0L);
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

    private String buildWalletTransactionWhere(WalletTransactionQuery query, List<Object> parameters) {
        StringBuilder where = new StringBuilder("WHERE wt.user_id = ?");
        parameters.add(query.userId());
        if (query.transactionType() != null) {
            where.append(" AND wt.transaction_type = ?");
            parameters.add(query.transactionType());
        }
        if (query.startTime() != null) {
            where.append(" AND wt.create_time >= ?");
            parameters.add(query.startTime());
        }
        if (query.endTime() != null) {
            where.append(" AND wt.create_time <= ?");
            parameters.add(query.endTime());
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
                normalizeRoleCode(rs.getString("role")),
                rs.getString("display_name"),
                rs.getString("avatar"),
                rs.getBigDecimal("wallet_balance"),
                rs.getBigDecimal("frozen_amount"));
    }

    static Integer normalizeRoleCode(String role) {
        if (role == null || role.isBlank()) {
            return 1;
        }
        String normalized = role.trim();
        try {
            return Integer.valueOf(normalized);
        } catch (NumberFormatException ignored) {
            return switch (normalized.toUpperCase(Locale.ROOT)) {
                case "ADMIN" -> 2;
                case "STUDENT" -> 1;
                default -> 1;
            };
        }
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

    private WalletTransactionItem mapWalletTransaction(ResultSet rs) throws SQLException {
        return new WalletTransactionItem(
                rs.getLong("transaction_id"),
                rs.getObject("user_id", Long.class),
                rs.getObject("transaction_type", Integer.class),
                rs.getBigDecimal("amount"),
                rs.getBigDecimal("balance_before"),
                rs.getBigDecimal("balance_after"),
                rs.getBigDecimal("frozen_before"),
                rs.getBigDecimal("frozen_after"),
                rs.getObject("related_order_id", Long.class),
                rs.getString("related_order_number"),
                rs.getString("transaction_no"),
                rs.getString("remark"),
                rs.getObject("create_time", LocalDateTime.class));
    }
}
