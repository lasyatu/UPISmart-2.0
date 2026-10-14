package com.upismart.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vault_audit_logs")
public class VaultAuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "vault_id", nullable = false)
    private Long vaultId;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "is_successful", nullable = false)
    private Boolean isSuccessful;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "resulting_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal resultingBalance;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public VaultAuditLogEntity() {}

    public VaultAuditLogEntity(Long vaultId, String actionType, BigDecimal amount,
                               Boolean isSuccessful, String failureReason, BigDecimal resultingBalance) {
        this.vaultId = vaultId;
        this.actionType = actionType;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.isSuccessful = isSuccessful;
        this.failureReason = failureReason != null ? failureReason : "";
        this.resultingBalance = resultingBalance;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }

    public Long getVaultId() { return vaultId; }
    public void setVaultId(Long vaultId) { this.vaultId = vaultId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Boolean getIsSuccessful() { return isSuccessful; }
    public void setIsSuccessful(Boolean isSuccessful) { this.isSuccessful = isSuccessful; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public BigDecimal getResultingBalance() { return resultingBalance; }
    public void setResultingBalance(BigDecimal resultingBalance) { this.resultingBalance = resultingBalance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
