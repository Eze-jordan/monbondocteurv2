// com.esiitech.monbondocteurv2.dto.ResetPasswordRequest.java
package com.esiitech.monbondocteurv2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {

    @NotBlank
    @Size(min = 8, max = 128)
    private String nouveauMotDePasse;

    @NotBlank
    private String confirmerMotDePasse;

    public @NotBlank @Size(min = 8, max = 128) String getNouveauMotDePasse() {
        return nouveauMotDePasse;
    }

    public void setNouveauMotDePasse(@NotBlank @Size(min = 8, max = 128) String nouveauMotDePasse) {
        this.nouveauMotDePasse = nouveauMotDePasse;
    }

    public @NotBlank String getConfirmerMotDePasse() {
        return confirmerMotDePasse;
    }

    public void setConfirmerMotDePasse(@NotBlank String confirmerMotDePasse) {
        this.confirmerMotDePasse = confirmerMotDePasse;
    }
}
