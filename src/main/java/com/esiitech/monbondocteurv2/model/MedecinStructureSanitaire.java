package com.esiitech.monbondocteurv2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "medecin_structure_sanitaire")
public class MedecinStructureSanitaire {
    @Id
    @Column(name = "id", nullable = false,length = 100,updatable = false)
    private String  id;
    @ManyToOne(cascade = CascadeType.PERSIST)
    private StructureSanitaire structureSanitaire;
    @ManyToOne(cascade = CascadeType.PERSIST)
    private Medecin medecin;
    @Column(nullable = false)
    private boolean actif = false;

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public Medecin getMedecin() {
        return medecin;
    }

    public void setMedecin(Medecin medecin) {
        this.medecin = medecin;
    }

    public StructureSanitaire getStructureSanitaire() {
        return structureSanitaire;
    }

    public void setStructureSanitaire(StructureSanitaire structureSanitaire) {
        this.structureSanitaire = structureSanitaire;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
