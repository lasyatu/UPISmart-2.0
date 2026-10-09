package com.example.core.db;

import com.example.core.model.VaultAuditLog;

import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for Emergency Vault state and audit logging in MySQL.
 */
public interface EmergencyVaultDao {

    double getReserveBalance(long userId) throws SQLException;

    boolean updateReserveBalanceWithAudit(long userId, double newBalance, VaultAuditLog auditLog) throws SQLException;

    List<VaultAuditLog> getAuditLogs(long userId, int limit) throws SQLException;
}
