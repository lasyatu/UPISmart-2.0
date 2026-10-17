package com.upismart.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class BillRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least ₹1.00")
    private BigDecimal amount;

    @Min(value = 1, message = "Due day must be between 1 and 31")
    @Max(value = 31, message = "Due day must be between 1 and 31")
    private int dueDay;

    private String billerUpi = "billing@demo.upismart";

    public BillRequest() {}

    public BillRequest(String title, String category, BigDecimal amount, int dueDay, String billerUpi) {
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.dueDay = dueDay;
        this.billerUpi = billerUpi != null ? billerUpi : "billing@demo.upismart";
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public int getDueDay() { return dueDay; }
    public void setDueDay(int dueDay) { this.dueDay = dueDay; }

    public String getBillerUpi() { return billerUpi; }
    public void setBillerUpi(String billerUpi) { this.billerUpi = billerUpi; }
}
