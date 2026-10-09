package com.example.core.detector;

import java.io.Serializable;

/**
 * Result of a transparent rule-based spending check.
 * Strictly rule-based heuristics — not machine learning.
 */
public class SpendingRuleResult implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum RuleType {
        NORMAL,
        HIGH_BUDGET_PERCENTAGE,
        EXCEEDS_REMAINING_BUDGET,
        MULTIPLIER_OF_AVERAGE,
        BUDGET_VELOCITY_CRITICAL
    }

    private final boolean isFlagged;
    private final RuleType ruleType;
    private final String title;
    private final String message;
    private final String explanation;

    public SpendingRuleResult(boolean isFlagged, RuleType ruleType, String title, String message, String explanation) {
        this.isFlagged = isFlagged;
        this.ruleType = ruleType;
        this.title = title != null ? title : "";
        this.message = message != null ? message : "";
        this.explanation = explanation != null ? explanation : "";
    }

    public static SpendingRuleResult clean() {
        return new SpendingRuleResult(false, RuleType.NORMAL, "Normal Transaction", "Within expected personal spending limits.", "");
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getExplanation() {
        return explanation;
    }

    @Override
    public String toString() {
        return "SpendingRuleResult{" +
                "flagged=" + isFlagged +
                ", rule=" + ruleType +
                ", msg='" + message + '\'' +
                '}';
    }
}
