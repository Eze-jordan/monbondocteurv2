package com.esiitech.monbondocteurv2.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
public class StructureSanitaire implements UserDetails {
    @Id
    @Column(length = 6)
    private String id;
    @Enumerated(EnumType.STRING)
    private Ville ville;
    private String nomStructureSanitaire;
    private String adresse;
    @Column(name = "logo")
    private String photoPath;
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

    @Column(nullable = false)
    private boolean actif = false;
    @Enumerated(EnumType.STRING)
    private Role role;


    // Méthode pour obtenir l'ID formaté sur 4 chiffres
    public String getIdFormate() {
        return String.format("%04d", this.id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Ville getVille() {
        return ville;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
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

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    @Override
    public String getPassword() {
        return this.motDePasse;
    }

    @Override
    public String getUsername() {
        return this.email;
    }
}
