package com.esiitech.monbondocteurv2.dto;

import com.esiitech.monbondocteurv2.model.AgendaUpdatePolicy;

import java.time.LocalDate;
import java.util.List;

public class AgendaSemainePlanifieeRequest {
    private String medecinId;
    private String structureSanitaireId;

    private LocalDate weekStart; // lundi de la semaine où tu veux appliquer
    private AgendaUpdatePolicy policy;

    private List<AgendaMedecinDto> agendas; // 7 jours


    public String getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(String medecinId) {
        this.medecinId = medecinId;
    }

    public String getStructureSanitaireId() {
        return structureSanitaireId;
    }

    public void setStructureSanitaireId(String structureSanitaireId) {
        this.structureSanitaireId = structureSanitaireId;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(LocalDate weekStart) {
        this.weekStart = weekStart;
    }

    public AgendaUpdatePolicy getPolicy() {
        return policy;
    }

    public void setPolicy(AgendaUpdatePolicy policy) {
        this.policy = policy;
    }

    public List<AgendaMedecinDto> getAgendas() {
        return agendas;
    }

    public void setAgendas(List<AgendaMedecinDto> agendas) {
        this.agendas = agendas;
    }
}

