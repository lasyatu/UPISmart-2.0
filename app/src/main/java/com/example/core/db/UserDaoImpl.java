package com.example.core.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Concrete JDBC implementation of UserDao demonstrating parameterized PreparedStatements.
 */
public class UserDaoImpl implements UserDao {

    private final DatabaseConnectionManager connectionManager;

    public UserDaoImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public long insertUser(UserProfile user) throws SQLException {
        String sql = "INSERT INTO users (username, email, upi_handle, monthly_salary, security_pin_hash, biometrics_enabled) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getUpiHandle());
            pstmt.setDouble(4, user.getMonthlySalary());
            pstmt.setString(5, user.getSecurityPinHash());
            pstmt.setBoolean(6, user.isBiometricsEnabled());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long generatedId = rs.getLong(1);
                    user.setUserId(generatedId);
                    return generatedId;
                }
            }
        }
        return -1L;
    }

    @Override
    public Optional<UserProfile> findById(long userId) throws SQLException {
        String sql = "SELECT user_id, username, email, upi_handle, monthly_salary, security_pin_hash, biometrics_enabled " +
                "FROM users WHERE user_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<UserProfile> findByUpiHandle(String upiHandle) throws SQLException {
        String sql = "SELECT user_id, username, email, upi_handle, monthly_salary, security_pin_hash, biometrics_enabled " +
                "FROM users WHERE upi_handle = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, upiHandle);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean updateMonthlySalary(long userId, double newSalary) throws SQLException {
        String sql = "UPDATE users SET monthly_salary = ? WHERE user_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, newSalary);
            pstmt.setLong(2, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    private UserProfile mapResultSet(ResultSet rs) throws SQLException {
        return new UserProfile(
                rs.getLong("user_id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("upi_handle"),
                rs.getDouble("monthly_salary"),
                rs.getString("security_pin_hash"),
                rs.getBoolean("biometrics_enabled")
        );
    }
}
