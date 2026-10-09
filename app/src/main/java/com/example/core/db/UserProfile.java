package com.example.core.db;

import java.io.Serializable;

/**
 * Encapsulates user profile details stored in the MySQL users table.
 */
public class UserProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    private long userId;
    private String username;
    private String email;
    private String upiHandle;
    private double monthlySalary;
    private String securityPinHash;
    private boolean biometricsEnabled;

    public UserProfile() {
    }

    public UserProfile(long userId, String username, String email, String upiHandle,
                       double monthlySalary, String securityPinHash, boolean biometricsEnabled) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.upiHandle = upiHandle;
        this.monthlySalary = monthlySalary;
        this.securityPinHash = securityPinHash;
        this.biometricsEnabled = biometricsEnabled;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUpiHandle() {
        return upiHandle;
    }

    public void setUpiHandle(String upiHandle) {
        this.upiHandle = upiHandle;
    }

    public double getMonthlySalary() {
        return monthlySalary;
    }

    public void setMonthlySalary(double monthlySalary) {
        this.monthlySalary = monthlySalary;
    }

    public String getSecurityPinHash() {
        return securityPinHash;
    }

    public void setSecurityPinHash(String securityPinHash) {
        this.securityPinHash = securityPinHash;
    }

    public boolean isBiometricsEnabled() {
        return biometricsEnabled;
    }

    public void setBiometricsEnabled(boolean biometricsEnabled) {
        this.biometricsEnabled = biometricsEnabled;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "id=" + userId +
                ", username='" + username + '\'' +
                ", upi='" + upiHandle + '\'' +
                ", salary=" + monthlySalary +
                '}';
    }
}
