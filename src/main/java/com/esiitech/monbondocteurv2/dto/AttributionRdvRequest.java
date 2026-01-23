package com.esiitech.monbondocteurv2.dto;

import java.time.LocalTime;

public class AttributionRdvRequest {
    private String medecinId;
    private String agendaId;
    private LocalTime heureDebut; // optionnel : si tu veux forcer une autre heure
    // getters/setters

    public String getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(String medecinId) {
        this.medecinId = medecinId;
    }

    public String getAgendaId() {
        return agendaId;
    }

    public void setAgendaId(String agendaId) {
        this.agendaId = agendaId;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }
}
