package com.esiitech.monbondocteurv2.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "agenda_medecin")
public class AgendaMedecin {
    @Id
    @Column(name = "id", nullable = false, length = 100, updatable = false)
    private String  id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medecin_id", nullable = false)
    private Medecin medecin;
    @Column(nullable = false)
    private JourSemaine jour;
    private boolean autorise;
    @Column(nullable = false)
    private LocalDate effectiveFrom; // date à partir de laquelle cette version est valable


    @OneToMany(
            mappedBy = "agenda",
            cascade = CascadeType.ALL
         //   orphanRemoval = true
    )    private List<PlageHoraire> plages;
    @ManyToOne
    @JoinColumn(name = "structure_sanitaire_id", nullable = false)
    private StructureSanitaire structureSanitaire;

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public StructureSanitaire getStructureSanitaire() {
        return structureSanitaire;
    }

    public void setStructureSanitaire(StructureSanitaire structureSanitaire) {
        this.structureSanitaire = structureSanitaire;
    }
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

    public JourSemaine getJour() {
        return jour;
    }

    public void setJour(JourSemaine jour) {
        this.jour = jour;
    }

    public boolean isAutorise() {
        return autorise;
    }

    public void setAutorise(boolean autorise) {
        this.autorise = autorise;
    }

    public List<PlageHoraire> getPlages() {
        return plages;
    }

    public void setPlages(List<PlageHoraire> plages) {
        this.plages = plages;
    }
}
