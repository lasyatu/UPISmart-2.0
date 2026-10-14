package com.upismart.backend.core.exception;

/**
 * Thrown when an operation is attempted on an Emergency Vault that is currently locked
 * due to multiple failed access attempts or strict reserve protection.
 */
public class VaultLockedException extends UpiSmartException {

    private final long unlockTimestampEpochMs;

    public VaultLockedException(String message, long unlockTimestampEpochMs) {
        super(message, "VAULT_LOCKED");
        this.unlockTimestampEpochMs = unlockTimestampEpochMs;
    }

    public long getUnlockTimestampEpochMs() {
        return unlockTimestampEpochMs;
    }
}
