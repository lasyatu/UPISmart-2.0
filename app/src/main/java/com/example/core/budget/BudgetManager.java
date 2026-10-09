package com.example.core.budget;

import com.example.core.exception.InvalidAmountException;
import com.example.core.model.AccountBalance;
import com.example.core.model.Transaction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * BudgetManager encapsulates budget lifecycle, safe-to-spend calculations,
 * and maintains synchronous state with the 100% Salary-Utilization Meter.
 */
public class BudgetManager implements Serializable {

    private static final long serialVersionUID = 1L;

    private double monthlyIncome;
    private double reservedBills;
    private final List<Transaction> monthlyTransactions;

    public BudgetManager() {
        this(50000.0, 0.0);
    }

    public BudgetManager(double monthlyIncome, double reservedBills) {
        this.monthlyIncome = Math.max(0.0, monthlyIncome);
        this.reservedBills = Math.max(0.0, reservedBills);
        this.monthlyTransactions = new ArrayList<>();
    }

    public synchronized double getMonthlyIncome() {
        return monthlyIncome;
    }

    public synchronized void setMonthlyIncome(double monthlyIncome) throws InvalidAmountException {
        if (monthlyIncome < 0.0) {
            throw new InvalidAmountException(monthlyIncome, "Monthly income cannot be negative.");
        }
        this.monthlyIncome = monthlyIncome;
    }

    public synchronized double getReservedBills() {
        return reservedBills;
    }

    public synchronized void setReservedBills(double reservedBills) {
        this.reservedBills = Math.max(0.0, reservedBills);
    }

    public synchronized void setTransactions(List<Transaction> transactions) {
        this.monthlyTransactions.clear();
        if (transactions != null) {
            this.monthlyTransactions.addAll(transactions);
        }
    }

    public synchronized void recordTransaction(Transaction tx) {
        if (tx != null && "SUCCESS".equalsIgnoreCase(tx.getStatus())) {
            this.monthlyTransactions.add(tx);
        }
    }

    public synchronized List<Transaction> getTransactions() {
        return Collections.unmodifiableList(new ArrayList<>(monthlyTransactions));
    }

    /**
     * Computes the current 100% SalaryUtilizationMeter.
     */
    public synchronized SalaryUtilizationMeter getUtilizationMeter() {
        return SalaryUtilizationMeter.compute(monthlyIncome, monthlyTransactions);
    }

    /**
     * Calculates the remaining balance available for discretionary daily spending.
     * Disposable balance = Remaining Balance - Reserved Bills
     */
    public synchronized double getUnreservedDisposableBalance() {
        SalaryUtilizationMeter meter = getUtilizationMeter();
        double remaining = meter.getRemainingBalance();
        return Math.max(0.0, remaining - reservedBills);
    }

    /**
     * Calculates Safe-to-Spend per day using the formula:
     * Safe-to-Spend = max(0, Unreserved Disposable Balance / remainingDays)
     */
    public synchronized double calculateSafeToSpendPerDay(int remainingDaysInCycle) {
        if (remainingDaysInCycle <= 0) {
            return 0.0;
        }
        double disposable = getUnreservedDisposableBalance();
        if (disposable <= 0.0) {
            return 0.0;
        }
        BigDecimal dispBD = BigDecimal.valueOf(disposable);
        BigDecimal daysBD = BigDecimal.valueOf(remainingDaysInCycle);
        return dispBD.divide(daysBD, 2, RoundingMode.HALF_EVEN).doubleValue();
    }

    /**
     * Returns an immutable snapshot of current account balance state.
     */
    public synchronized AccountBalance getAccountBalanceSnapshot(double emergencyReserve) {
        SalaryUtilizationMeter meter = getUtilizationMeter();
        return new AccountBalance(
                monthlyIncome,
                meter.getTotalExpenditure(),
                reservedBills,
                emergencyReserve
        );
    }
}
