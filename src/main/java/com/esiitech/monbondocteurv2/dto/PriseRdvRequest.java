package com.esiitech.monbondocteurv2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class PriseRdvRequest {
    @NotNull private String medecinId;
    @NotNull private LocalDate date;
    @NotNull private LocalTime heureDebut;
    private String structureId;
    @NotBlank private String patientNom;
    @NotBlank private String patientPrenom;
    @Email private String patientEmail;
    private String patientTelephone;
    private Integer patientAge;
    private String patientGenre;
    private String patientAdresse;
    private String motif;

    @NotBlank private String lienParente; // MOI, ENFANT, PARENT, CONJOINT, AUTRE
}