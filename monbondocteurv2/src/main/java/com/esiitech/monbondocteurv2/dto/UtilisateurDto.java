package com.esiitech.monbondocteurv2.dto;

import com.esiitech.monbondocteurv2.model.Role;
import com.esiitech.monbondocteurv2.model.Sexe;
import com.esiitech.monbondocteurv2.model.Utilisateur;

public class UtilisateurDto  {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private Sexe sexe;
    private String photoPath;
    private Role role;
    private boolean actif;


    // Getters and Setters
    // Ajoutez un constructeur ou une initialisation pour définir un rôle par défaut
    public UtilisateurDto() {
        if (this.role == null) {
            this.role = Role.USER;  // Attribuer un rôle par défaut
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
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

    public Sexe getSexe() {
        return sexe;
    }

    public void setSexe(Sexe sexe) {
        this.sexe = sexe;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
