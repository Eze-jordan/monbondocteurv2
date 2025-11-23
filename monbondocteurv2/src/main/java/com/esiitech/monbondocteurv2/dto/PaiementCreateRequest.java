// com.esiitech.monbondocteurv2.dto.PaiementCreateRequest.java
package com.esiitech.monbondocteurv2.dto;

public class PaiementCreateRequest {
    private String structureId;
    private String formulesId;
    private String reference;
    private String servicePaiement; // ex: "MobileMoney", "Carte", ...
    private String compteDebite;    // MSISDN, carte masquée, etc.
    private String montantPaye;     // tu utilises String dans l’entity

    // Getters / Setters
    public String getStructureId() { return structureId; }
    public void setStructureId(String structureId) { this.structureId = structureId; }
    public String getFormulesId() { return formulesId; }
    public void setFormulesId(String formulesId) { this.formulesId = formulesId; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getServicePaiement() { return servicePaiement; }
    public void setServicePaiement(String servicePaiement) { this.servicePaiement = servicePaiement; }
    public String getCompteDebite() { return compteDebite; }
    public void setCompteDebite(String compteDebite) { this.compteDebite = compteDebite; }
    public String getMontantPaye() { return montantPaye; }
    public void setMontantPaye(String montantPaye) { this.montantPaye = montantPaye; }


}
