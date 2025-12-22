package com.esiitech.monbondocteurv2.model;

import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "plage_horaire")
public class PlageHoraire {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private String  id;

    @ManyToOne
    @JoinColumn(name = "agenda_id", nullable = false)
    private AgendaMedecin agenda;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodeJournee periode; // MATIN ou SOIR

    private LocalTime heureDebut;
    private LocalTime heureFin;

    private Integer nombrePatients;

    private boolean autorise;

    @OneToMany(mappedBy = "plageHoraire")
    private List<RendezVous> rendezVous; // liste des RDV liés à cette plage

    public int getNombrePatientsRestants() {
        return nombrePatients - rendezVous.size();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AgendaMedecin getAgenda() {
        return agenda;
    }

    public void setAgenda(AgendaMedecin agenda) {
        this.agenda = agenda;
    }

    public PeriodeJournee getPeriode() {
        return periode;
    }

    public void setPeriode(PeriodeJournee periode) {
        this.periode = periode;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    public Integer getNombrePatients() {
        return nombrePatients;
    }

    public void setNombrePatients(Integer nombrePatients) {
        this.nombrePatients = nombrePatients;
    }

    public boolean isAutorise() {
        return autorise;
    }

    public void setAutorise(boolean autorise) {
        this.autorise = autorise;
    }




}
