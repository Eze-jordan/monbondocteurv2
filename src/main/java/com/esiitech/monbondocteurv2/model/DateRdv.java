    package com.esiitech.monbondocteurv2.model;

    import jakarta.persistence.*;

    import java.time.LocalDate;


    @Entity
    @Table(name = "date_rdv")
    public class DateRdv {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @ManyToOne(cascade = CascadeType.PERSIST)
        private AgendaMedecin agendaMedecin;
        private LocalDate date;
        private Double nombrePatient;
        private Double rdvPris;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public AgendaMedecin getAgendaMedecin() {
            return agendaMedecin;
        }

        public void setAgendaMedecin(AgendaMedecin agendaMedecin) {
            this.agendaMedecin = agendaMedecin;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public Double getNombrePatient() {
            return nombrePatient;
        }

        public void setNombrePatient(Double nombrePatient) {
            this.nombrePatient = nombrePatient;
        }

        public Double getRdvPris() {
            return rdvPris;
        }

        public void setRdvPris(Double rdvPris) {
            this.rdvPris = rdvPris;
        }
    }
