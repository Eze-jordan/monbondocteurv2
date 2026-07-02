package com.esiitech.monbondocteurv2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private long totalSpecialites;
    private long totalMedecins;
    private long totalStructures;
    private long totalUtilisateurs;
    private long totalRendezVous;
    private long totalAvis;
}