package com.esiitech.monbondocteurv2.dto;

import com.esiitech.monbondocteurv2.enums.Sexe;
import com.esiitech.monbondocteurv2.enums.StatutRendezVous;
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
    @Size(min = 11, message = "Le Numero doit contenir au moins 11 caractères")
    private String telephone;
    private Sexe sexe;
    private int age;
    private String motif;
    private Set<String> refSpecialites;
    private LocalDate date;
    private String agendaId;
    private boolean actif = true;
    private String specialite;
    private String structureId;
    private StatutRendezVous statut;
    private String jour;
    private String periodeJournee;
    private PlageHoraireDto plage;
    private LocalTime heureDebut;


    // ✅ Infos médecin (pour affichage)
    private String medecinId;
    private String medecinNom;
    private String medecinPrenom;
    private String medecinSpecialite;
    private String medecinPhoto;
    private String medecinEmail;
    private String medecinTelephone;

    // ✅ Infos structure (pour affichage)
    private String structureNom;
    private String structureAdresse;
    private String structureVille;
    private String structurePhoto;
    private String structureEmail;
    private String structureTelephone;
    private Double structureLatitude;
    private Double structureLongitude;
    private String structureSanitaireId;
    private String structureSanitaireNom;

    // ==================== GETTERS & SETTERS ====================

    public PlageHoraireDto getPlage() { return plage; }
    public void setPlage(PlageHoraireDto plage) { this.plage = plage; }

    public String getPeriodeJournee() { return periodeJournee; }
    public void setPeriodeJournee(String periodeJournee) { this.periodeJournee = periodeJournee; }

    public String getJour() { return jour; }
    public void setJour(String jour) { this.jour = jour; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public String getAgendaId() { return agendaId; }
    public void setAgendaId(String agendaId) { this.agendaId = agendaId; }

    public StatutRendezVous getStatut() { return statut; }
    public void setStatut(StatutRendezVous statut) { this.statut = statut; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Sexe getSexe() { return sexe; }
    public void setSexe(Sexe sexe) { this.sexe = sexe; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public Set<String> getRefSpecialites() { return refSpecialites; }
    public void setRefSpecialites(Set<String> refSpecialites) { this.refSpecialites = refSpecialites; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public String getStructureId() { return structureId; }
    public void setStructureId(String structureId) { this.structureId = structureId; }

    // ✅ Médecin
    public String getMedecinId() { return medecinId; }
    public void setMedecinId(String medecinId) { this.medecinId = medecinId; }

    public String getMedecinNom() { return medecinNom; }
    public void setMedecinNom(String medecinNom) { this.medecinNom = medecinNom; }

    public String getMedecinPrenom() { return medecinPrenom; }
    public void setMedecinPrenom(String medecinPrenom) { this.medecinPrenom = medecinPrenom; }

    public String getMedecinSpecialite() { return medecinSpecialite; }
    public void setMedecinSpecialite(String medecinSpecialite) { this.medecinSpecialite = medecinSpecialite; }

    public String getMedecinPhoto() { return medecinPhoto; }
    public void setMedecinPhoto(String medecinPhoto) { this.medecinPhoto = medecinPhoto; }

    public String getMedecinEmail() { return medecinEmail; }
    public void setMedecinEmail(String medecinEmail) { this.medecinEmail = medecinEmail; }

    public String getMedecinTelephone() { return medecinTelephone; }
    public void setMedecinTelephone(String medecinTelephone) { this.medecinTelephone = medecinTelephone; }

    // ✅ Structure
    public String getStructureNom() { return structureNom; }
    public void setStructureNom(String structureNom) { this.structureNom = structureNom; }

    public String getStructureAdresse() { return structureAdresse; }
    public void setStructureAdresse(String structureAdresse) { this.structureAdresse = structureAdresse; }

    public String getStructureVille() { return structureVille; }
    public void setStructureVille(String structureVille) { this.structureVille = structureVille; }

    public String getStructurePhoto() { return structurePhoto; }
    public void setStructurePhoto(String structurePhoto) { this.structurePhoto = structurePhoto; }

    public String getStructureEmail() { return structureEmail; }
    public void setStructureEmail(String structureEmail) { this.structureEmail = structureEmail; }

    public String getStructureTelephone() { return structureTelephone; }
    public void setStructureTelephone(String structureTelephone) { this.structureTelephone = structureTelephone; }
    public Double getStructureLatitude() { return structureLatitude; }
    public void setStructureLatitude(Double structureLatitude) { this.structureLatitude = structureLatitude; }
    public Double getStructureLongitude() { return structureLongitude; }
    public void setStructureLongitude(Double structureLongitude) { this.structureLongitude = structureLongitude; }
}