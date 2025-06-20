package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.DateRdv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DateRdvRepository extends JpaRepository<DateRdv, Long> {
    List<DateRdv> findByAgendaMedecin_Medecin_Id(Long medecinId);
}