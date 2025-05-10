package com.esiitech.monbondocteurv2.dto;

import com.esiitech.monbondocteurv2.model.RefSpecialite;
import com.esiitech.monbondocteurv2.model.Sexe;

public class RendezVousDTO {

    private Long id;
    private Long structureSanitaireId;
    private RefSpecialite refSpecialite;
    private Long medecinId;
    private Long agendaMedecinId;
    private Long dateRdvId;
    private Long horaireRdvId;

    private String nom;
    private String prenom;
    private String email;
    private Sexe sexe;
    private int age;
    private Double montantPaye;
    private String motif;

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStructureSanitaireId() {
        return structureSanitaireId;
    }

    public void setStructureSanitaireId(Long structureSanitaireId) {
        this.structureSanitaireId = structureSanitaireId;
    }

    public RefSpecialite getRefSpecialite() {
        return refSpecialite;
    }

    public void setRefSpecialite(RefSpecialite refSpecialite) {
        this.refSpecialite = refSpecialite;
    }

    public Long getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(Long medecinId) {
        this.medecinId = medecinId;
    }

    public Long getAgendaMedecinId() {
        return agendaMedecinId;
    }

    public void setAgendaMedecinId(Long agendaMedecinId) {
        this.agendaMedecinId = agendaMedecinId;
    }

    public Long getDateRdvId() {
        return dateRdvId;
    }

    public void setDateRdvId(Long dateRdvId) {
        this.dateRdvId = dateRdvId;
    }

    public Long getHoraireRdvId() {
        return horaireRdvId;
    }

    public void setHoraireRdvId(Long horaireRdvId) {
        this.horaireRdvId = horaireRdvId;
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

    public Double getMontantPaye() {
        return montantPaye;
    }

    public void setMontantPaye(Double montantPaye) {
        this.montantPaye = montantPaye;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }
}
