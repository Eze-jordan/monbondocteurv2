package com.esiitech.monbondocteurv2.dto;

import java.util.List;

public class JourneeActiviteDTO {
    private String id;
    private String date;
    private String agendaId; // nouveau champ
    private MedecinDTO medecin;
    private StructureSanitaireDTO structureSanitaire;
    private String statut;
    // JourneeActiviteDTO.java
    private List<PlageHoraireDto> plages;
    private List<RendezVousDTO> rdvs;
    private String jour;

    public String getJour() { return jour; }
    public void setJour(String jour) { this.jour = jour; }

    public List<RendezVousDTO> getRdvs() { return rdvs; }
    public void setRdvs(List<RendezVousDTO> rdvs) { this.rdvs = rdvs; }

    public List<PlageHoraireDto> getPlages() { return plages; }
    public void setPlages(List<PlageHoraireDto> plages) { this.plages = plages; }




    public String getAgendaId() {
        return agendaId;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public void setAgendaId(String agendaId) {
        this.agendaId = agendaId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public MedecinDTO getMedecin() {
        return medecin;
    }

    public void setMedecin(MedecinDTO medecin) {
        this.medecin = medecin;
    }

    public StructureSanitaireDTO getStructureSanitaire() {
        return structureSanitaire;
    }

    public void setStructureSanitaire(StructureSanitaireDTO structureSanitaire) {
        this.structureSanitaire = structureSanitaire;
    }

    // getters & setters

    public static class MedecinDTO {
        private String nomMedecin;
        private String prenomMedecin;
        private String refSpecialite;
        private String photoPath;




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

        public String getRefSpecialite() {
            return refSpecialite;
        }

        public void setRefSpecialite(String refSpecialite) {
            this.refSpecialite = refSpecialite;
        }

        public String getPhotoPath() {
            return photoPath;
        }

        public void setPhotoPath(String photoPath) {
            this.photoPath = photoPath;
        }
    }

    public static class StructureSanitaireDTO {
        private String nomStructureSanitaire;
        private String ville;
        private String photoPath;


        public String getNomStructureSanitaire() {
            return nomStructureSanitaire;
        }

        public void setNomStructureSanitaire(String nomStructureSanitaire) {
            this.nomStructureSanitaire = nomStructureSanitaire;
        }

        public String getVille() {
            return ville;
        }

        public void setVille(String ville) {
            this.ville = ville;
        }

        public String getPhotoPath() {
            return photoPath;
        }

        public void setPhotoPath(String photoPath) {
            this.photoPath = photoPath;
        }
    }

    public static class Agenda{

    }
}

