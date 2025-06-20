package com.esiitech.monbondocteurv2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "agenda_medecin")
public class AgendaMedecin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(cascade = CascadeType.PERSIST)
    private StructureSanitaire structureSanitaire;
    @ManyToOne(cascade = CascadeType.PERSIST)
    private Medecin medecin;
    @Column(nullable = false)
    private boolean actif = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StructureSanitaire getStructureSanitaire() {
        return structureSanitaire;
    }

    public void setStructureSanitaire(StructureSanitaire structureSanitaire) {
        this.structureSanitaire = structureSanitaire;
    }

    public Medecin getMedecin() {
        return medecin;
    }

    public void setMedecin(Medecin medecin) {
        this.medecin = medecin;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
