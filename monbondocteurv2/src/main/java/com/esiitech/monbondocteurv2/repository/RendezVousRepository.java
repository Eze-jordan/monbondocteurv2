package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.DateRdv;
import com.esiitech.monbondocteurv2.model.HoraireRdv;
import com.esiitech.monbondocteurv2.model.RendezVous;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {
    Optional<RendezVous> findByDateRdvAndHoraireRdv(DateRdv dateRdv, HoraireRdv horaireRdv);
}