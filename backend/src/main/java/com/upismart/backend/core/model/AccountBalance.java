package com.upismart.backend.core.model;

import java.io.Serializable;

/**
 * Encapsulates financial balances: monthly income, expenditures, unreserved spending balance,
 * and emergency reserve allocations.
 */
public class AccountBalance implements Serializable {

    private static final long serialVersionUID = 1L;

    private final double monthlySalary;
    private final double totalExpenditure;
    private final double committedBills;
    private final double emergencyReserve;
    private final double availableBalance;

    public AccountBalance(double monthlySalary, double totalExpenditure, double committedBills, double emergencyReserve) {
        this.monthlySalary = Math.max(0, monthlySalary);
        this.totalExpenditure = Math.max(0, totalExpenditure);
        this.committedBills = Math.max(0, committedBills);
        this.emergencyReserve = Math.max(0, emergencyReserve);
        // Available balance = Salary - (Expenditure + Committed Bills + Emergency Reserve)
        this.availableBalance = Math.max(0, this.monthlySalary - (this.totalExpenditure + this.committedBills + this.emergencyReserve));
    }

    public double getMonthlySalary() {
        return monthlySalary;
    }

    public double getTotalExpenditure() {
        return totalExpenditure;
    }

    public double getCommittedBills() {
        return committedBills;
    }

    public double getEmergencyReserve() {
        return emergencyReserve;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }

    public double getPercentageSpent() {
        if (monthlySalary <= 0) return 0.0;
        return Math.min(100.0, (totalExpenditure / monthlySalary) * 100.0);
    }

    public double getPercentageRemaining() {
        if (monthlySalary <= 0) return 0.0;
        double remaining = monthlySalary - totalExpenditure;
        return Math.max(0.0, (remaining / monthlySalary) * 100.0);
    }
}
