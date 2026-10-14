package com.upismart.backend.dto;

import java.math.BigDecimal;

public class PaymentApiResponse {

    private boolean success;
    private String referenceId;
    private String message;
    private BigDecimal amount;
    private String recipientUpi;
    private String recipientName;
    private String category;
    private BigDecimal remainingSafeToSpend;
    private long timestamp;

    public PaymentApiResponse() {}

    public PaymentApiResponse(boolean success, String referenceId, String message,
                              BigDecimal amount, String recipientUpi, String recipientName,
                              String category, BigDecimal remainingSafeToSpend, long timestamp) {
        this.success = success;
        this.referenceId = referenceId;
        this.message = message;
        this.amount = amount;
        this.recipientUpi = recipientUpi;
        this.recipientName = recipientName;
        this.category = category;
        this.remainingSafeToSpend = remainingSafeToSpend;
        this.timestamp = timestamp;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getRecipientUpi() { return recipientUpi; }
    public void setRecipientUpi(String recipientUpi) { this.recipientUpi = recipientUpi; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getRemainingSafeToSpend() { return remainingSafeToSpend; }
    public void setRemainingSafeToSpend(BigDecimal remainingSafeToSpend) { this.remainingSafeToSpend = remainingSafeToSpend; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
