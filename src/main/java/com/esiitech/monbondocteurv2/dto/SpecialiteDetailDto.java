package com.esiitech.monbondocteurv2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialiteDetailDto {

    // Informations de base
    private String specialite;           // nom de la spécialité
    private String nomSpecialite;        // alias pour compatibilité
    private String structureNom;         // nom de la structure
    private int nombreMedecins;          // nombre de médecins dans cette spécialité
    private double noteMoyenne;          // note moyenne de la spécialité

    // Statistiques
    private long totalAvis;              // total des avis pour cette spécialité
    private long nombreTotalRdv;         // total des RDV pour cette spécialité
    private double tauxOccupation;       // taux d'occupation (%)
    private long delaiMoyenAttenteJours; // délai moyen d'attente en jours

    // Disponibilité
    private LocalDateTime prochainCreneau;        // prochain créneau disponible
    private int creneauxDisponiblesSemaine;       // nombre de créneaux disponibles cette semaine
    private int medecinsDisponiblesAujourdhui;    // nombre de médecins disponibles aujourd'hui

    // Classement
    private int rang;                    // classement de la spécialité
    private boolean estMeilleure;        // est la meilleure spécialité de la structure

    // Médecins associés
    private List<MedecinSpecialiteDetailDto> medecins;

    // ==================== SOUS-DTO POUR LES MÉDECINS ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedecinSpecialiteDetailDto {
        private String id;
        private String nom;
        private String prenom;
        private String photoPath;
        private String grade;                     // grade du médecin
        private double noteMoyenne;               // note moyenne du médecin
        private long nombreAvis;                  // nombre d'avis du médecin
        private long nombreRdvEffectues;          // nombre de RDV effectués
        private LocalDateTime prochaineDisponibilite; // prochaine disponibilité
        private boolean disponibleAujourdhui;     // disponible aujourd'hui
        private List<CreneauDto> creneauxDisponibles; // créneaux disponibles

        // Pour compatibilité avec l'ancien code
        private double note;                      // alias pour noteMoyenne
    }

    // ==================== SOUS-DTO POUR LES CRÉNEAUX ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreneauDto {
        private String date;      // date du créneau (YYYY-MM-DD)
        private String heure;     // heure du créneau (HH:MM)
    }

    // ==================== MÉTHODES DE CONVERSION POUR COMPATIBILITÉ ====================

    /**
     * Retourne le nom de la spécialité (getter de compatibilité)
     */
    public String getNomSpecialite() {
        return specialite != null ? specialite : nomSpecialite;
    }

    /**
     * Définit le nom de la spécialité (setter de compatibilité)
     */
    public void setNomSpecialite(String nomSpecialite) {
        this.specialite = nomSpecialite;
        this.nomSpecialite = nomSpecialite;
    }

    /**
     * Retourne le nom de la spécialité
     */
    public String getSpecialite() {
        return specialite;
    }

    /**
     * Définit le nom de la spécialité
     */
    public void setSpecialite(String specialite) {
        this.specialite = specialite;
        this.nomSpecialite = specialite;
    }
}