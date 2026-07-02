package com.esiitech.monbondocteurv2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructureSpecialitesStatsDto {

    // Infos structure
    private String structureId;
    private String nomStructure;

    // Stats globales
    private int nombreTotalSpecialites;
    private int nombreTotalMedecins;
    private long nombreTotalRdv;

    // Spécialités
    private String meilleureSpecialite;
    private List<SpecialiteDetailDto> specialites;

    // Résumé par spécialité (pour graphiques)
    private Map<String, Long> rdvParSpecialite;
    private Map<String, Integer> medecinsParSpecialite;
}