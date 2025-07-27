package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.model.Ville;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StructureSanitaireRepository extends JpaRepository<StructureSanitaire, String> {
    Optional<StructureSanitaire> findByEmail(String email);
    List<StructureSanitaire> findByVilleIgnoreCase(String ville);

    // Dans StructureSanitaireRepository
    Optional<StructureSanitaire> findByNomStructureSanitaireIgnoreCase(String nomStructureSanitaire);
    @Query("SELECT s FROM StructureSanitaire s WHERE LOWER(s.refSpecialites) LIKE LOWER(CONCAT('%', :specialite, '%'))")
    List<StructureSanitaire> findBySpecialiteContainingIgnoreCase(@Param("specialite") String specialite);


}