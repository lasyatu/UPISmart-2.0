package com.upismart.backend.core.vault;

import com.upismart.backend.core.exception.InsufficientBalanceException;
import com.upismart.backend.core.exception.InvalidAmountException;
import com.upismart.backend.core.exception.InvalidPinException;
import com.upismart.backend.core.exception.VaultLockedException;
import com.upismart.backend.core.model.VaultAuditLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Encapsulated Core Java business logic for the Emergency Vault Lock concept.
 *
 * Provides:
 * 1. Isolated, separately tracked emergency reserve balance.
 * 2. PIN validation with automatic temporary lockout after 3 consecutive failures.
 * 3. Atomic deposit and withdrawal operations with defensive bounds checking.
 * 4. Immutable audit trail logging every access and balance change.
 */
public class EmergencyVault implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int MAX_FAILED_ATTEMPTS = 3;
    public static final long LOCKOUT_DURATION_MS = 60 * 1000; // 60 seconds demo lockout

    private double reserveBalance;
    private final String accessPin;
    private boolean isUnlocked;
    private int consecutiveFailedAttempts;
    private long lockedUntilTimestamp;
    private final List<VaultAuditLog> auditLogs;

    public EmergencyVault(double initialReserve, String accessPin) {
        this.reserveBalance = Math.max(0.0, initialReserve);
        this.accessPin = (accessPin != null && !accessPin.trim().isEmpty()) ? accessPin.trim() : "1234";
        this.isUnlocked = false;
        this.consecutiveFailedAttempts = 0;
        this.lockedUntilTimestamp = 0;
        this.auditLogs = new ArrayList<>();
        recordAudit("INITIALIZATION", initialReserve, true, "", initialReserve);
    }

    public synchronized boolean unlock(String enteredPin) throws InvalidPinException, VaultLockedException {
        checkLockoutStatus();

        if (enteredPin == null || enteredPin.trim().isEmpty()) {
            throw new InvalidPinException("Vault PIN cannot be empty.", MAX_FAILED_ATTEMPTS - consecutiveFailedAttempts);
        }

        if (accessPin.equals(enteredPin.trim())) {
            this.isUnlocked = true;
            this.consecutiveFailedAttempts = 0;
            recordAudit("UNLOCK_SUCCESS", 0.0, true, "", reserveBalance);
            return true;
        } else {
            this.consecutiveFailedAttempts++;
            int remaining = Math.max(0, MAX_FAILED_ATTEMPTS - consecutiveFailedAttempts);
            if (consecutiveFailedAttempts >= MAX_FAILED_ATTEMPTS) {
                this.lockedUntilTimestamp = System.currentTimeMillis() + LOCKOUT_DURATION_MS;
                this.isUnlocked = false;
                recordAudit("LOCKOUT_TRIGGERED", 0.0, false, "Maximum PIN attempts exceeded", reserveBalance);
                throw new VaultLockedException(
                        "Vault locked due to " + MAX_FAILED_ATTEMPTS + " consecutive failed attempts. Try again in 60s.",
                        lockedUntilTimestamp
                );
            }
            recordAudit("UNLOCK_FAILED", 0.0, false, "Incorrect PIN", reserveBalance);
            throw new InvalidPinException("Incorrect vault PIN. " + remaining + " attempts remaining.", remaining);
        }
    }

    public synchronized void lock() {
        this.isUnlocked = false;
        recordAudit("MANUAL_LOCK", 0.0, true, "", reserveBalance);
    }

    public synchronized double deposit(double amount) throws InvalidAmountException {
        if (amount <= 0.0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new InvalidAmountException("Deposit amount must be strictly greater than zero.", amount);
        }
        this.reserveBalance += amount;
        recordAudit("DEPOSIT", amount, true, "", reserveBalance);
        return reserveBalance;
    }

    public synchronized double withdraw(double amount, String pin)
            throws InvalidAmountException, InsufficientBalanceException, InvalidPinException, VaultLockedException {

        checkLockoutStatus();

        if (amount <= 0.0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new InvalidAmountException("Withdrawal amount must be strictly greater than zero.", amount);
        }

        if (!isUnlocked) {
            unlock(pin);
        }

        if (amount > reserveBalance) {
            recordAudit("WITHDRAW_FAILED", amount, false, "Insufficient vault funds", reserveBalance);
            throw new InsufficientBalanceException(
                    String.format("Insufficient emergency reserve: Requested ₹%.2f exceeds available vault balance of ₹%.2f.",
                            amount, reserveBalance),
                    amount,
                    reserveBalance
            );
        }

        this.reserveBalance -= amount;
        recordAudit("WITHDRAW_SUCCESS", amount, true, "", reserveBalance);
        return reserveBalance;
    }

    private void checkLockoutStatus() throws VaultLockedException {
        if (lockedUntilTimestamp > 0) {
            long now = System.currentTimeMillis();
            if (now < lockedUntilTimestamp) {
                long remainingSeconds = (lockedUntilTimestamp - now) / 1000;
                throw new VaultLockedException(
                        "Vault is locked for security. Please wait " + remainingSeconds + " seconds.",
                        lockedUntilTimestamp
                );
            } else {
                // Lockout expired
                lockedUntilTimestamp = 0;
                consecutiveFailedAttempts = 0;
            }
        }
    }

    private void recordAudit(String action, double amount, boolean success, String reason, double balance) {
        String logId = "VLOG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        VaultAuditLog log = new VaultAuditLog(logId, action, amount, success, reason, balance, System.currentTimeMillis());
        auditLogs.add(log);
    }

    public synchronized double getReserveBalance() {
        return reserveBalance;
    }

    public synchronized boolean isUnlocked() {
        return isUnlocked;
    }

    public synchronized boolean isLockedOut() {
        return lockedUntilTimestamp > System.currentTimeMillis();
    }

    public synchronized int getRemainingAttempts() {
        return Math.max(0, MAX_FAILED_ATTEMPTS - consecutiveFailedAttempts);
    }

    public synchronized List<VaultAuditLog> getAuditLogs() {
        return Collections.unmodifiableList(new ArrayList<>(auditLogs));
    }
}
