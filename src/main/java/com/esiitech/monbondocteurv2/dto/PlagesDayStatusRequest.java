package com.esiitech.monbondocteurv2.dto;

import com.esiitech.monbondocteurv2.model.JourSemaine;

public class PlagesDayStatusRequest {
    private String medecinId;
    private String structureSanitaireId;
    private JourSemaine jour;   // LUNDI, MARDI, ...
    private boolean autorise;   // true ou false

    public String getMedecinId() { return medecinId; }
    public void setMedecinId(String medecinId) { this.medecinId = medecinId; }

    public String getStructureSanitaireId() { return structureSanitaireId; }
    public void setStructureSanitaireId(String structureSanitaireId) { this.structureSanitaireId = structureSanitaireId; }

    public JourSemaine getJour() { return jour; }
    public void setJour(JourSemaine jour) { this.jour = jour; }

    public boolean isAutorise() { return autorise; }
    public void setAutorise(boolean autorise) { this.autorise = autorise; }
}
