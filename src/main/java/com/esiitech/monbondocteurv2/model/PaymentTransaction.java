package com.esiitech.monbondocteurv2.model;

import com.esiitech.monbondocteurv2.enums.PaymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {

    @Id
    @Column(name = "payment_id", nullable = false, length = 100)
    private String paymentId;

    @Column(name = "structure_id", nullable = false, length = 100)
    private String structureId;

    @Column(name = "formule_id", nullable = false, length = 100)
    private String formuleId;

    @Column(name = "reference", nullable = false, unique = true, length = 16)
    private String reference;

    @Column(name = "app_id", nullable = false)
    private String appId;

    @Column(name = "operator_name", nullable = false)
    private String operatorName;

    @Column(name = "customer_account_number", nullable = false)
    private String customerAccountNumber;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "external_transaction_id")
    private String externalTransactionId;

    @Column(name = "provider_message", columnDefinition = "TEXT")
    private String providerMessage;

    @Column(name = "callback_raw_status")
    private String callbackRawStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        if (this.createdAt == null) {
            this.createdAt = now;
        }

        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();

        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
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

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getCustomerAccountNumber() {
        return customerAccountNumber;
    }

    public void setCustomerAccountNumber(String customerAccountNumber) {
        this.customerAccountNumber = customerAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public String getProviderMessage() {
        return providerMessage;
    }

    public void setProviderMessage(String providerMessage) {
        this.providerMessage = providerMessage;
    }

    public String getCallbackRawStatus() {
        return callbackRawStatus;
    }

    public void setCallbackRawStatus(String callbackRawStatus) {
        this.callbackRawStatus = callbackRawStatus;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


}