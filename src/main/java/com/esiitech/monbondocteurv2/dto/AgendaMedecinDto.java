package com.esiitech.monbondocteurv2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalTime;

public class AgendaMedecinDto {
    private Long id;
    private Long medecinId;
    private LocalDate date;
    private Double nombrePatient;
    private Double rdvPris;
    private LocalTime heureDebut;

    private LocalTime heureFin;
    private boolean actif;

    // ✅ Champ calculé dynamiquement
    @JsonProperty("rdvRestant")
    public Double getRdvRestant() {
        if (nombrePatient != null && rdvPris != null) {
            return nombrePatient - rdvPris;
        }
        return null;
    }
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getNombrePatient() {
        return nombrePatient;
    }

    public void setNombrePatient(Double nombrePatient) {
        this.nombrePatient = nombrePatient;
    }

    public Double getRdvPris() {
        return rdvPris;
    }

    public void setRdvPris(Double rdvPris) {
        this.rdvPris = rdvPris;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(Long medecinId) {
        this.medecinId = medecinId;
    }


    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
