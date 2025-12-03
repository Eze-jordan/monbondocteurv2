package com.esiitech.monbondocteurv2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MedecinNonTrouveException extends RuntimeException {
    public MedecinNonTrouveException(String message) {
        super(message);
    }
}
