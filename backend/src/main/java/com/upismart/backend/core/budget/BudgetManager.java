package com.upismart.backend.core.budget;

import com.upismart.backend.core.exception.InvalidAmountException;
import com.upismart.backend.core.model.AccountBalance;
import com.upismart.backend.core.model.Transaction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Encapsulated Budget Manager providing single source of truth for:
 * 1. Income updates and validations
 * 2. Safe-to-spend daily calculations
 * 3. Aggregation across category buckets
 */
public class BudgetManager implements Serializable {

    private static final long serialVersionUID = 1L;

    private double monthlySalary;
    private double reservedBills;
    private double emergencyReserve;

    public BudgetManager(double monthlySalary, double reservedBills, double emergencyReserve) {
        this.monthlySalary = Math.max(0.0, monthlySalary);
        this.reservedBills = Math.max(0.0, reservedBills);
        this.emergencyReserve = Math.max(0.0, emergencyReserve);
    }

    public synchronized void updateSalary(double newSalary) throws InvalidAmountException {
        if (newSalary < 0.0 || Double.isNaN(newSalary) || Double.isInfinite(newSalary)) {
            throw new InvalidAmountException("Monthly salary must be a positive finite number.", newSalary);
        }
        this.monthlySalary = newSalary;
    }

    public synchronized AccountBalance calculateAccountBalance(double totalExpenditure) {
        return new AccountBalance(monthlySalary, totalExpenditure, reservedBills, emergencyReserve);
    }

    public synchronized SalaryUtilizationMeter computeUtilization(List<Transaction> transactions) {
        return SalaryUtilizationMeter.calculate(monthlySalary, transactions, reservedBills);
    }

    public synchronized double calculateDailySafeToSpend(double currentExpenditure, int daysRemainingInMonth) {
        if (daysRemainingInMonth <= 0) daysRemainingInMonth = 1;
        double unreservedPool = monthlySalary - (reservedBills + emergencyReserve);
        double remainingPool = unreservedPool - currentExpenditure;
        if (remainingPool <= 0.0) return 0.0;
        BigDecimal pool = BigDecimal.valueOf(remainingPool);
        BigDecimal days = BigDecimal.valueOf(daysRemainingInMonth);
        return pool.divide(days, 2, RoundingMode.HALF_UP).doubleValue();
    }

    public double getMonthlySalary() {
        return monthlySalary;
    }

    public double getReservedBills() {
        return reservedBills;
    }

    public double getEmergencyReserve() {
        return emergencyReserve;
    }

    public void setReservedBills(double reservedBills) {
        this.reservedBills = Math.max(0.0, reservedBills);
    }

    public void setEmergencyReserve(double emergencyReserve) {
        this.emergencyReserve = Math.max(0.0, emergencyReserve);
    }
}
