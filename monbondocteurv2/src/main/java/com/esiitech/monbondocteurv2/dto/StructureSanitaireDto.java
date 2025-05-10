package com.esiitech.monbondocteurv2.dto;

import com.esiitech.monbondocteurv2.model.RefSpecialite;
import com.esiitech.monbondocteurv2.model.RefType;
import com.esiitech.monbondocteurv2.model.Ville;

import java.util.Set;

public class StructureSanitaireDto {
    private Long id;
    private String nomStructureSanitaire;
    private String adresse;
    private String email;
    private String motDePasse;
    private String numeroTelephone;
    private String logoPath;
    private Ville ville;
    private RefType refType;
    private Float gpsLongitude;
    private Float gpsLatitude;
    private Set<RefSpecialite> refSpecialites;

    // Getters et setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomStructureSanitaire() {
        return nomStructureSanitaire;
    }

    public void setNomStructureSanitaire(String nomStructureSanitaire) {
        this.nomStructureSanitaire = nomStructureSanitaire;
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

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public Ville getVille() {
        return ville;
    }

    public void setVille(Ville ville) {
        this.ville = ville;
    }

    public RefType getRefType() {
        return refType;
    }

    public void setRefType(RefType refType) {
        this.refType = refType;
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

    public Set<RefSpecialite> getRefSpecialites() {
        return refSpecialites;
    }

    public void setRefSpecialites(Set<RefSpecialite> refSpecialites) {
        this.refSpecialites = refSpecialites;
    }
}
