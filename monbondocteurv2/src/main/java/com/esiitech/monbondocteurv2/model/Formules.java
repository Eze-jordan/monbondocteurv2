package com.esiitech.monbondocteurv2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "formules")
public class Formules {
    @Id
    @Column(name = "id", nullable = false,length = 100,updatable = false)
    private String  id;

    private String nomFormule;
    private String descriptionFormule;
    private int nombreDeMois;
    private float montantTTC;


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

    public float getMontantTTC() {
        return montantTTC;
    }

    public void setMontantTTC(float montantTTC) {
        this.montantTTC = montantTTC;
    }
}

