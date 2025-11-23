package com.esiitech.monbondocteurv2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "paiements")
public class Paiements {
    @Id
    @Column(name = "id", nullable = false,length = 100,updatable = false)
    private String  id;
    @ManyToOne
    @JoinColumn(name = "formules-id")
    private Formules formules;
    @ManyToOne
    @JoinColumn(name = "structureSanitaire-id")
    private StructureSanitaire structureSanitaire;
    @Column(name = "reference")
    private String reference;
    @Column(name = "service-paiement")
    private String servicePaiement;
    @Column(name = "compte-debite")
    private String compteDebite;
    @Column(name = "montant-paye")
    private String montantPaye;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Formules getFormules() {
        return formules;
    }

    public void setFormules(Formules formules) {
        this.formules = formules;
    }

    public StructureSanitaire getStructureSanitaire() {
        return structureSanitaire;
    }

    public void setStructureSanitaire(StructureSanitaire structureSanitaire) {
        this.structureSanitaire = structureSanitaire;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getServicePaiement() {
        return servicePaiement;
    }

    public void setServicePaiement(String servicePaiement) {
        this.servicePaiement = servicePaiement;
    }

    public String getCompteDebite() {
        return compteDebite;
    }

    public void setCompteDebite(String compteDebite) {
        this.compteDebite = compteDebite;
    }

    public String getMontantPaye() {
        return montantPaye;
    }

    public void setMontantPaye(String montantPaye) {
        this.montantPaye = montantPaye;
    }
}
