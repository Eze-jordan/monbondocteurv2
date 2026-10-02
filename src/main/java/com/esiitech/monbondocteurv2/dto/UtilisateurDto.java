package com.esiitech.monbondocteurv2.dto;

import com.esiitech.monbondocteurv2.enums.Role;
import com.esiitech.monbondocteurv2.enums.Sexe;
import com.esiitech.monbondocteurv2.enums.StatutCompte;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    private StatutCompte statut;

    /*
     * false = création par invitation
     * true  = génération automatique du mot de passe
     *         + compte directement ACTIF
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean genererMotDePasse;

    public UtilisateurDto() {
        this.role = Role.USER;
        this.statut = StatutCompte.INVITE;
        this.genererMotDePasse = false;
    }

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

    public String getNumeroTelephone() {
        return numeroTelephone;
    }

    public void setNumeroTelephone(String numeroTelephone) {
        this.numeroTelephone = numeroTelephone;
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

    public boolean isGenererMotDePasse() {
        return genererMotDePasse;
    }

    public void setGenererMotDePasse(boolean genererMotDePasse) {
        this.genererMotDePasse = genererMotDePasse;
    }

    @Override
    public String toString() {
        return "UtilisateurDto{" +
                "id='" + id + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", sexe=" + sexe +
                ", numeroTelephone='" + numeroTelephone + '\'' +
                ", photoPath='" + photoPath + '\'' +
                ", role=" + role +
                ", statut=" + statut +
                ", genererMotDePasse=" + genererMotDePasse +
                '}';
    }
}