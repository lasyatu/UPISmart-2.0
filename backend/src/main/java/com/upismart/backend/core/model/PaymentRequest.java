package com.upismart.backend.core.model;

import java.io.Serializable;

/**
 * Encapsulates the parameters required to execute a simulated payment.
 */
public class PaymentRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String recipientUpi;
    private final String recipientName;
    private final double amount;
    private final String category;
    private final String enteredPin;
    private final String note;
    private final boolean userConfirmed;

    public PaymentRequest(String recipientUpi, String recipientName, double amount,
                          String category, String enteredPin, String note, boolean userConfirmed) {
        this.recipientUpi = recipientUpi != null ? recipientUpi.trim() : "";
        this.recipientName = recipientName != null ? recipientName.trim() : "";
        this.amount = amount;
        this.category = category != null ? category.trim() : "Other";
        this.enteredPin = enteredPin != null ? enteredPin.trim() : "";
        this.note = note != null ? note.trim() : "";
        this.userConfirmed = userConfirmed;
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

    public String getEnteredPin() {
        return enteredPin;
    }

    public String getNote() {
        return note;
    }

    public boolean isUserConfirmed() {
        return userConfirmed;
    }
}
