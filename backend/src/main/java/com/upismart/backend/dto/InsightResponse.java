package com.upismart.backend.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class InsightResponse {

    private BigDecimal monthlyBudget;
    private BigDecimal currentSpend;
    private BigDecimal remainingBudget;
    private BigDecimal dailySafeToSpend;
    private BigDecimal billsReserved;
    private String topSpendingCategory;
    private BigDecimal topCategoryAmount;
    private String largestDebitTitle;
    private BigDecimal largestDebitAmount;
    private double spendingVelocityPercent;
    private boolean isVelocityWarning;
    private List<String> ruleDetections;
    private List<String> bucksTips;

    public InsightResponse() {}

    public InsightResponse(BigDecimal monthlyBudget, BigDecimal currentSpend, BigDecimal remainingBudget,
                           BigDecimal dailySafeToSpend, BigDecimal billsReserved, String topSpendingCategory,
                           BigDecimal topCategoryAmount, String largestDebitTitle, BigDecimal largestDebitAmount,
                           double spendingVelocityPercent, boolean isVelocityWarning,
                           List<String> ruleDetections, List<String> bucksTips) {
        this.monthlyBudget = monthlyBudget;
        this.currentSpend = currentSpend;
        this.remainingBudget = remainingBudget;
        this.dailySafeToSpend = dailySafeToSpend;
        this.billsReserved = billsReserved;
        this.topSpendingCategory = topSpendingCategory;
        this.topCategoryAmount = topCategoryAmount;
        this.largestDebitTitle = largestDebitTitle;
        this.largestDebitAmount = largestDebitAmount;
        this.spendingVelocityPercent = spendingVelocityPercent;
        this.isVelocityWarning = isVelocityWarning;
        this.ruleDetections = ruleDetections;
        this.bucksTips = bucksTips;
    }

    public BigDecimal getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(BigDecimal monthlyBudget) { this.monthlyBudget = monthlyBudget; }

    public BigDecimal getCurrentSpend() { return currentSpend; }
    public void setCurrentSpend(BigDecimal currentSpend) { this.currentSpend = currentSpend; }

    public BigDecimal getRemainingBudget() { return remainingBudget; }
    public void setRemainingBudget(BigDecimal remainingBudget) { this.remainingBudget = remainingBudget; }

    public BigDecimal getDailySafeToSpend() { return dailySafeToSpend; }
    public void setDailySafeToSpend(BigDecimal dailySafeToSpend) { this.dailySafeToSpend = dailySafeToSpend; }

    public BigDecimal getBillsReserved() { return billsReserved; }
    public void setBillsReserved(BigDecimal billsReserved) { this.billsReserved = billsReserved; }

    public String getTopSpendingCategory() { return topSpendingCategory; }
    public void setTopSpendingCategory(String topSpendingCategory) { this.topSpendingCategory = topSpendingCategory; }

    public BigDecimal getTopCategoryAmount() { return topCategoryAmount; }
    public void setTopCategoryAmount(BigDecimal topCategoryAmount) { this.topCategoryAmount = topCategoryAmount; }

    public String getLargestDebitTitle() { return largestDebitTitle; }
    public void setLargestDebitTitle(String largestDebitTitle) { this.largestDebitTitle = largestDebitTitle; }

    public BigDecimal getLargestDebitAmount() { return largestDebitAmount; }
    public void setLargestDebitAmount(BigDecimal largestDebitAmount) { this.largestDebitAmount = largestDebitAmount; }

    public double getSpendingVelocityPercent() { return spendingVelocityPercent; }
    public void setSpendingVelocityPercent(double spendingVelocityPercent) { this.spendingVelocityPercent = spendingVelocityPercent; }

    public boolean isVelocityWarning() { return isVelocityWarning; }
    public void setVelocityWarning(boolean velocityWarning) { isVelocityWarning = velocityWarning; }

    public List<String> getRuleDetections() { return ruleDetections; }
    public void setRuleDetections(List<String> ruleDetections) { this.ruleDetections = ruleDetections; }

    public List<String> getBucksTips() { return bucksTips; }
    public void setBucksTips(List<String> bucksTips) { this.bucksTips = bucksTips; }
}
