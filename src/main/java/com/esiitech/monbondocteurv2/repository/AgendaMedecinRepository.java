package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.AgendaMedecin;
import com.esiitech.monbondocteurv2.model.JourSemaine;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface AgendaMedecinRepository extends JpaRepository<AgendaMedecin, String> {
    List<AgendaMedecin> findByMedecinId(String medecinId);


    Optional<AgendaMedecin> findByMedecin_IdAndStructureSanitaire_IdAndJour(
            String medecinId,
            String structureSanitaireId,
            JourSemaine jour
    );



    List<AgendaMedecin> findByStructureSanitaireId(String structureId);

    List<AgendaMedecin> findByMedecin_IdAndStructureSanitaire_Id(
            String medecinId,
            String structureSanitaireId
    );

    Optional<AgendaMedecin> findFirstByMedecin_IdAndStructureSanitaire_IdAndJourAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            String medecinId,
            String structureId,
            JourSemaine jour,
            LocalDate date
    );



    boolean existsByMedecinIdAndStructureSanitaireIdAndJourAndEffectiveFrom(String id, String id1, JourSemaine jour, LocalDate effectiveFrom);
    java.util.Optional<AgendaMedecin> findByMedecin_IdAndStructureSanitaire_IdAndJourAndEffectiveFrom(
            String medecinId,
            String structureId,
            JourSemaine jour,
            LocalDate effectiveFrom
    );
    List<AgendaMedecin> findByMedecin_IdAndStructureSanitaire_IdAndEffectiveFromLessThanEqual(
            String medecinId, String structureId, LocalDate dateRef
    );

    // ====== POUR /medecin/{id} ======
    @EntityGraph(attributePaths = "plages")
    List<AgendaMedecin> findByMedecin_IdAndEffectiveFromIsNotNullAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            String medecinId,
            LocalDate dateRef
    );

    // ====== POUR /structure/{id} ======
    @EntityGraph(attributePaths = "plages")
    List<AgendaMedecin> findByStructureSanitaire_IdAndEffectiveFromIsNotNullAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            String structureId,
            LocalDate dateRef
    );



}
