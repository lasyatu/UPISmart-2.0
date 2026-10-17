-- ========================================================================
-- UPISmart 2.0 — Intelligent UPI-Based Personal Financial Management System
-- Academic MySQL Relational Database Schema
-- Compatible with MySQL 8.0+ / MariaDB 10.5+
-- ========================================================================

CREATE DATABASE IF NOT EXISTS upismart_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE upismart_db;

-- ------------------------------------------------------------------------
-- Table 1: users (User Profile & Account Setup)
-- ------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    upi_handle VARCHAR(100) NOT NULL UNIQUE,
    monthly_salary DECIMAL(12, 2) NOT NULL DEFAULT 50000.00,
    security_pin_hash VARCHAR(255) NOT NULL,
    biometrics_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_upi (upi_handle),
    INDEX idx_user_email (email)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------
-- Table 2: transactions (Persistent Financial Transaction History)
-- ------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transactions (
    tx_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reference_id VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    recipient_upi VARCHAR(100) NOT NULL,
    recipient_name VARCHAR(150) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    note VARCHAR(255) DEFAULT '',
    is_simulated BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tx_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_tx_user_created (user_id, created_at),
    INDEX idx_tx_category (user_id, category),
    INDEX idx_tx_reference (reference_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------
-- Table 3: emergency_vault (Separately Tracked Emergency Reserve)
-- ------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS emergency_vault (
    vault_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    reserve_balance DECIMAL(12, 2) NOT NULL DEFAULT 15000.00,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP NULL DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_vault_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------
-- Table 4: vault_audit_logs (Emergency Vault Security Audit Trail)
-- ------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vault_audit_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vault_id BIGINT NOT NULL,
    action_type ENUM('DEPOSIT', 'WITHDRAWAL', 'UNLOCK_ATTEMPT', 'LOCKOUT_TRIGGERED') NOT NULL,
    amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    is_successful BOOLEAN NOT NULL,
    failure_reason VARCHAR(255) DEFAULT '',
    resulting_balance DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_vault FOREIGN KEY (vault_id) REFERENCES emergency_vault(vault_id) ON DELETE CASCADE,
    INDEX idx_vault_logs (vault_id, created_at)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------
-- Table 5: bills (Bills Radar & Recurring Obligations)
-- ------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bills (
    bill_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    due_day INT NOT NULL,
    is_paid BOOLEAN NOT NULL DEFAULT FALSE,
    is_recurring BOOLEAN NOT NULL DEFAULT TRUE,
    biller_upi VARCHAR(100) DEFAULT 'billing@demo.upismart',
    last_paid_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bill_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_bill_user (user_id, due_day)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------
-- Initial Seed Data for Academic Testing & Demonstration
-- ------------------------------------------------------------------------
INSERT INTO users (user_id, username, email, upi_handle, monthly_salary, security_pin_hash, biometrics_enabled)
VALUES (1, 'Alex Rivera', 'alex@demo.upismart', 'alex@demo.upismart', 50000.00, 'SHA256_SALTED_DEMO_HASH', TRUE)
ON DUPLICATE KEY UPDATE username=VALUES(username);

INSERT INTO emergency_vault (vault_id, user_id, reserve_balance, is_locked, failed_attempts)
VALUES (1, 1, 15000.00, FALSE, 0)
ON DUPLICATE KEY UPDATE reserve_balance=VALUES(reserve_balance);

INSERT INTO bills (bill_id, user_id, title, category, amount, due_day, is_paid, is_recurring)
VALUES 
    (1, 1, 'Fiber High-Speed Internet', 'Bills & Utilities', 1299.00, 5, FALSE, TRUE),
    (2, 1, 'Electricity / Power Grid', 'Bills & Utilities', 2450.00, 12, FALSE, TRUE),
    (3, 1, 'Cloud Storage & Streaming', 'Subscriptions', 799.00, 20, FALSE, TRUE),
    (4, 1, 'Apartment Maintenance', 'Housing', 3500.00, 25, FALSE, TRUE)
ON DUPLICATE KEY UPDATE title=VALUES(title);

