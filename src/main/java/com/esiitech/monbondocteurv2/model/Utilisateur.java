package com.esiitech.monbondocteurv2.model;

import com.esiitech.monbondocteurv2.enums.Role;
import com.esiitech.monbondocteurv2.enums.Sexe;
import com.esiitech.monbondocteurv2.enums.StatutCompte;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "utilisateur")
public class Utilisateur implements UserDetails {

    @Id
    @Column(name = "id", nullable = false, length = 100, updatable = false)
    private String id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(name = "numero_telephone")
    private String numeroTelephone;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    private Sexe sexe;

    @Column(name = "photo")
    private String photoPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    /*
     * Champ conservé pour compatibilité avec l'ancien projet avec paiement.
     * Certains services existants peuvent encore utiliser isActif() ou setActif().
     */
    @Column(nullable = false)
    private boolean actif = false;

    /*
     * Nouveau système de statut venant du projet sans paiement.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_compte", nullable = false)
    private StatutCompte statutCompte = StatutCompte.INVITE;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Role roleEffectif = this.role != null ? this.role : Role.USER;

        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + roleEffectif.name())
        );
    }

    @Override
    public String getPassword() {
        return this.motDePasse;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isCompteActif();
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.statutCompte != StatutCompte.SUSPENDU;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isCompteActif();
    }

    @Override
    public boolean isEnabled() {
        return isCompteActif();
    }

    public boolean isCompteActif() {
        if (this.statutCompte == StatutCompte.SUSPENDU) {
            return false;
        }

        if (this.statutCompte == StatutCompte.ACTIF) {
            return true;
        }

        return this.actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;

        if (actif) {
            this.statutCompte = StatutCompte.ACTIF;
        } else {
            if (this.statutCompte == null || this.statutCompte == StatutCompte.ACTIF) {
                this.statutCompte = StatutCompte.INVITE;
            }
        }
    }

    public void setStatutCompte(StatutCompte statutCompte) {
        this.statutCompte = statutCompte;

        if (statutCompte == StatutCompte.ACTIF) {
            this.actif = true;
        } else if (statutCompte == StatutCompte.INVITE || statutCompte == StatutCompte.SUSPENDU || statutCompte == null) {
            this.actif = false;
        }
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

    public String getNumeroTelephone() {
        return numeroTelephone;
    }

    public void setNumeroTelephone(String numeroTelephone) {
        this.numeroTelephone = numeroTelephone;
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

    public boolean isActif() {
        return actif;
    }

    public StatutCompte getStatutCompte() {
        return statutCompte;
    }
}