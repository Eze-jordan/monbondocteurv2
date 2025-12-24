package com.esiitech.monbondocteurv2.dto;

import java.util.List;

public class AgendaSemaineRequest {

    private String medecinId;
    private String structureSanitaireId;
    private List<AgendaMedecinDto> agendas;

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

    public List<AgendaMedecinDto> getAgendas() {
        return agendas;
    }

    public void setAgendas(List<AgendaMedecinDto> agendas) {
        this.agendas = agendas;
    }
}
