package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StructureSanitaireRepository extends JpaRepository<StructureSanitaire, Long> {
    Optional<StructureSanitaire> findByEmail(String email);

}