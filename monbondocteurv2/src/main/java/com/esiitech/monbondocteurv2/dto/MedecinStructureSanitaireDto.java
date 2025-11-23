package com.esiitech.monbondocteurv2.dto;

public class MedecinStructureSanitaireDto {
    private String id;
    private String medecinId;
    private String structureSanitaireId;
    private boolean actif;

    // Getters et setters

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

    public String getStructureSanitaireId() {
        return structureSanitaireId;
    }

    public void setStructureSanitaireId(String structureSanitaireId) {
        this.structureSanitaireId = structureSanitaireId;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}