package com.upismart.backend.core.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Encapsulated Core Java domain model representing a financial transaction.
 * Demonstrates OOP data encapsulation with private fields, defensive validation,
 * immutability conventions, and standard object methods.
 */
public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String title;
    private final String recipientUpi;
    private final String recipientName;
    private final double amount;
    private final String category;
    private final long timestamp;
    private final String status;
    private final String note;
    private final boolean isSimulated;

    public Transaction(String id, String title, String recipientUpi, String recipientName,
                       double amount, String category, long timestamp, String status,
                       String note, boolean isSimulated) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID cannot be null or empty.");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Transaction amount cannot be negative.");
        }
        this.id = id;
        this.title = title != null ? title.trim() : "Simulated UPI Transfer";
        this.recipientUpi = recipientUpi != null ? recipientUpi.trim() : "";
        this.recipientName = recipientName != null ? recipientName.trim() : "Recipient";
        this.amount = amount;
        this.category = category != null && !category.trim().isEmpty() ? category.trim() : "Other";
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
        this.status = status != null ? status : "SUCCESS";
        this.note = note != null ? note : "";
        this.isSimulated = isSimulated;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getRecipientUpi() {
        return recipientUpi;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public boolean isSimulated() {
        return isSimulated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", recipientUpi='" + recipientUpi + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                ", isSimulated=" + isSimulated +
                '}';
    }
}
