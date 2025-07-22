package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.model.Utilisateur;
import com.esiitech.monbondocteurv2.model.Validation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ValidationRipository extends JpaRepository<Validation, String> {

    Optional<Validation> findByCode(String code);
    Optional<Validation> findByUtilisateur(Utilisateur utilisateur);
    Optional<Validation> findByMedecin(Medecin medecin);
    Optional<Validation> findByStructureSanitaire(StructureSanitaire structureSanitaire);
}

