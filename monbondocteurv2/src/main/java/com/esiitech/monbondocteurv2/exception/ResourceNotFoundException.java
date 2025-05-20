package com.esiitech.monbondocteurv2.exception;

import java.time.LocalDateTime;

public class ResourceNotFoundException extends RuntimeException {

    private String resourceName;  // Le nom de la ressource
    private String fieldName;     // Le nom du champ qui a causé l'exception
    private Object fieldValue;    // La valeur du champ qui a causé l'exception
    private LocalDateTime timestamp;  // La date et l'heure de l'erreur

    private int errorCode;  // Code d'erreur personnalisé

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue, int errorCode) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    // Constructeur avec message et cause pour la personnalisation
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue, int errorCode, Throwable cause) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue), cause);
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
