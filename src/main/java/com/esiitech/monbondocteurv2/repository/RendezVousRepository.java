package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.enums.StatutRendezVous;
import com.esiitech.monbondocteurv2.model.AgendaMedecin;
import com.esiitech.monbondocteurv2.model.JourneeActivite;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.PlageHoraire;
import com.esiitech.monbondocteurv2.model.RendezVous;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface RendezVousRepository extends JpaRepository<RendezVous, String> {

    // ==================== STRUCTURE SANITAIRE ====================

    List<RendezVous> findByStructureSanitaire(StructureSanitaire structureSanitaire);

    List<RendezVous> findByStructureSanitaire_IdAndStatut(
            String structureId,
            StatutRendezVous statut
    );

    // ==================== MÉDECIN ====================

    List<RendezVous> findByMedecin(Medecin medecin);

    List<RendezVous> findByMedecin_Id(String medecinId);

    List<RendezVous> findByMedecinIdAndStructureSanitaireId(
            String medecinId,
            String structureId
    );

    // ==================== AGENDA / JOURNÉE / PLAGE ====================

    List<RendezVous> findByAgendaMedecin_Id(String agendaId);

    List<RendezVous> findByJourneeActivite_Id(String journeeId);

    List<RendezVous> findByJourneeActivite_IdOrderByHeureDebutAsc(String journeeId);

    int countByJourneeActivite_IdAndActifTrue(String journeeId);

    int countByJourneeActivite_IdAndEmail(
            String journeeId,
            String email
    );

    int countByJourneeActivite_IdAndPlageHoraire_IdAndActifTrueAndArchiveFalse(
            String journeeId,
            String plageId
    );

    int countByJourneeActivite_IdAndActifTrueAndArchiveFalse(String journeeId);

    int countByMedecin_IdAndStructureSanitaire_IdAndDateBetweenAndActifTrueAndArchiveFalse(
            String medecinId,
            String structureId,
            LocalDate start,
            LocalDate end
    );

    @Query("SELECT COUNT(r) FROM RendezVous r " +
            "WHERE r.journeeActivite.id = :journeeId " +
            "AND r.plageHoraire.id = :plageId " +
            "AND r.heureDebut = :heureDebut " +
            "AND r.actif = true " +
            "AND r.archive = false")
    int countByJourneeActivite_IdAndPlageHoraire_IdAndHeureDebut(
            @Param("journeeId") String journeeId,
            @Param("plageId") String plageId,
            @Param("heureDebut") LocalTime heureDebut
    );

    boolean existsByPlageHoraire_Id(String plageId);

    // ==================== UTILISATEUR / PATIENT ====================

    List<RendezVous> findByUtilisateur_IdOrderByDateDescHeureDebutDesc(String utilisateurId);

    List<RendezVous> findByUtilisateur_IdAndActifTrueAndArchiveFalseOrderByDateDescHeureDebutDesc(
            String utilisateurId
    );

    // ==================== RDV EN ATTENTE PAR STRUCTURE ET SERVICE ====================

    default List<RendezVous> findEnAttenteByStructureAndService(
            String structureId,
            String specialite
    ) {
        return findByStructureAndSpecialiteAndStatut(
                structureId,
                specialite,
                StatutRendezVous.EN_ATTENTE
        );
    }

    @Query("SELECT r FROM RendezVous r " +
            "JOIN r.refSpecialites sp " +
            "WHERE r.structureSanitaire.id = :structureId " +
            "AND r.statut = :statut " +
            "AND (:specialite IS NULL OR LOWER(sp) = LOWER(:specialite))")
    List<RendezVous> findByStructureAndSpecialiteAndStatut(
            @Param("structureId") String structureId,
            @Param("specialite") String specialite,
            @Param("statut") StatutRendezVous statut
    );

    // ==================== STATISTIQUES STRUCTURE / MÉDECINS ====================

    @Query("SELECT COUNT(r) FROM RendezVous r " +
            "WHERE r.medecin.id IN :medecinIds " +
            "AND r.structureSanitaire.id = :structureId " +
            "AND r.statut = com.esiitech.monbondocteurv2.enums.StatutRendezVous.CONFIRME " +
            "AND r.archive = false")
    Long countByMedecinIdsAndStructureId(
            @Param("medecinIds") List<String> medecinIds,
            @Param("structureId") String structureId
    );

    @Query("SELECT COUNT(r) FROM RendezVous r " +
            "WHERE r.medecin.id = :medecinId " +
            "AND r.structureSanitaire.id = :structureId " +
            "AND r.statut = com.esiitech.monbondocteurv2.enums.StatutRendezVous.CONFIRME " +
            "AND r.archive = false")
    Long countByMedecinIdAndStructureSanitaireId(
            @Param("medecinId") String medecinId,
            @Param("structureId") String structureId
    );

    @Query("SELECT COUNT(r) FROM RendezVous r " +
            "WHERE r.medecin.id = :medecinId " +
            "AND r.structureSanitaire.id = :structureId " +
            "AND r.date BETWEEN :start AND :end " +
            "AND r.statut = com.esiitech.monbondocteurv2.enums.StatutRendezVous.CONFIRME " +
            "AND r.archive = false")
    Long countByMedecinIdAndStructureIdAndDateBetween(
            @Param("medecinId") String medecinId,
            @Param("structureId") String structureId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("SELECT r FROM RendezVous r " +
            "WHERE r.medecin.id = :medecinId " +
            "AND r.structureSanitaire.id = :structureId " +
            "AND r.archive = false " +
            "ORDER BY r.date DESC, r.heureDebut DESC")
    List<RendezVous> findAllByMedecinIdAndStructureSanitaireId(
            @Param("medecinId") String medecinId,
            @Param("structureId") String structureId
    );

    @Query("SELECT r FROM RendezVous r " +
            "WHERE r.medecin.id = :medecinId " +
            "AND r.structureSanitaire.id = :structureId " +
            "AND r.date >= CURRENT_DATE " +
            "AND r.archive = false " +
            "ORDER BY r.date ASC, r.heureDebut ASC")
    List<RendezVous> findFutursByMedecinIdAndStructureSanitaireId(
            @Param("medecinId") String medecinId,
            @Param("structureId") String structureId
    );

    @Query("SELECT r FROM RendezVous r " +
            "WHERE r.medecin.id = :medecinId " +
            "AND r.structureSanitaire.id = :structureId " +
            "AND r.date < CURRENT_DATE " +
            "AND r.archive = false " +
            "ORDER BY r.date DESC, r.heureDebut DESC")
    List<RendezVous> findPassesByMedecinIdAndStructureSanitaireId(
            @Param("medecinId") String medecinId,
            @Param("structureId") String structureId
    );

    @Query("SELECT r.statut, COUNT(r) FROM RendezVous r " +
            "WHERE r.medecin.id = :medecinId " +
            "AND r.structureSanitaire.id = :structureId " +
            "AND r.archive = false " +
            "GROUP BY r.statut")
    List<Object[]> countByStatutAndMedecinIdAndStructureSanitaireId(
            @Param("medecinId") String medecinId,
            @Param("structureId") String structureId
    );

    @Query("SELECT COUNT(r) FROM RendezVous r " +
            "WHERE r.statut = com.esiitech.monbondocteurv2.enums.StatutRendezVous.CONFIRME " +
            "AND r.archive = false")
    long countByStatutAndArchiveFalse();
}