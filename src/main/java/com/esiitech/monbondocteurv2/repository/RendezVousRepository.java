package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.DateRdv;
import com.esiitech.monbondocteurv2.model.HoraireRdv;
import com.esiitech.monbondocteurv2.model.RendezVous;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {
    long countByEmailAndDateRdvAndHoraireRdv(String email, DateRdv dateRdv, HoraireRdv horaireRdv);
}


