package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.DemandeMedecin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DemandeMedecinRepository extends JpaRepository<DemandeMedecin, Long> {
    Optional<DemandeMedecin> findByEmail(String email);
    boolean existsByEmail(String email);
}
