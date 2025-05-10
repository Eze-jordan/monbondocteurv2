package com.esiitech.monbondocteurv2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "medecin_structure_sanitaire")
public class MedecinStructureSanitaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
