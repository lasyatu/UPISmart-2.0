package com.upismart.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SalaryUpdateRequest {

    @NotNull(message = "Monthly salary is required")
    @DecimalMin(value = "1.00", message = "Monthly salary must be at least ₹1.00")
    private BigDecimal monthlySalary;

    public SalaryUpdateRequest() {}

    public SalaryUpdateRequest(BigDecimal monthlySalary) {
        this.monthlySalary = monthlySalary;
    }

    public BigDecimal getMonthlySalary() { return monthlySalary; }
    public void setMonthlySalary(BigDecimal monthlySalary) { this.monthlySalary = monthlySalary; }
}
