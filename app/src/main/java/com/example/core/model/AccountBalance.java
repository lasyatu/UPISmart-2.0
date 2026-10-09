package com.example.core.model;

import java.io.Serializable;

/**
 * Encapsulates financial balances: monthly income, expenditures, unreserved spending balance,
 * and emergency reserve allocations.
 */
public class AccountBalance implements Serializable {

    private static final long serialVersionUID = 1L;

    private double monthlyIncome;
    private double totalExpenditure;
    private double reservedBills;
    private double emergencyReserve;

    public AccountBalance() {
        this(50000.0, 0.0, 0.0, 10000.0);
    }

    public AccountBalance(double monthlyIncome, double totalExpenditure, double reservedBills, double emergencyReserve) {
        this.monthlyIncome = Math.max(0.0, monthlyIncome);
        this.totalExpenditure = Math.max(0.0, totalExpenditure);
        this.reservedBills = Math.max(0.0, reservedBills);
        this.emergencyReserve = Math.max(0.0, emergencyReserve);
    }

    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(double monthlyIncome) {
        this.monthlyIncome = Math.max(0.0, monthlyIncome);
    }

    public double getTotalExpenditure() {
        return totalExpenditure;
    }

    public void setTotalExpenditure(double totalExpenditure) {
        this.totalExpenditure = Math.max(0.0, totalExpenditure);
    }

    public double getReservedBills() {
        return reservedBills;
    }

    public void setReservedBills(double reservedBills) {
        this.reservedBills = Math.max(0.0, reservedBills);
    }

    public double getEmergencyReserve() {
        return emergencyReserve;
    }

    public void setEmergencyReserve(double emergencyReserve) {
        this.emergencyReserve = Math.max(0.0, emergencyReserve);
    }

    /**
     * Total remaining balance from monthly income minus expenditure.
     * Formula: Remaining balance = Monthly income − Total expenditure
     */
    public double getRemainingBalance() {
        return monthlyIncome - totalExpenditure;
    }

    /**
     * Unreserved balance available for daily discretionary spending
     * (Remaining Balance minus Reserved Bills).
     */
    public double getAvailableDiscretionaryBalance() {
        return Math.max(0.0, getRemainingBalance() - reservedBills);
    }

    @Override
    public String toString() {
        return "AccountBalance{" +
                "income=" + monthlyIncome +
                ", expenditure=" + totalExpenditure +
                ", remaining=" + getRemainingBalance() +
                ", reservedBills=" + reservedBills +
                ", emergencyReserve=" + emergencyReserve +
                '}';
    }
}
