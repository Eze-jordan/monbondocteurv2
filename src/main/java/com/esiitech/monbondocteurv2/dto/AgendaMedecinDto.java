package com.esiitech.monbondocteurv2.dto;

import com.esiitech.monbondocteurv2.model.JourSemaine;

import java.util.List;

public class AgendaMedecinDto {

    private String id;
    private String medecinId;
    private JourSemaine jour;
    private boolean autorise;
    private String structureSanitaireId;
    private List<PlageHoraireDto> plages;

    // getters / setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(String medecinId) {
        this.medecinId = medecinId;
    }

    public JourSemaine getJour() {
        return jour;
    }

    public void setJour(JourSemaine jour) {
        this.jour = jour;
    }

    public boolean isAutorise() {
        return autorise;
    }

    public void setAutorise(boolean autorise) {
        this.autorise = autorise;
    }

    public List<PlageHoraireDto> getPlages() {
        return plages;
    }

    public void setPlages(List<PlageHoraireDto> plages) {
        this.plages = plages;
    }

    public String getStructureSanitaireId() {
        return structureSanitaireId;
    }

    public void setStructureSanitaireId(String structureSanitaireId) {
        this.structureSanitaireId = structureSanitaireId;
    }
}
