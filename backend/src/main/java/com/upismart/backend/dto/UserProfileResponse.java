package com.upismart.backend.dto;

import java.math.BigDecimal;

public class UserProfileResponse {

    private Long userId;
    private String username;
    private String email;
    private String upiHandle;
    private BigDecimal monthlySalary;
    private boolean biometricsEnabled;

    public UserProfileResponse() {}

    public UserProfileResponse(Long userId, String username, String email, String upiHandle,
                               BigDecimal monthlySalary, boolean biometricsEnabled) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.upiHandle = upiHandle;
        this.monthlySalary = monthlySalary;
        this.biometricsEnabled = biometricsEnabled;
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

    public boolean isBiometricsEnabled() { return biometricsEnabled; }
    public void setBiometricsEnabled(boolean biometricsEnabled) { this.biometricsEnabled = biometricsEnabled; }
}
