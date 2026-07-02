package com.esiitech.monbondocteurv2.dto;

import com.esiitech.monbondocteurv2.enums.Statut;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Date;
import java.util.Set;

public class StructureSanitaireDto {
    private String id;

    @NotBlank(message = "Le nom de la structure est obligatoire")
    @Size(max = 200, message = "Le nom ne peut pas dépasser 200 caractères")
    private String nomStructureSanitaire;

    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;

    @JsonIgnore
    private String motDePasse;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(regexp = "^(\\+?[0-9]{9,15})$", message = "Format de téléphone invalide")
    private String numeroTelephone;

    private String photoPath;
    private String ville;
    private String refType;
    private Float gpsLongitude;
    private Float gpsLatitude;
    private Set<String> refSpecialites;
    private String urldocument;  // ✅ Changé: Urldocument → urldocument (camelCase)
    private Date dateDebutAbonnement;  // ✅ Changé: DateDebutAbonnement → dateDebutAbonnement
    private Date dateFinAbonnement;    // ✅ Changé: DateFinAbonnement → dateFinAbonnement
    private boolean abonneExpire = true;
    private boolean actif;
    private Statut statut;

    // Getters et setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNomStructureSanitaire() { return nomStructureSanitaire; }
    public void setNomStructureSanitaire(String nomStructureSanitaire) { this.nomStructureSanitaire = nomStructureSanitaire; }

    public String getUrldocument() { return urldocument; }
    public void setUrldocument(String urldocument) { this.urldocument = urldocument; }

    public Date getDateDebutAbonnement() { return dateDebutAbonnement; }
    public void setDateDebutAbonnement(Date dateDebutAbonnement) { this.dateDebutAbonnement = dateDebutAbonnement; }

    public Date getDateFinAbonnement() { return dateFinAbonnement; }
    public void setDateFinAbonnement(Date dateFinAbonnement) { this.dateFinAbonnement = dateFinAbonnement; }

    public boolean isAbonneExpire() { return abonneExpire; }
    public void setAbonneExpire(boolean abonneExpire) { this.abonneExpire = abonneExpire; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNumeroTelephone() { return numeroTelephone; }
    public void setNumeroTelephone(String numeroTelephone) { this.numeroTelephone = numeroTelephone; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getRefType() { return refType; }
    public void setRefType(String refType) { this.refType = refType; }

    public Set<String> getRefSpecialites() { return refSpecialites; }
    public void setRefSpecialites(Set<String> refSpecialites) { this.refSpecialites = refSpecialites; }

    public Float getGpsLongitude() { return gpsLongitude; }
    public void setGpsLongitude(Float gpsLongitude) { this.gpsLongitude = gpsLongitude; }

    public Float getGpsLatitude() { return gpsLatitude; }
    public void setGpsLatitude(Float gpsLatitude) { this.gpsLatitude = gpsLatitude; }
}