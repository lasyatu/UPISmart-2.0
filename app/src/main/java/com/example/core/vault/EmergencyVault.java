package com.example.core.vault;

import com.example.core.exception.InsufficientBalanceException;
import com.example.core.exception.InvalidAmountException;
import com.example.core.exception.InvalidPinException;
import com.example.core.exception.VaultLockedException;
import com.example.core.model.VaultAuditLog;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EmergencyVault implements secure, separately tracked emergency reserve funds.
 *
 * Enforces:
 * 1. Strict separation between regular spending funds and emergency reserve.
 * 2. Withdrawal validation requiring authentication against a salted/hashed access PIN.
 * 3. Brute-force lockout: 3 consecutive failed PIN attempts lock the vault for 15 minutes.
 * 4. Audit logging of all deposit, withdrawal, and authentication attempts.
 */
public class EmergencyVault implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int MAX_FAILED_ATTEMPTS = 3;
    public static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000L; // 15 minutes

    private double reserveBalance;
    private String pinHash;
    private boolean isLocked;
    private int failedAttempts;
    private long lockedUntilTimestamp;
    private final List<VaultAuditLog> auditLogs;

    public EmergencyVault() {
        this(15000.0, "1234");
    }

    public EmergencyVault(double initialReserve, String initialPin) {
        this.reserveBalance = Math.max(0.0, initialReserve);
        this.pinHash = hashPin(initialPin != null ? initialPin : "1234");
        this.isLocked = false;
        this.failedAttempts = 0;
        this.lockedUntilTimestamp = 0L;
        this.auditLogs = new ArrayList<>();

        recordLog(VaultAuditLog.ActionType.DEPOSIT, this.reserveBalance, true, "Initial emergency reserve established");
    }

    public synchronized double getReserveBalance() {
        return reserveBalance;
    }

    public synchronized boolean isLocked() {
        if (isLocked && lockedUntilTimestamp > 0 && System.currentTimeMillis() >= lockedUntilTimestamp) {
            // Lockout period expired, reset automatic time-lock
            isLocked = false;
            failedAttempts = 0;
            lockedUntilTimestamp = 0L;
        }
        return isLocked;
    }

    public synchronized int getFailedAttempts() {
        return failedAttempts;
    }

    public synchronized long getLockedUntilTimestamp() {
        return lockedUntilTimestamp;
    }

    public synchronized List<VaultAuditLog> getAuditLogs() {
        return Collections.unmodifiableList(new ArrayList<>(auditLogs));
    }

    /**
     * Updates the access PIN. Requires existing PIN verification.
     */
    public synchronized void changePin(String oldPin, String newPin) throws InvalidPinException, VaultLockedException {
        verifyAccess(oldPin);
        if (newPin == null || newPin.trim().length() < 4) {
            throw new IllegalArgumentException("New vault PIN must be at least 4 digits.");
        }
        this.pinHash = hashPin(newPin.trim());
        recordLog(VaultAuditLog.ActionType.UNLOCK_ATTEMPT, 0.0, true, "Vault PIN changed successfully");
    }

    /**
     * Deposits money into the emergency reserve.
     */
    public synchronized void deposit(double amount) throws InvalidAmountException {
        if (amount <= 0.0) {
            throw new InvalidAmountException(amount, "Deposit amount must be strictly greater than zero.");
        }
        this.reserveBalance += amount;
        recordLog(VaultAuditLog.ActionType.DEPOSIT, amount, true, "Emergency fund deposit");
    }

    /**
     * Validates and executes a withdrawal from the emergency reserve.
     */
    public synchronized double withdraw(double amount, String pin)
            throws VaultLockedException, InvalidPinException, InsufficientBalanceException, InvalidAmountException {
        if (amount <= 0.0) {
            throw new InvalidAmountException(amount, "Withdrawal amount must be strictly positive.");
        }

        // Authenticate access
        verifyAccess(pin);

        // Check reserve adequacy
        if (amount > this.reserveBalance) {
            recordLog(VaultAuditLog.ActionType.WITHDRAWAL, amount, false,
                    "Insufficient vault funds. Available: ₹" + reserveBalance);
            throw new InsufficientBalanceException(amount, reserveBalance,
                    String.format("Requested ₹%.2f exceeds available Emergency Vault reserve of ₹%.2f.",
                            amount, reserveBalance));
        }

        this.reserveBalance -= amount;
        recordLog(VaultAuditLog.ActionType.WITHDRAWAL, amount, true, "Emergency reserve withdrawal approved");
        return amount;
    }

    /**
     * Explicitly unlocks the vault using the correct PIN.
     */
    public synchronized boolean unlock(String pin) throws InvalidPinException, VaultLockedException {
        verifyAccess(pin);
        this.isLocked = false;
        this.failedAttempts = 0;
        this.lockedUntilTimestamp = 0L;
        recordLog(VaultAuditLog.ActionType.UNLOCK_ATTEMPT, 0.0, true, "Vault unlocked by user");
        return true;
    }

    /**
     * Explicitly locks the vault.
     */
    public synchronized void lock() {
        this.isLocked = true;
        recordLog(VaultAuditLog.ActionType.LOCKOUT_TRIGGERED, 0.0, true, "Vault locked manually");
    }

    /**
     * Internal PIN validation with rate-limiting and automatic lockout.
     */
    private void verifyAccess(String pin) throws VaultLockedException, InvalidPinException {
        long now = System.currentTimeMillis();

        if (isLocked) {
            if (lockedUntilTimestamp > 0 && now < lockedUntilTimestamp) {
                long remainingSec = (lockedUntilTimestamp - now) / 1000L;
                throw new VaultLockedException("Emergency Vault is locked due to security attempts. Try again in " + remainingSec + "s.", lockedUntilTimestamp);
            } else if (lockedUntilTimestamp == 0L) {
                // Manually locked, requires correct PIN to unlock
            }
        }

        String inputHash = hashPin(pin);
        if (!this.pinHash.equals(inputHash)) {
            failedAttempts++;
            int remaining = Math.max(0, MAX_FAILED_ATTEMPTS - failedAttempts);

            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                isLocked = true;
                lockedUntilTimestamp = now + LOCKOUT_DURATION_MS;
                recordLog(VaultAuditLog.ActionType.LOCKOUT_TRIGGERED, 0.0, false,
                        "Vault locked: maximum failed PIN attempts exceeded (" + MAX_FAILED_ATTEMPTS + ")");
                throw new VaultLockedException("Maximum invalid PIN attempts reached. Vault locked for 15 minutes.", lockedUntilTimestamp);
            } else {
                recordLog(VaultAuditLog.ActionType.UNLOCK_ATTEMPT, 0.0, false,
                        "Invalid PIN entered. Remaining attempts: " + remaining);
                throw new InvalidPinException("Incorrect Emergency Vault PIN. Remaining attempts: " + remaining, remaining);
            }
        }

        // On successful PIN entry:
        failedAttempts = 0;
        lockedUntilTimestamp = 0L;
    }

    private void recordLog(VaultAuditLog.ActionType action, double amount, boolean success, String reason) {
        this.auditLogs.add(new VaultAuditLog(System.currentTimeMillis(), action, amount, success, reason, this.reserveBalance));
    }

    private static String hashPin(String pin) {
        if (pin == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(("UPISMART_SALT_" + pin).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(pin.hashCode());
        }
    }
}
