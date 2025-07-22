package com.esiitech.monbondocteurv2.model;

import com.esiitech.monbondocteurv2.dto.MedecinDto;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class Medecin  implements UserDetails {
    @Id
    @Column(length = 6)
    private String id;
    private String nomMedecin;
    private String prenomMedecin;
    @Enumerated(EnumType.STRING)
    private RefGrade refGrade;
    @Enumerated(EnumType.STRING)

    private  RefSpecialite refSpecialite;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String motDePasse;
    @Column(name = "photo")
    private String photoPath;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean actif = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
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
