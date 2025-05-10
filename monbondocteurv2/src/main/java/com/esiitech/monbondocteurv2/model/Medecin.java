package com.esiitech.monbondocteurv2.model;

import jakarta.persistence.*;

@Entity
public class Medecin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nomMedecin;
    private String prenomMedecin;
    @Enumerated(EnumType.STRING)
    private RefGrade refGrade;
    @Enumerated(EnumType.STRING)
    private  RefSpecialite refSpecialite;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String motDePasse;
    @Column(name = "photo")
    private String photoPath;

    @Column(nullable = false)
    private boolean actif = false;

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
