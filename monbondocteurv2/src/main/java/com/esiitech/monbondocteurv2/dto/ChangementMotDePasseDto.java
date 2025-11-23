package com.esiitech.monbondocteurv2.dto;

public class ChangementMotDePasseDto {
    private String email;
    private String nouveauMotDePasse;
    private String confirmerMotDePasse;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNouveauMotDePasse() {
        return nouveauMotDePasse;
    }

    public void setNouveauMotDePasse(String nouveauMotDePasse) {
        this.nouveauMotDePasse = nouveauMotDePasse;
    }

    public String getConfirmerMotDePasse() {
        return confirmerMotDePasse;
    }

    public void setConfirmerMotDePasse(String confirmerMotDePasse) {
        this.confirmerMotDePasse = confirmerMotDePasse;
    }

    // Getters et setters
}
