package com.esiitech.monbondocteurv2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DisponibiliteConflitException extends RuntimeException {
    public DisponibiliteConflitException(String message) {
        super(message);
    }
}
