package com.upismart.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "upi_handle", nullable = false, unique = true, length = 100)
    private String upiHandle;

    @Column(name = "monthly_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlySalary;

    @Column(name = "security_pin_hash", nullable = false)
    private String securityPinHash;

    @Column(name = "biometrics_enabled", nullable = false)
    private Boolean biometricsEnabled;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserEntity() {}

    public UserEntity(String username, String email, String upiHandle, BigDecimal monthlySalary, String securityPinHash, Boolean biometricsEnabled) {
        this.username = username;
        this.email = email;
        this.upiHandle = upiHandle;
        this.monthlySalary = monthlySalary;
        this.securityPinHash = securityPinHash;
        this.biometricsEnabled = biometricsEnabled;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUpiHandle() { return upiHandle; }
    public void setUpiHandle(String upiHandle) { this.upiHandle = upiHandle; }

    public BigDecimal getMonthlySalary() { return monthlySalary; }
    public void setMonthlySalary(BigDecimal monthlySalary) { this.monthlySalary = monthlySalary; }

    public String getSecurityPinHash() { return securityPinHash; }
    public void setSecurityPinHash(String securityPinHash) { this.securityPinHash = securityPinHash; }

    public Boolean getBiometricsEnabled() { return biometricsEnabled; }
    public void setBiometricsEnabled(Boolean biometricsEnabled) { this.biometricsEnabled = biometricsEnabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
