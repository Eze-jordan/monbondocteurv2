package com.esiitech.monbondocteurv2.exception;

import java.time.Instant;

public class ErrorDetails {

    private int statusCode;
    private String message;
    private String timestamp;

    public ErrorDetails(int statusCode, String message, String path) {
        this.statusCode = statusCode;
        this.message = message;
        this.timestamp = Instant.now().toString();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

}
