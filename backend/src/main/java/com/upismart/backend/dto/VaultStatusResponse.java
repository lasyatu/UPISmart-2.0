package com.upismart.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class VaultStatusResponse {

    private Long vaultId;
    private BigDecimal reserveBalance;
    private boolean isLocked;
    private int remainingAttempts;
    private String statusMessage;
    private List<VaultAuditLogDto> auditLogs;

    public VaultStatusResponse() {}

    public VaultStatusResponse(Long vaultId, BigDecimal reserveBalance, boolean isLocked,
                               int remainingAttempts, String statusMessage, List<VaultAuditLogDto> auditLogs) {
        this.vaultId = vaultId;
        this.reserveBalance = reserveBalance;
        this.isLocked = isLocked;
        this.remainingAttempts = remainingAttempts;
        this.statusMessage = statusMessage;
        this.auditLogs = auditLogs;
    }

    public Long getVaultId() { return vaultId; }
    public void setVaultId(Long vaultId) { this.vaultId = vaultId; }

    public BigDecimal getReserveBalance() { return reserveBalance; }
    public void setReserveBalance(BigDecimal reserveBalance) { this.reserveBalance = reserveBalance; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }

    public int getRemainingAttempts() { return remainingAttempts; }
    public void setRemainingAttempts(int remainingAttempts) { this.remainingAttempts = remainingAttempts; }

    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }

    public List<VaultAuditLogDto> getAuditLogs() { return auditLogs; }
    public void setAuditLogs(List<VaultAuditLogDto> auditLogs) { this.auditLogs = auditLogs; }

    public static class VaultAuditLogDto {
        private String actionType;
        private BigDecimal amount;
        private boolean isSuccessful;
        private String failureReason;
        private BigDecimal resultingBalance;
        private String timestamp;

        public VaultAuditLogDto() {}

        public VaultAuditLogDto(String actionType, BigDecimal amount, boolean isSuccessful,
                                String failureReason, BigDecimal resultingBalance, String timestamp) {
            this.actionType = actionType;
            this.amount = amount;
            this.isSuccessful = isSuccessful;
            this.failureReason = failureReason;
            this.resultingBalance = resultingBalance;
            this.timestamp = timestamp;
        }

        public String getActionType() { return actionType; }
        public BigDecimal getAmount() { return amount; }
        public boolean isSuccessful() { return isSuccessful; }
        public String getFailureReason() { return failureReason; }
        public BigDecimal getResultingBalance() { return resultingBalance; }
        public String getTimestamp() { return timestamp; }
    }
}
