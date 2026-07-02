package com.esiitech.monbondocteurv2.dto;

import com.esiitech.monbondocteurv2.enums.Role;
import com.esiitech.monbondocteurv2.enums.Sexe;
import com.esiitech.monbondocteurv2.enums.StatutCompte;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UtilisateurDto {

    private String id;

    private String nom;

    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;

    private Sexe sexe;

    private String numeroTelephone;

    private String photoPath;

    private Role role;

    // Remplace actif par statut (clean architecture)
    private StatutCompte statut;

    // =========================
    // CONSTRUCTEUR
    // =========================

    public UtilisateurDto() {
        this.role = Role.USER;
        this.statut = StatutCompte.INVITE;
    }

    public UtilisateurDto(
            String id,
            String nom,
            String prenom,
            String email,
            Sexe sexe,
            String photoPath,
            Role role,
            StatutCompte statut
    ) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.sexe = sexe;
        this.photoPath = photoPath;
        this.role = role;
        this.statut = statut;
    }

    // =========================
    // GETTERS & SETTERS
    // =========================

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public StatutCompte getStatut() {
        return statut;
    }

    public void setStatut(StatutCompte statut) {
        this.statut = statut;
    }

    public String getNumeroTelephone() { return numeroTelephone; }
    public void setNumeroTelephone(String numeroTelephone) { this.numeroTelephone = numeroTelephone; }

    // =========================
    // TOSTRING
    // =========================

    @Override
    public String toString() {
        return "UtilisateurDto{" +
                "id='" + id + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", sexe=" + sexe +
                ", photoPath='" + photoPath + '\'' +
                ", role=" + role +
                ", statut=" + statut +
                '}';
    }
}