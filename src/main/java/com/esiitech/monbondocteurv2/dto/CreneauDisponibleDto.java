package com.esiitech.monbondocteurv2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreneauDisponibleDto {
    private String heure;
    private String heureFin;
    private boolean disponible;
    private int placesRestantes;
    private String periode;
    private String plageId;
}