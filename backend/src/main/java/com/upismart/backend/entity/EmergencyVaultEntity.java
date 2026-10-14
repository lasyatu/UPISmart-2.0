package com.upismart.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_vault")
public class EmergencyVaultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vault_id")
    private Long vaultId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "reserve_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal reserveBalance;

    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked;

    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public EmergencyVaultEntity() {}

    public EmergencyVaultEntity(Long userId, BigDecimal reserveBalance, Boolean isLocked, Integer failedAttempts) {
        this.userId = userId;
        this.reserveBalance = reserveBalance;
        this.isLocked = isLocked != null ? isLocked : false;
        this.failedAttempts = failedAttempts != null ? failedAttempts : 0;
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    @PrePersist
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getVaultId() { return vaultId; }
    public void setVaultId(Long vaultId) { this.vaultId = vaultId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getReserveBalance() { return reserveBalance; }
    public void setReserveBalance(BigDecimal reserveBalance) { this.reserveBalance = reserveBalance; }

    public Boolean getIsLocked() { return isLocked; }
    public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }

    public Integer getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(Integer failedAttempts) { this.failedAttempts = failedAttempts; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
