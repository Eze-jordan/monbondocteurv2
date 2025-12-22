    package com.esiitech.monbondocteurv2.model;

    import jakarta.persistence.*;

    import java.time.LocalDate;
    import java.time.LocalTime;
    import java.util.HashSet;
    import java.util.Set;

    @Entity
    public class RendezVous {
        @Id
        @Column(name = "id", nullable = false,length = 100,updatable = false)
        private String  id;
        @ManyToOne(cascade = CascadeType.PERSIST)
        private AgendaMedecin agendaMedecin;
        @ManyToOne(cascade = CascadeType.PERSIST)
        private Medecin medecin;
        @ManyToOne
        @JoinColumn(name = "structure_sanitaire_id")
        private StructureSanitaire structureSanitaire;
        @ElementCollection
        @CollectionTable(name = "rendezvous_specialite", joinColumns = @JoinColumn(name = "rendezvous_id"))
        @Column(name = "specialite", nullable = false, length = 100)
        private Set<String> refSpecialites = new HashSet<>();

        @Column(nullable = false)
        private String nom;
        @Column(nullable = false)
        private String prenom;
        @Column(nullable = false)
        private String email;
        @Column(nullable = false)
        private String adresse;
        @Column(nullable = false)
        private String telephone;
        @Enumerated(EnumType.STRING)
        private Sexe sexe;
        @Column(nullable = false)
        private int age;
        @Column(nullable = false)
        private String motif;
        @Column(nullable = false)
        private boolean actif = true;

        @ManyToOne
        @JoinColumn(name = "utilisateur_id", referencedColumnName = "id")
        private Utilisateur utilisateur;
        private LocalDate date;
        private LocalTime heureDebut;
        private PeriodeJournee periodeJournee;
        @ManyToOne
        @JoinColumn(name = "plage_id")
        private PlageHoraire plageHoraire;
        private String agendaId;


// + getters/setters

        public String getAgendaId() {
            return agendaId;
        }

        public void setAgendaId(String agendaId) {
            this.agendaId = agendaId;
        }

        public LocalDate getDate() {
            return date;
        }

        public boolean isActif() {
            return actif;
        }

        public void setActif(boolean actif) {
            this.actif = actif;
        }

        public PlageHoraire getPlageHoraire() {
            return plageHoraire;
        }

        public void setPlageHoraire(PlageHoraire plageHoraire) {
            this.plageHoraire = plageHoraire;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public LocalTime getHeureDebut() {
            return heureDebut;
        }

        public void setHeureDebut(LocalTime heureDebut) {
            this.heureDebut = heureDebut;
        }

        public PeriodeJournee getPeriodeJournee() {
            return periodeJournee;
        }

        public void setPeriodeJournee(PeriodeJournee periodeJournee) {
            this.periodeJournee = periodeJournee;
        }

        public Set<String> getRefSpecialites() {
            return refSpecialites;
        }

        public void setRefSpecialites(Set<String> refSpecialites) {
            this.refSpecialites = refSpecialites;
        }

        public StructureSanitaire getStructureSanitaire() {
            return structureSanitaire;
        }

        public void setStructureSanitaire(StructureSanitaire structureSanitaire) {
            this.structureSanitaire = structureSanitaire;
        }
        public Medecin getMedecin() {
            return medecin;
        }
        public void setMedecin(Medecin medecin) {
            this.medecin = medecin;
        }
        public AgendaMedecin getAgendaMedecin() {
            return agendaMedecin;
        }
        public void setAgendaMedecin(AgendaMedecin agendaMedecin) {
            this.agendaMedecin = agendaMedecin;
        }
        public String getId() {
            return id;
        }

        @ManyToOne
        @JoinColumn(name = "journee_activite_id", nullable = false)
        private JourneeActivite journeeActivite;

        public JourneeActivite getJourneeActivite() {
            return journeeActivite;
        }

        public void setJourneeActivite(JourneeActivite journeeActivite) {
            this.journeeActivite = journeeActivite;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAdresse() {
            return adresse;
        }

        public void setAdresse(String adresse) {
            this.adresse = adresse;
        }

        public String getTelephone() {
            return telephone;
        }

        public void setTelephone(String telephone) {
            this.telephone = telephone;
        }

        public Sexe getSexe() {
            return sexe;
        }

        public void setSexe(Sexe sexe) {
            this.sexe = sexe;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }


        public String getMotif() {
            return motif;
        }

        public void setMotif(String motif) {
            this.motif = motif;
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

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setUtilisateur(Utilisateur utilisateur) {
            this.utilisateur = utilisateur;
        }

        public Utilisateur getUtilisateur() {
            return utilisateur;
        }



    }
