package com.esiitech.monbondocteurv2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesAvisDto {

    private String structureId;
    private String nomStructure;

    // Meilleure spécialité
    private String meilleureSpecialite;
    private Double noteMeilleureSpecialite;

    // Notes par spécialité (classées par ordre décroissant)
    private Map<String, Double> notesParSpecialite;
    private Map<String, Long> nombreAvisParSpecialite;

    // Pour le détail d'une spécialité
    @Builder.Default
    private Double noteMoyenneSpecialite = 0.0;

    @Builder.Default
    private Long totalAvisSpecialite = 0L;
}