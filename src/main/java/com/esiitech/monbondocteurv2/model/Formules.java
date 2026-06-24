package com.esiitech.monbondocteurv2.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "formules")
public class Formules {

    @Id
    @Column(name = "id", nullable = false, length = 100, updatable = false)
    private String id;

    @Column(name = "nom_formule", nullable = false)
    private String nomFormule;

    @Column(name = "description_formule", columnDefinition = "TEXT")
    private String descriptionFormule;

    @Column(name = "nombre_de_mois", nullable = false)
    private int nombreDeMois;

    @Column(name = "montant_ttc", nullable = false, precision = 19, scale = 2)
    private BigDecimal montantTTC;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        if (this.createdAt == null) {
            this.createdAt = now;
        }

        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();

        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomFormule() {
        return nomFormule;
    }

    public void setNomFormule(String nomFormule) {
        this.nomFormule = nomFormule;
    }

    public String getDescriptionFormule() {
        return descriptionFormule;
    }

    public void setDescriptionFormule(String descriptionFormule) {
        this.descriptionFormule = descriptionFormule;
    }

    public int getNombreDeMois() {
        return nombreDeMois;
    }

    public void setNombreDeMois(int nombreDeMois) {
        this.nombreDeMois = nombreDeMois;
    }

    public BigDecimal getMontantTTC() {
        return montantTTC;
    }

    public void setMontantTTC(BigDecimal montantTTC) {
        this.montantTTC = montantTTC;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}