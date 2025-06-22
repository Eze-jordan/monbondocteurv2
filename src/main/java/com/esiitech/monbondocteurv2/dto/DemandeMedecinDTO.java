package com.esiitech.monbondocteurv2.dto;

import com.esiitech.monbondocteurv2.model.RefGrade;
import com.esiitech.monbondocteurv2.model.RefSpecialite;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class DemandeMedecinDTO {
    private Long id;
    private String nomMedecin;
    private String prenomMedecin;
    private RefGrade refGrade;
    private RefSpecialite refSpecialite;
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;
    private String matricule;

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


    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    // Getters et setters
}
