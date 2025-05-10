    package com.esiitech.monbondocteurv2.model;

    import jakarta.persistence.*;


    @Entity
    @Table(name = "date_rdv")
    public class DateRdv {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @ManyToOne(cascade = CascadeType.PERSIST)
        private AgendaMedecin agendaMedecin;
        @ManyToOne(cascade = CascadeType.PERSIST)
        private DateRdv dateRdv;
        private Double nombrePatient;
        private Double rdvPris;

        public Double getRdvPris() {
            return rdvPris;
        }

        public void setRdvPris(Double rdvPris) {
            this.rdvPris = rdvPris;
        }

        public Double getNombrePatient() {
            return nombrePatient;
        }

        public void setNombrePatient(Double nombrePatient) {
            this.nombrePatient = nombrePatient;
        }

        public DateRdv getDateRdv() {
            return dateRdv;
        }

        public void setDateRdv(DateRdv dateRdv) {
            this.dateRdv = dateRdv;
        }

        public AgendaMedecin getAgendaMedecin() {
            return agendaMedecin;
        }

        public void setAgendaMedecin(AgendaMedecin agendaMedecin) {
            this.agendaMedecin = agendaMedecin;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }



    }
