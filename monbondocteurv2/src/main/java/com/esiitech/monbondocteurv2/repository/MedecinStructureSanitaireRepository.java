package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.MedecinStructureSanitaire;
import com.esiitech.monbondocteurv2.model.RefSpecialite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedecinStructureSanitaireRepository extends JpaRepository<MedecinStructureSanitaire, String> {
    @Query("SELECT mss.medecin FROM MedecinStructureSanitaire mss " +
            "WHERE mss.structureSanitaire.id = :structureId " +
            "AND mss.medecin.refSpecialite = :specialite")
    List<Medecin> findMedecinsByStructureAndSpecialite(
            @Param("structureId") Long structureId,
            @Param("specialite") RefSpecialite specialite);

    Optional<MedecinStructureSanitaire> findByMedecinAndActifTrue(Medecin medecin);


}