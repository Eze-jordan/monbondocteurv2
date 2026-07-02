package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.AgendaMedecin;
import com.esiitech.monbondocteurv2.enums.JourSemaine;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AgendaMedecinRepository extends JpaRepository<AgendaMedecin, String> {

    // ==================== MÉTHODES EXISTANTES DU PROJET AVEC PAIEMENT ====================

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

    boolean existsByMedecinIdAndStructureSanitaireIdAndJourAndEffectiveFrom(
            String medecinId,
            String structureId,
            JourSemaine jour,
            LocalDate effectiveFrom
    );

    Optional<AgendaMedecin> findByMedecin_IdAndStructureSanitaire_IdAndJourAndEffectiveFrom(
            String medecinId,
            String structureId,
            JourSemaine jour,
            LocalDate effectiveFrom
    );

    List<AgendaMedecin> findByMedecin_IdAndStructureSanitaire_IdAndEffectiveFromLessThanEqual(
            String medecinId,
            String structureId,
            LocalDate dateRef
    );

    @EntityGraph(attributePaths = "plages")
    List<AgendaMedecin> findByMedecin_IdAndEffectiveFromIsNotNullAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            String medecinId,
            LocalDate dateRef
    );

    @EntityGraph(attributePaths = "plages")
    List<AgendaMedecin> findByStructureSanitaire_IdAndEffectiveFromIsNotNullAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            String structureId,
            LocalDate dateRef
    );

    // ==================== MÉTHODES AJOUTÉES DEPUIS LE PROJET SANS PAIEMENT ====================

    Optional<AgendaMedecin> findFirstByMedecin_IdAndStructureSanitaire_IdAndJour(
            String medecinId,
            String structureId,
            JourSemaine jour
    );

    Optional<AgendaMedecin> findFirstByMedecin_IdAndJourAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            String medecinId,
            JourSemaine jour,
            LocalDate date
    );

    @EntityGraph(attributePaths = "plages")
    List<AgendaMedecin> findByMedecinIdAndStructureSanitaireId(
            String medecinId,
            String structureId
    );

    @EntityGraph(attributePaths = "plages")
    List<AgendaMedecin> findByMedecinIdAndStructureSanitaireIdAndJourAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            String medecinId,
            String structureId,
            JourSemaine jour,
            LocalDate dateRef
    );

    Optional<AgendaMedecin> findFirstByMedecinIdAndStructureSanitaireIdAndJourOrderByEffectiveFromDesc(
            String medecinId,
            String structureId,
            JourSemaine jour
    );

    @EntityGraph(attributePaths = "plages")
    List<AgendaMedecin> findByStructureSanitaireIdAndMedecinRefSpecialiteIgnoreCase(
            String structureId,
            String specialite
    );

    @EntityGraph(attributePaths = "plages")
    List<AgendaMedecin> findByMedecinIdAndStructureSanitaireIdAndAutoriseTrue(
            String medecinId,
            String structureId
    );

    boolean existsByMedecinIdAndStructureSanitaireIdAndJour(
            String medecinId,
            String structureId,
            JourSemaine jour
    );
}