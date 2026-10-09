package com.example.core.db;

import com.example.core.model.Transaction;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object interface for financial transaction persistence in MySQL.
 */
public interface TransactionDao {

    long recordTransaction(long userId, Transaction transaction) throws SQLException;

    List<Transaction> getTransactionsByUserId(long userId, int limit) throws SQLException;

    double getTotalSpentThisMonth(long userId, int year, int month) throws SQLException;

    Map<String, Double> getCategoryBreakdown(long userId) throws SQLException;
}
