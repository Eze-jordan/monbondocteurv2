package com.esiitech.monbondocteurv2.dto;

import lombok.Data;

@Data
public class ActivationRequest {
    private String code;
    private String motDePasse;
    private String confirmationMotDePasse;
}
