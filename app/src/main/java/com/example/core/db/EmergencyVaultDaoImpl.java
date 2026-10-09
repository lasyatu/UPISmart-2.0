package com.example.core.db;

import com.example.core.model.VaultAuditLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete JDBC implementation of EmergencyVaultDao.
 * Uses atomic multi-table transaction: updates emergency_vault balance and writes vault_audit_logs.
 */
public class EmergencyVaultDaoImpl implements EmergencyVaultDao {

    private final DatabaseConnectionManager connectionManager;

    public EmergencyVaultDaoImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public double getReserveBalance(long userId) throws SQLException {
        String sql = "SELECT reserve_balance FROM emergency_vault WHERE user_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("reserve_balance");
                }
            }
        }
        return 0.0;
    }

    @Override
    public boolean updateReserveBalanceWithAudit(long userId, double newBalance, VaultAuditLog auditLog) throws SQLException {
        String updateVaultSql = "UPDATE emergency_vault SET reserve_balance = ? WHERE user_id = ?";
        String getVaultIdSql = "SELECT vault_id FROM emergency_vault WHERE user_id = ?";
        String insertAuditSql = "INSERT INTO vault_audit_logs " +
                "(vault_id, action_type, amount, is_successful, failure_reason, resulting_balance, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = connectionManager.getConnection();
            conn.setAutoCommit(false);

            long vaultId = -1L;
            try (PreparedStatement pstmt = conn.prepareStatement(getVaultIdSql)) {
                pstmt.setLong(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        vaultId = rs.getLong("vault_id");
                    }
                }
            }

            if (vaultId == -1L) {
                // Initialize vault record if not already existing
                String insertVault = "INSERT INTO emergency_vault (user_id, reserve_balance, is_locked, failed_attempts) VALUES (?, ?, false, 0)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertVault, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    pstmt.setLong(1, userId);
                    pstmt.setDouble(2, newBalance);
                    pstmt.executeUpdate();
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) vaultId = rs.getLong(1);
                    }
                }
            } else {
                try (PreparedStatement pstmt = conn.prepareStatement(updateVaultSql)) {
                    pstmt.setDouble(1, newBalance);
                    pstmt.setLong(2, userId);
                    pstmt.executeUpdate();
                }
            }

            // Write audit log inside the same atomic transaction
            if (vaultId > 0 && auditLog != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(insertAuditSql)) {
                    pstmt.setLong(1, vaultId);
                    pstmt.setString(2, auditLog.getActionType().name());
                    pstmt.setDouble(3, auditLog.getAmount());
                    pstmt.setBoolean(4, auditLog.isSuccessful());
                    pstmt.setString(5, auditLog.getReason());
                    pstmt.setDouble(6, auditLog.getResultingVaultBalance());
                    pstmt.setTimestamp(7, new Timestamp(auditLog.getTimestamp()));
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
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
    public List<VaultAuditLog> getAuditLogs(long userId, int limit) throws SQLException {
        String sql = "SELECT l.action_type, l.amount, l.is_successful, l.failure_reason, l.resulting_balance, l.created_at " +
                "FROM vault_audit_logs l " +
                "JOIN emergency_vault v ON l.vault_id = v.vault_id " +
                "WHERE v.user_id = ? " +
                "ORDER BY l.created_at DESC LIMIT ?";

        List<VaultAuditLog> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setInt(2, Math.max(1, limit));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    VaultAuditLog.ActionType type = VaultAuditLog.ActionType.valueOf(rs.getString("action_type"));
                    VaultAuditLog log = new VaultAuditLog(
                            rs.getTimestamp("created_at").getTime(),
                            type,
                            rs.getDouble("amount"),
                            rs.getBoolean("is_successful"),
                            rs.getString("failure_reason"),
                            rs.getDouble("resulting_balance")
                    );
                    list.add(log);
                }
            }
        }
        return list;
    }
}
