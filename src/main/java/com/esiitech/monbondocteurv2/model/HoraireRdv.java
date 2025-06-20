package com.esiitech.monbondocteurv2.model;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "horaire_rdv")
public class HoraireRdv {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(cascade = CascadeType.PERSIST)
    private DateRdv dateRdv;
    private LocalTime heureDebut;

    private LocalTime heureFin;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DateRdv getDateRdv() {
        return dateRdv;
    }

    public void setDateRdv(DateRdv dateRdv) {
        this.dateRdv = dateRdv;
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
}
