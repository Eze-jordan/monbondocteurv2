package com.esiitech.monbondocteurv2.model;

import jakarta.persistence.*;

@Entity
public class RendezVous {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne (cascade = CascadeType.PERSIST)
    private StructureSanitaire structureSanitaire;
    @Enumerated(EnumType.STRING)
    private RefSpecialite refSpecialite;
    @OneToOne (cascade = CascadeType.PERSIST)
    private Medecin medecin;
    @OneToOne (cascade = CascadeType.PERSIST)
    private AgendaMedecin agendaMedecin;
    @OneToOne (cascade = CascadeType.PERSIST)
    private DateRdv dateRdv;
    @OneToOne (cascade = CascadeType.PERSIST)
    private HoraireRdv horaireRdv;
    @Column(nullable = false)
    private String nom;
    @Column(nullable = false)
    private String prenom;
    @Column(unique = true, nullable = false)
    private String email;
    @Enumerated(EnumType.STRING)
    private Sexe sexe;
    @Column(nullable = false)
    private int age;
    @Column(nullable = false)
    private Double montantPaye;
    @Column(nullable = false)
    private String motif;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StructureSanitaire getStructureSanitaire() {
        return structureSanitaire;
    }

    public void setStructureSanitaire(StructureSanitaire structureSanitaire) {
        this.structureSanitaire = structureSanitaire;
    }

    public RefSpecialite getRefSpecialite() {
        return refSpecialite;
    }

    public void setRefSpecialite(RefSpecialite refSpecialite) {
        this.refSpecialite = refSpecialite;
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

    public DateRdv getDateRdv() {
        return dateRdv;
    }

    public void setDateRdv(DateRdv dateRdv) {
        this.dateRdv = dateRdv;
    }

    public HoraireRdv getHoraireRdv() {
        return horaireRdv;
    }

    public void setHoraireRdv(HoraireRdv horaireRdv) {
        this.horaireRdv = horaireRdv;
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

    public Double getMontantPaye() {
        return montantPaye;
    }

    public void setMontantPaye(Double montantPaye) {
        this.montantPaye = montantPaye;
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
}
