package com.upismart.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tx_id")
    private Long txId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reference_id", nullable = false, unique = true, length = 64)
    private String referenceId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "recipient_upi", nullable = false, length = 100)
    private String recipientUpi;

    @Column(name = "recipient_name", nullable = false, length = 150)
    private String recipientName;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "is_simulated", nullable = false)
    private Boolean isSimulated;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public TransactionEntity() {}

    public TransactionEntity(Long userId, String referenceId, String title, String recipientUpi,
                             String recipientName, BigDecimal amount, String category,
                             String status, String note, Boolean isSimulated) {
        this.userId = userId;
        this.referenceId = referenceId;
        this.title = title;
        this.recipientUpi = recipientUpi;
        this.recipientName = recipientName;
        this.amount = amount;
        this.category = category;
        this.status = status;
        this.note = note != null ? note : "";
        this.isSimulated = isSimulated != null ? isSimulated : true;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getTxId() { return txId; }
    public void setTxId(Long txId) { this.txId = txId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getRecipientUpi() { return recipientUpi; }
    public void setRecipientUpi(String recipientUpi) { this.recipientUpi = recipientUpi; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Boolean getIsSimulated() { return isSimulated; }
    public void setIsSimulated(Boolean isSimulated) { this.isSimulated = isSimulated; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
