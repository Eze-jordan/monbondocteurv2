package com.esiitech.monbondocteurv2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvisDto {

    private String id;
    private String rendezVousId;
    private String utilisateurId;
    private String medecinId;
    private String structureSanitaireId;

    // Pour l'affichage
    private String nomUtilisateur;
    private String prenomUtilisateur;
    private String photoUtilisateur;
    private String nomMedecin;
    private String prenomMedecin;

    private Integer note; // 1-5
    private String commentaire;
    private boolean anonyme;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    // Statistiques calculées
    @Builder.Default
    private Double noteMoyenneMedecin = 0.0;

    @Builder.Default
    private Long nombreTotalAvisMedecin = 0L;

    @Builder.Default
    private Double noteMoyenneSpecialite = 0.0;
}