package com.esiitech.monbondocteurv2.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class StructureSanitaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private Ville ville;
    private String nomStructureSanitaire;
    private String adresse;
    @Column(name = "logo")
    private String logoPath;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(unique = true, nullable = false)
    private String numeroTelephone;
    @Column(nullable = false)
    private String motDePasse;
    @Enumerated(EnumType.STRING)
    private RefType refType;
    @Column(name = "Longitude")
    private Float GpsLongitude;
    @Column(name = "Latitude")
    private Float GpsLatitude;
    @ElementCollection(targetClass = RefSpecialite.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "structureSanitaire_specialite", joinColumns = @JoinColumn(name = "structureSanitaire_id"))
    @Column(name = "specialite")
    private Set<RefSpecialite> refSpecialites = new HashSet<RefSpecialite>();

    // Méthode pour obtenir l'ID formaté sur 4 chiffres
    public String getIdFormate() {
        return String.format("%04d", this.id);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ville getVille() {
        return ville;
    }

    public void setVille(Ville ville) {
        this.ville = ville;
    }

    public String getNomStructureSanitaire() {
        return nomStructureSanitaire;
    }

    public void setNomStructureSanitaire(String nomStructureSanitaire) {
        this.nomStructureSanitaire = nomStructureSanitaire;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String photoPath) {
        this.logoPath = photoPath;
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

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public RefType getRefType() {
        return refType;
    }

    public void setRefType(RefType refType) {
        this.refType = refType;
    }

    public Float getGpsLongitude() {
        return GpsLongitude;
    }

    public void setGpsLongitude(Float gpsLongitude) {
        GpsLongitude = gpsLongitude;
    }

    public Float getGpsLatitude() {
        return GpsLatitude;
    }

    public void setGpsLatitude(Float gpsLatitude) {
        GpsLatitude = gpsLatitude;
    }

    public Set<RefSpecialite> getRefSpecialites() {
        return refSpecialites;
    }

    public void setRefSpecialites(Set<RefSpecialite> refSpecialites) {
        this.refSpecialites = refSpecialites;
    }
}
