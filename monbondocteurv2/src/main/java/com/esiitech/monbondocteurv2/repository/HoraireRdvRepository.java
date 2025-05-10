package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.HoraireRdv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HoraireRdvRepository extends JpaRepository<HoraireRdv, Long> {
    List<HoraireRdv> findByDateRdvId(Long dateRdvId);
}