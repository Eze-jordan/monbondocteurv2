package com.esiitech.monbondocteurv2.dto;

public class DateRdvDto {
    private Long id;
    private Long agendaMedecinId;
    private Long dateRdvId;
    private Double nombrePatient;
    private Double rdvPris;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAgendaMedecinId() { return agendaMedecinId; }
    public void setAgendaMedecinId(Long agendaMedecinId) { this.agendaMedecinId = agendaMedecinId; }

    public Long getDateRdvId() { return dateRdvId; }
    public void setDateRdvId(Long dateRdvId) { this.dateRdvId = dateRdvId; }

    public Double getNombrePatient() { return nombrePatient; }
    public void setNombrePatient(Double nombrePatient) { this.nombrePatient = nombrePatient; }

    public Double getRdvPris() { return rdvPris; }
    public void setRdvPris(Double rdvPris) { this.rdvPris = rdvPris; }
}
