package com.esiitech.monbondocteurv2.dto;

import java.time.LocalDate;

public class DateRdvDto {
    private Long id;
    private Long agendaMedecinId;
    private LocalDate date;
    private Double nombrePatient;
    private Double rdvPris;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAgendaMedecinId() { return agendaMedecinId; }
    public void setAgendaMedecinId(Long agendaMedecinId) { this.agendaMedecinId = agendaMedecinId; }

    public LocalDate getDate() {
        return date;}

    public void setDate(LocalDate date) {
    this.date = date;
    }

    public Double getNombrePatient() { return nombrePatient; }
    public void setNombrePatient(Double nombrePatient) { this.nombrePatient = nombrePatient; }

    public Double getRdvPris() { return rdvPris; }
    public void setRdvPris(Double rdvPris) { this.rdvPris = rdvPris; }
}
