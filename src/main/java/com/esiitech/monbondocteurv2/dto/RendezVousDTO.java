package com.esiitech.monbondocteurv2.dto;

import com.esiitech.monbondocteurv2.model.Sexe;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public class RendezVousDTO {

    private String id;
    private String nom;
    private String prenom;
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;
    private String adresse;
    @Size(min = 15, message = "Le Numero doit contenir au moins 15 caractères")
    private String telephone;
    private Sexe sexe;
    private int age;
    private String motif;
    private Set<String> refSpecialites;
    private LocalDate date;
    private LocalTime heureDebut; // + getter/setter
    private String agendaId;
    private boolean actif = true;

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }
    public String getAgendaId() {
        return agendaId;
    }
    public void setAgendaId(String agendaId) {
        this.agendaId = agendaId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
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

    public Sexe getSexe() {
        return sexe;
    }

    public void setSexe(Sexe sexe) {
        this.sexe = sexe;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }


    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public Set<String> getRefSpecialites() {
        return refSpecialites;
    }

    public void setRefSpecialites(Set<String> refSpecialites) {
        this.refSpecialites = refSpecialites;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
