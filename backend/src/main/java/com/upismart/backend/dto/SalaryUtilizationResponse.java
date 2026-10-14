package com.upismart.backend.dto;

import java.math.BigDecimal;
import java.util.Map;

public class SalaryUtilizationResponse {

    private BigDecimal monthlyIncome;
    private BigDecimal totalExpenditure;
    private BigDecimal remainingBalance;
    private double percentageSpent;
    private double percentageRemaining;
    private String utilizationStatus;
    private BigDecimal dailySafeToSpend;
    private Map<String, Double> categoryBreakdown;

    public SalaryUtilizationResponse() {}

    public SalaryUtilizationResponse(BigDecimal monthlyIncome, BigDecimal totalExpenditure,
                                     BigDecimal remainingBalance, double percentageSpent,
                                     double percentageRemaining, String utilizationStatus,
                                     BigDecimal dailySafeToSpend, Map<String, Double> categoryBreakdown) {
        this.monthlyIncome = monthlyIncome;
        this.totalExpenditure = totalExpenditure;
        this.remainingBalance = remainingBalance;
        this.percentageSpent = percentageSpent;
        this.percentageRemaining = percentageRemaining;
        this.utilizationStatus = utilizationStatus;
        this.dailySafeToSpend = dailySafeToSpend;
        this.categoryBreakdown = categoryBreakdown;
    }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public BigDecimal getTotalExpenditure() { return totalExpenditure; }
    public void setTotalExpenditure(BigDecimal totalExpenditure) { this.totalExpenditure = totalExpenditure; }

    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }

    public double getPercentageSpent() { return percentageSpent; }
    public void setPercentageSpent(double percentageSpent) { this.percentageSpent = percentageSpent; }

    public double getPercentageRemaining() { return percentageRemaining; }
    public void setPercentageRemaining(double percentageRemaining) { this.percentageRemaining = percentageRemaining; }

    public String getUtilizationStatus() { return utilizationStatus; }
    public void setUtilizationStatus(String utilizationStatus) { this.utilizationStatus = utilizationStatus; }

    public BigDecimal getDailySafeToSpend() { return dailySafeToSpend; }
    public void setDailySafeToSpend(BigDecimal dailySafeToSpend) { this.dailySafeToSpend = dailySafeToSpend; }

    public Map<String, Double> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(Map<String, Double> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }
}
