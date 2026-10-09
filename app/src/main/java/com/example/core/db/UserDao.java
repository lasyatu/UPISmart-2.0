package com.example.core.db;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Data Access Object interface for user profiles using JDBC PreparedStatements.
 */
public interface UserDao {

    long insertUser(UserProfile user) throws SQLException;

    Optional<UserProfile> findById(long userId) throws SQLException;

    Optional<UserProfile> findByUpiHandle(String upiHandle) throws SQLException;

    boolean updateMonthlySalary(long userId, double newSalary) throws SQLException;
}
