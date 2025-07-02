package com.esiitech.monbondocteurv2.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "agenda_medecin")
public class AgendaMedecin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(cascade = CascadeType.PERSIST)
    private Medecin medecin;
    private LocalDate date;
    private Double nombrePatient;
    private Double rdvPris;
    private LocalTime heureDebut;

    private LocalTime heureFin;
    @Column(nullable = false)
    private boolean actif = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Medecin getMedecin() {
        return medecin;
    }

    public void setMedecin(Medecin medecin) {
        this.medecin = medecin;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
