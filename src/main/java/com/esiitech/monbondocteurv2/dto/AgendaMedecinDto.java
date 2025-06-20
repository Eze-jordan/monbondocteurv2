package com.esiitech.monbondocteurv2.dto;

public class AgendaMedecinDto {
    private Long id;
    private Long medecinId;
    private Long structureSanitaireId;
    private boolean actif;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(Long medecinId) {
        this.medecinId = medecinId;
    }

    public Long getStructureSanitaireId() {
        return structureSanitaireId;
    }

    public void setStructureSanitaireId(Long structureSanitaireId) {
        this.structureSanitaireId = structureSanitaireId;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
