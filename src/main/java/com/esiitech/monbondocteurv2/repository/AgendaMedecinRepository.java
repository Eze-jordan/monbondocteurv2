package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.AgendaMedecin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface AgendaMedecinRepository extends JpaRepository<AgendaMedecin, Long> {
    @Query("SELECT a FROM AgendaMedecin a " +
            "WHERE a.medecin.id = :medecinId AND EXISTS (" +
            "SELECT mss FROM MedecinStructureSanitaire mss " +
            "WHERE mss.medecin.id = :medecinId AND mss.structureSanitaire.id = :structureId)")
    List<AgendaMedecin> findByMedecinAndStructure(@Param("medecinId") Long medecinId, @Param("structureId") Long structureId);

    List<AgendaMedecin> findByMedecinId(Long medecinId);

}
