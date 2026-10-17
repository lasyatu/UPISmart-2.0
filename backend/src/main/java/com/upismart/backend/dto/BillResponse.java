package com.upismart.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BillResponse {

    private Long billId;
    private Long userId;
    private String title;
    private String category;
    private BigDecimal amount;
    private int dueDay;
    private Boolean isPaid;
    private Boolean isRecurring;
    private String billerUpi;
    private LocalDateTime lastPaidAt;
    private boolean isOverdue;

    public BillResponse() {}

    public BillResponse(Long billId, Long userId, String title, String category, BigDecimal amount,
                        int dueDay, Boolean isPaid, Boolean isRecurring, String billerUpi,
                        LocalDateTime lastPaidAt, boolean isOverdue) {
        this.billId = billId;
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.dueDay = dueDay;
        this.isPaid = isPaid;
        this.isRecurring = isRecurring;
        this.billerUpi = billerUpi;
        this.lastPaidAt = lastPaidAt;
        this.isOverdue = isOverdue;
    }

    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public int getDueDay() { return dueDay; }
    public void setDueDay(int dueDay) { this.dueDay = dueDay; }

    public Boolean getIsPaid() { return isPaid; }
    public void setIsPaid(Boolean isPaid) { this.isPaid = isPaid; }

    public Boolean getIsRecurring() { return isRecurring; }
    public void setIsRecurring(Boolean isRecurring) { this.isRecurring = isRecurring; }

    public String getBillerUpi() { return billerUpi; }
    public void setBillerUpi(String billerUpi) { this.billerUpi = billerUpi; }

    public LocalDateTime getLastPaidAt() { return lastPaidAt; }
    public void setLastPaidAt(LocalDateTime lastPaidAt) { this.lastPaidAt = lastPaidAt; }

    public boolean isOverdue() { return isOverdue; }
    public void setOverdue(boolean overdue) { isOverdue = overdue; }
}
