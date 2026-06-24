package com.esiitech.monbondocteurv2.dto;

public class PaymentFormuleInitRequest {

    private String structureId;
    private String formuleId;
    private String appId;
    private String operatorName;
    private String customerAccountNumber;

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
}