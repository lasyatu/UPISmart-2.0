package com.example.core.exception;

/**
 * Thrown when an operation is attempted on an Emergency Vault that is currently locked
 * due to multiple failed access attempts or strict reserve protection.
 */
public class VaultLockedException extends UpiSmartException {

    private final long lockedUntilTimestamp;

    public VaultLockedException(String message) {
        super("ERR_VAULT_LOCKED", message);
        this.lockedUntilTimestamp = 0L;
    }

    public VaultLockedException(String message, long lockedUntilTimestamp) {
        super("ERR_VAULT_LOCKED", message);
        this.lockedUntilTimestamp = lockedUntilTimestamp;
    }

    public long getLockedUntilTimestamp() {
        return lockedUntilTimestamp;
    }
}
