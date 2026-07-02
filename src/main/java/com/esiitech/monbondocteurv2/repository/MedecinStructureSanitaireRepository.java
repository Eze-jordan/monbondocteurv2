package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.MedecinStructureSanitaire;
import com.esiitech.monbondocteurv2.enums.RefSpecialite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            @Param("specialite") RefSpecialite specialite
    );

    Optional<MedecinStructureSanitaire> findFirstByMedecinAndActifTrue(Medecin medecin);

    List<MedecinStructureSanitaire> findByMedecinAndActifTrue(Medecin medecin);

    List<MedecinStructureSanitaire> findByStructureSanitaireId(String structureId);

    List<MedecinStructureSanitaire> findByStructureSanitaireIdAndActifTrue(String structureId);

    boolean existsByStructureSanitaireIdAndMedecinId(String structureId, String medecinId);

    List<MedecinStructureSanitaire> findByMedecinIdAndActifTrue(String medecinId);

    @Query("SELECT mss.medecin FROM MedecinStructureSanitaire mss " +
            "WHERE mss.structureSanitaire.id = :structureId " +
            "AND mss.actif = true " +
            "AND LOWER(TRIM(mss.medecin.refSpecialite)) = LOWER(TRIM(:specialite))")
    List<Medecin> findMedecinsActifsByStructureAndSpecialite(
            @Param("structureId") String structureId,
            @Param("specialite") String specialite
    );

    @Query("SELECT mss.medecin FROM MedecinStructureSanitaire mss " +
            "WHERE mss.structureSanitaire.id = :structureId " +
            "AND mss.actif = true " +
            "AND mss.medecin.refSpecialite = :specialite")
    List<Medecin> findByStructureSanitaireIdAndSpecialite(
            @Param("structureId") String structureId,
            @Param("specialite") String specialite
    );

    @Query("SELECT mss.medecin FROM MedecinStructureSanitaire mss " +
            "WHERE mss.structureSanitaire.id = :structureId " +
            "AND LOWER(TRIM(mss.medecin.refSpecialite)) = LOWER(TRIM(:specialite))")
    List<Medecin> findMedecinsByStructureIdAndSpecialite(
            @Param("structureId") String structureId,
            @Param("specialite") String specialite
    );

    @Query("SELECT COUNT(mss) FROM MedecinStructureSanitaire mss " +
            "WHERE mss.structureSanitaire.id = :structureId " +
            "AND mss.actif = true " +
            "AND LOWER(TRIM(mss.medecin.refSpecialite)) = LOWER(TRIM(:specialite))")
    Long countMedecinsActifsByStructureAndSpecialite(
            @Param("structureId") String structureId,
            @Param("specialite") String specialite
    );

    @Query("SELECT DISTINCT mss.medecin.refSpecialite FROM MedecinStructureSanitaire mss " +
            "WHERE mss.structureSanitaire.id = :structureId " +
            "AND mss.actif = true " +
            "AND mss.medecin.refSpecialite IS NOT NULL")
    List<String> findDistinctSpecialitesByStructureId(
            @Param("structureId") String structureId
    );

    @Query("SELECT mss.medecin FROM MedecinStructureSanitaire mss " +
            "WHERE mss.structureSanitaire.id = :structureId " +
            "AND mss.actif = true")
    Page<Medecin> findMedecinsByStructureIdPaged(
            @Param("structureId") String structureId,
            Pageable pageable
    );

    boolean existsByMedecinIdAndStructureSanitaireIdAndActifTrue(
            String medecinId,
            String structureId
    );

    Optional<MedecinStructureSanitaire> findByMedecinIdAndStructureSanitaireId(
            String medecinId,
            String structureId
    );
}