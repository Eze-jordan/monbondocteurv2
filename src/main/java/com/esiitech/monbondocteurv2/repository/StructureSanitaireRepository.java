package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.model.Ville;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StructureSanitaireRepository extends JpaRepository<StructureSanitaire, Long> {
    Optional<StructureSanitaire> findByEmail(String email);
    List<StructureSanitaire> findByVille(Ville ville);

    // Dans StructureSanitaireRepository
    Optional<StructureSanitaire> findByNomStructureSanitaireIgnoreCase(String nomStructureSanitaire);


}