package com.esiitech.monbondocteurv2.dto;

import java.time.OffsetDateTime;

public class ApiErrorResponse {

    private int statusCode;
    private String errorCode;
    private String message;
    private boolean renewalRequired;
    private OffsetDateTime timestamp;
    private String path;

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(
            int statusCode,
            String errorCode,
            String message,
            boolean renewalRequired,
            OffsetDateTime timestamp,
            String path
    ) {
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.message = message;
        this.renewalRequired = renewalRequired;
        this.timestamp = timestamp;
        this.path = path;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRenewalRequired() {
        return renewalRequired;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }
}