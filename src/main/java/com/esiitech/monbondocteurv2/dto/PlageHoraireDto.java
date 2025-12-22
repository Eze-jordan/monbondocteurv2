package com.esiitech.monbondocteurv2.dto;

import com.esiitech.monbondocteurv2.model.PeriodeJournee;
import java.time.LocalTime;

public class PlageHoraireDto {

    private String id;
    private PeriodeJournee periode;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Integer nombrePatients;
    private boolean autorise;

    // =========================
    // GETTERS & SETTERS
    // =========================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PeriodeJournee getPeriode() {
        return periode;
    }

    public void setPeriode(PeriodeJournee periode) {
        this.periode = periode;
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

    public Integer getNombrePatients() {
        return nombrePatients;
    }

    public void setNombrePatients(Integer nombrePatients) {
        this.nombrePatients = nombrePatients;
    }

    public boolean isAutorise() {
        return autorise;
    }

    public void setAutorise(boolean autorise) {
        this.autorise = autorise;
    }
}
