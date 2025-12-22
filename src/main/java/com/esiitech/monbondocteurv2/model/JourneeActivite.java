package com.esiitech.monbondocteurv2.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "journee_activite",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"date", "medecin_id", "structure_sanitaire_id"}
        )
)
public class JourneeActivite {

    @Id
    @Column(length = 100)
    private String id;

    @ManyToOne
    @JoinColumn(name = "medecin_id", nullable = false)
    private Medecin medecin;

    @ManyToOne
    @JoinColumn(name = "structure_sanitaire_id", nullable = false)
    private StructureSanitaire structureSanitaire;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "agenda_id", nullable = false)
    private AgendaMedecin agenda;

    @Column(nullable = false)
    private boolean autorise;

    @Column(nullable = false)
    private LocalDateTime heureOuverture;

    // getters / setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Medecin getMedecin() {
        return medecin;
    }

    public void setMedecin(Medecin medecin) {
        this.medecin = medecin;
    }

    public StructureSanitaire getStructureSanitaire() {
        return structureSanitaire;
    }

    public void setStructureSanitaire(StructureSanitaire structureSanitaire) {
        this.structureSanitaire = structureSanitaire;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public AgendaMedecin getAgenda() {
        return agenda;
    }

    public void setAgenda(AgendaMedecin agenda) {
        this.agenda = agenda;
    }

    public boolean isAutorise() {
        return autorise;
    }

    public void setAutorise(boolean autorise) {
        this.autorise = autorise;
    }

    public LocalDateTime getHeureOuverture() {
        return heureOuverture;
    }

    public void setHeureOuverture(LocalDateTime heureOuverture) {
        this.heureOuverture = heureOuverture;
    }
}

