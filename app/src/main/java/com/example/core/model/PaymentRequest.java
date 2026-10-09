package com.example.core.model;

import java.io.Serializable;

/**
 * Encapsulates the parameters required to execute a simulated payment.
 */
public class PaymentRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String recipientUpiId;
    private String recipientName;
    private double amount;
    private String category;
    private String note;
    private String demoPin;
    private boolean isQrScanned;

    public PaymentRequest(String recipientUpiId, String recipientName, double amount,
                          String category, String note, String demoPin) {
        this(recipientUpiId, recipientName, amount, category, note, demoPin, false);
    }

    public PaymentRequest(String recipientUpiId, String recipientName, double amount,
                          String category, String note, String demoPin, boolean isQrScanned) {
        this.recipientUpiId = recipientUpiId != null ? recipientUpiId.trim() : "";
        this.recipientName = recipientName != null ? recipientName.trim() : "Payee";
        this.amount = amount;
        this.category = category != null && !category.trim().isEmpty() ? category.trim() : "Food";
        this.note = note != null ? note.trim() : "";
        this.demoPin = demoPin != null ? demoPin.trim() : "";
        this.isQrScanned = isQrScanned;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDemoPin() {
        return demoPin;
    }

    public void setDemoPin(String demoPin) {
        this.demoPin = demoPin;
    }

    public boolean isQrScanned() {
        return isQrScanned;
    }

    public void setQrScanned(boolean qrScanned) {
        isQrScanned = qrScanned;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "to='" + recipientUpiId + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                '}';
    }
}
