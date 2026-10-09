package com.example.core.db;

import com.example.core.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Concrete JDBC implementation of TransactionDao demonstrating:
 * - ACID transactions with manual commit and rollback on failure.
 * - PreparedStatements protecting against SQL injection.
 */
public class TransactionDaoImpl implements TransactionDao {

    private final DatabaseConnectionManager connectionManager;

    public TransactionDaoImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public long recordTransaction(long userId, Transaction transaction) throws SQLException {
        String insertSql = "INSERT INTO transactions " +
                "(user_id, reference_id, title, recipient_upi, recipient_name, amount, category, status, note, is_simulated, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = connectionManager.getConnection();
            // Begin ACID transaction block
            conn.setAutoCommit(false);

            long generatedTxId = -1L;
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setLong(1, userId);
                pstmt.setString(2, transaction.getReferenceId());
                pstmt.setString(3, transaction.getTitle());
                pstmt.setString(4, transaction.getRecipientUpiId());
                pstmt.setString(5, transaction.getRecipientName());
                pstmt.setDouble(6, transaction.getAmount());
                pstmt.setString(7, transaction.getCategory());
                pstmt.setString(8, transaction.getStatus());
                pstmt.setString(9, transaction.getNote());
                pstmt.setBoolean(10, transaction.isSimulated());
                pstmt.setTimestamp(11, new Timestamp(transaction.getTimestamp()));

                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedTxId = rs.getLong(1);
                        transaction.setId(generatedTxId);
                    }
                }
            }

            // Commit atomic transaction
            conn.commit();
            return generatedTxId;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    // Rollback transaction to maintain database consistency
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    @Override
    public List<Transaction> getTransactionsByUserId(long userId, int limit) throws SQLException {
        String sql = "SELECT tx_id, reference_id, title, recipient_upi, recipient_name, amount, category, status, note, is_simulated, created_at " +
                "FROM transactions WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";

        List<Transaction> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setInt(2, Math.max(1, limit));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction tx = new Transaction(
                            rs.getLong("tx_id"),
                            rs.getString("title"),
                            rs.getString("recipient_upi"),
                            rs.getString("recipient_name"),
                            rs.getDouble("amount"),
                            rs.getString("category"),
                            rs.getTimestamp("created_at").getTime(),
                            rs.getString("status"),
                            rs.getString("reference_id"),
                            rs.getString("note"),
                            rs.getBoolean("is_simulated")
                    );
                    list.add(tx);
                }
            }
        }
        return list;
    }

    @Override
    public double getTotalSpentThisMonth(long userId, int year, int month) throws SQLException {
        // month is 1-12
        String sql = "SELECT COALESCE(SUM(amount), 0.0) AS total_spent " +
                "FROM transactions WHERE user_id = ? AND status = 'SUCCESS' " +
                "AND YEAR(created_at) = ? AND MONTH(created_at) = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setInt(2, year);
            pstmt.setInt(3, month);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_spent");
                }
            }
        }
        return 0.0;
    }

    @Override
    public Map<String, Double> getCategoryBreakdown(long userId) throws SQLException {
        String sql = "SELECT category, COALESCE(SUM(amount), 0.0) AS category_total " +
                "FROM transactions WHERE user_id = ? AND status = 'SUCCESS' " +
                "GROUP BY category";

        Map<String, Double> breakdown = new HashMap<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    breakdown.put(rs.getString("category"), rs.getDouble("category_total"));
                }
            }
        }
        return breakdown;
    }
}
