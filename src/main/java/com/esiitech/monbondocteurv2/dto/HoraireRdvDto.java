package com.esiitech.monbondocteurv2.dto;

import java.time.LocalTime;

public class HoraireRdvDto {
    private Long id;
    private Long dateRdvId;
    private LocalTime heureDebut;
    private LocalTime heureFin;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDateRdvId() { return dateRdvId; }
    public void setDateRdvId(Long dateRdvId) { this.dateRdvId = dateRdvId; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }
}