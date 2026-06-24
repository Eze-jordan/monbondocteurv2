package com.esiitech.monbondocteurv2.dto;

import java.math.BigDecimal;

public class PaymentProviderInitRequest {

    private BigDecimal amount;
    private String reference;
    private String customerAccountNumber;

    public PaymentProviderInitRequest() {
    }

    public PaymentProviderInitRequest(BigDecimal amount, String reference, String customerAccountNumber) {
        this.amount = amount;
        this.reference = reference;
        this.customerAccountNumber = customerAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getCustomerAccountNumber() {
        return customerAccountNumber;
    }

    public void setCustomerAccountNumber(String customerAccountNumber) {
        this.customerAccountNumber = customerAccountNumber;
    }
}