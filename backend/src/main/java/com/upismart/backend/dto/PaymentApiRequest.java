package com.upismart.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PaymentApiRequest {

    @NotBlank(message = "Recipient UPI handle is required")
    private String recipientUpi;

    private String recipientName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least ₹0.01")
    private BigDecimal amount;

    private String category;
    private String enteredPin;
    private String note;
    private Boolean userConfirmed;

    public PaymentApiRequest() {}

    public PaymentApiRequest(String recipientUpi, String recipientName, BigDecimal amount,
                             String category, String enteredPin, String note, Boolean userConfirmed) {
        this.recipientUpi = recipientUpi;
        this.recipientName = recipientName;
        this.amount = amount;
        this.category = category;
        this.enteredPin = enteredPin;
        this.note = note;
        this.userConfirmed = userConfirmed;
    }

    public String getRecipientUpi() { return recipientUpi; }
    public void setRecipientUpi(String recipientUpi) { this.recipientUpi = recipientUpi; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getEnteredPin() { return enteredPin; }
    public void setEnteredPin(String enteredPin) { this.enteredPin = enteredPin; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Boolean getUserConfirmed() { return userConfirmed; }
    public void setUserConfirmed(Boolean userConfirmed) { this.userConfirmed = userConfirmed; }
}
