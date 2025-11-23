package com.esiitech.monbondocteurv2.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table (name = "validation")
public class Validation {
    @Id
    @Column(name = "id", nullable = false,length = 100,updatable = false)
    private String  id;
    private Instant creation;
    private Instant expiration;
    private Instant activation;
    private String code;
    @OneToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @OneToOne
    @JoinColumn(name = "medecin_id")
    private Medecin medecin;

    @OneToOne
    @JoinColumn(name = "structure_sanitaire_id")
    private StructureSanitaire structureSanitaire;


    public Validation() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getCreation() {
        return creation;
    }

    public void setCreation(Instant creation) {
        this.creation = creation;
    }

    public Instant getExpiration() {
        return expiration;
    }

    public void setExpiration(Instant expiration) {
        this.expiration = expiration;
    }

    public Instant getActivation() {
        return activation;
    }

    public void setActivation(Instant activation) {
        this.activation = activation;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
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

    public Validation(String id, Instant creation, Instant expiration, Instant activation, String code, Utilisateur utilisateur, Medecin medecin, StructureSanitaire structureSanitaire) {
        this.id = id;
        this.creation = creation;
        this.expiration = expiration;
        this.activation = activation;
        this.code = code;
        this.utilisateur = utilisateur;
        this.medecin = medecin;
        this.structureSanitaire = structureSanitaire;
    }
}
