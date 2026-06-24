package com.esiitech.monbondocteurv2.dto;

import java.math.BigDecimal;

public class PaymentFormuleResponse {

    private String paymentId;
    private String structureId;
    private String formuleId;
    private String reference;
    private String status;
    private BigDecimal amount;
    private String externalTransactionId;
    private String message;

    public PaymentFormuleResponse() {
    }

    public PaymentFormuleResponse(
            String paymentId,
            String structureId,
            String formuleId,
            String reference,
            String status,
            BigDecimal amount,
            String externalTransactionId,
            String message
    ) {
        this.paymentId = paymentId;
        this.structureId = structureId;
        this.formuleId = formuleId;
        this.reference = reference;
        this.status = status;
        this.amount = amount;
        this.externalTransactionId = externalTransactionId;
        this.message = message;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getStructureId() {
        return structureId;
    }

    public void setStructureId(String structureId) {
        this.structureId = structureId;
    }

    public String getFormuleId() {
        return formuleId;
    }

    public void setFormuleId(String formuleId) {
        this.formuleId = formuleId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}