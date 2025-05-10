package com.esiitech.monbondocteurv2.dto;

import com.esiitech.monbondocteurv2.model.NoteMedecin;
import com.esiitech.monbondocteurv2.model.RefGrade;
import com.esiitech.monbondocteurv2.model.RefSpecialite;

public class MedecinDto {
    private Long id;
    private String nomMedecin;
    private String prenomMedecin;
    private RefGrade refGrade;
    private RefSpecialite refSpecialite;
    private String email;
    private String motDePasse;
    private String photoPath;
    private boolean actif;

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomMedecin() {
        return nomMedecin;
    }

    public void setNomMedecin(String nomMedecin) {
        this.nomMedecin = nomMedecin;
    }

    public String getPrenomMedecin() {
        return prenomMedecin;
    }

    public void setPrenomMedecin(String prenomMedecin) {
        this.prenomMedecin = prenomMedecin;
    }

    public RefGrade getRefGrade() {
        return refGrade;
    }

    public void setRefGrade(RefGrade refGrade) {
        this.refGrade = refGrade;
    }

    public RefSpecialite getRefSpecialite() {
        return refSpecialite;
    }

    public void setRefSpecialite(RefSpecialite refSpecialite) {
        this.refSpecialite = refSpecialite;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }


    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}