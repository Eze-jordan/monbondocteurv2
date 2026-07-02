package com.esiitech.monbondocteurv2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedecinCreneauxResponse {
    private MedecinResumeDto medecin;
    private String date;
    private List<CreneauDisponibleDto> creneaux;
}