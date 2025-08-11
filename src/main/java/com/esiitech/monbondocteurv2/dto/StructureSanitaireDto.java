package com.esiitech.monbondocteurv2.dto;

import com.esiitech.monbondocteurv2.model.RefSpecialite;
import com.esiitech.monbondocteurv2.model.RefType;
import com.esiitech.monbondocteurv2.model.Ville;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class StructureSanitaireDto {
    private String id;
    private String nomStructureSanitaire;
    private String adresse;
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 12, message = "Le mot de passe doit contenir au moins 12 caractères")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
            message = "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial"
    )
    private String motDePasse;
    private String numeroTelephone;
    private String photoPath;
    private String ville;
    private String refType;
    private Float gpsLongitude;
    private Float gpsLatitude;
    private Set<String> refSpecialites; // ou List<String>
    private boolean actif;


    // Getters et setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomStructureSanitaire() {
        return nomStructureSanitaire;
    }

    public void setNomStructureSanitaire(String nomStructureSanitaire) {
        this.nomStructureSanitaire = nomStructureSanitaire;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;
    }

    public Set<String> getRefSpecialites() {
        return refSpecialites;
    }

    public void setRefSpecialites(Set<String> refSpecialites) {
        this.refSpecialites = refSpecialites;
    }

    public Float getGpsLongitude() {
        return gpsLongitude;
    }

    public void setGpsLongitude(Float gpsLongitude) {
        this.gpsLongitude = gpsLongitude;
    }

    public Float getGpsLatitude() {
        return gpsLatitude;
    }

    public void setGpsLatitude(Float gpsLatitude) {
        this.gpsLatitude = gpsLatitude;
    }
}
