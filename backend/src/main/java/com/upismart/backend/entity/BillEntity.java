package com.upismart.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity representing a recurring or scheduled bill obligation.
 */
@Entity
@Table(name = "bills")
public class BillEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_id")
    private Long billId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "due_day", nullable = false)
    private int dueDay; // Day of month (1-31)

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = true;

    @Column(name = "biller_upi", length = 100)
    private String billerUpi = "billing@demo.upismart";

    @Column(name = "last_paid_at")
    private LocalDateTime lastPaidAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public BillEntity() {}

    public BillEntity(Long userId, String title, String category, BigDecimal amount, int dueDay, Boolean isPaid) {
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.dueDay = dueDay;
        this.isPaid = isPaid != null ? isPaid : false;
        this.createdAt = LocalDateTime.now();
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
