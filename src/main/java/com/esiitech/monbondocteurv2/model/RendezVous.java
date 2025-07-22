    package com.esiitech.monbondocteurv2.model;

    import jakarta.persistence.*;

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
        @ElementCollection(targetClass = RefSpecialite.class)
        @Enumerated(EnumType.STRING)
        @CollectionTable(name = "utilisateur_specialite", joinColumns = @JoinColumn(name = "utilisateur_id"))
        @Column(name = "specialite")
        private Set<RefSpecialite> refSpecialites = new HashSet<>();
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

        @ManyToOne
        @JoinColumn(name = "utilisateur_id", referencedColumnName = "id")
        private Utilisateur utilisateur;

        public Set<RefSpecialite> getRefSpecialites() {
            return refSpecialites;
        }

        public void setRefSpecialites(Set<RefSpecialite> refSpecialites) {
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
