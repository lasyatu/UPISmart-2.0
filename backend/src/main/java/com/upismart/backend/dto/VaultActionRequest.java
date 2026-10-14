package com.upismart.backend.dto;

import java.math.BigDecimal;

public class VaultActionRequest {

    private BigDecimal amount;
    private String pin;

    public VaultActionRequest() {}

    public VaultActionRequest(BigDecimal amount, String pin) {
        this.amount = amount;
        this.pin = pin;
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
}
