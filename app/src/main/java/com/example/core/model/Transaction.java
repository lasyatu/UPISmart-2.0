package com.example.core.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Encapsulated Core Java domain model representing a financial transaction.
 * Demonstrates OOP data encapsulation with private fields, defensive validation,
 * and standard accessor methods.
 */
public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String recipientUpiId;
    private String recipientName;
    private double amount;
    private String category;
    private long timestamp;
    private String status;
    private String referenceId;
    private String note;
    private boolean isSimulated;

    public Transaction() {
        this.timestamp = System.currentTimeMillis();
        this.status = "SUCCESS";
        this.isSimulated = true;
    }

    public Transaction(Long id, String title, String recipientUpiId, String recipientName,
                       double amount, String category, long timestamp, String status,
                       String referenceId, String note, boolean isSimulated) {
        this.id = id;
        this.title = title != null ? title.trim() : "Direct Payment";
        this.recipientUpiId = recipientUpiId != null ? recipientUpiId.trim() : "";
        this.recipientName = recipientName != null ? recipientName.trim() : "Payee";
        this.amount = amount;
        this.category = category != null ? category.trim() : "Other";
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
        this.status = status != null ? status : "SUCCESS";
        this.referenceId = referenceId != null ? referenceId : "";
        this.note = note != null ? note : "";
        this.isSimulated = isSimulated;
    }

    // Getters and Setters demonstrating encapsulation
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRecipientUpiId() {
        return recipientUpiId;
    }

    public void setRecipientUpiId(String recipientUpiId) {
        this.recipientUpiId = recipientUpiId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isSimulated() {
        return isSimulated;
    }

    public void setSimulated(boolean simulated) {
        isSimulated = simulated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Double.compare(that.amount, amount) == 0 &&
                timestamp == that.timestamp &&
                Objects.equals(recipientUpiId, that.recipientUpiId) &&
                Objects.equals(referenceId, that.referenceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipientUpiId, amount, timestamp, referenceId);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", recipientUpiId='" + recipientUpiId + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", referenceId='" + referenceId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
