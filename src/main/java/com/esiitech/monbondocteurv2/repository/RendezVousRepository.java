package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.*;
import com.esiitech.monbondocteurv2.enums.StatutRendezVous;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RendezVousRepository extends JpaRepository<RendezVous, String> {

    // 🔍 Par structure sanitaire
    List<RendezVous> findByStructureSanitaire(StructureSanitaire structureSanitaire);

    int countByJourneeActivite_IdAndActifTrue(String journeeId);

    // 🔍 Par médecin
    List<RendezVous> findByMedecin(Medecin medecin);

    List<RendezVous> findByMedecin_Id(String medecinId);

    List<RendezVous> findByAgendaMedecin_Id(String agendaId);

    int countByJourneeActivite_IdAndEmail(String journeeId, String email);

    List<RendezVous> findByJourneeActivite_Id(String journeeId);

    default List<RendezVous> findEnAttenteByStructureAndService(String structureId, String specialite) {
        return findByStructureAndSpecialiteAndStatut(
                structureId,
                specialite,
                StatutRendezVous.EN_ATTENTE
        );
    }

    @Query("""
        SELECT r
        FROM RendezVous r
        JOIN r.refSpecialites sp
        WHERE r.structureSanitaire.id = :structureId
        AND r.statut = :statut
        AND (:specialite IS NULL OR lower(sp) = lower(:specialite))
    """)
    List<RendezVous> findByStructureAndSpecialiteAndStatut(
            @Param("structureId") String structureId,
            @Param("specialite") String specialite,
            @Param("statut") StatutRendezVous statut
    );

    List<RendezVous> findByJourneeActivite_IdOrderByHeureDebutAsc(String journeeId);

    // ✅ Utilisé pour calculer la capacité par créneau dans une journée
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

    boolean existsByPlageHoraire_Id(String plageId);

    List<RendezVous> findByUtilisateur_IdOrderByDateDescHeureDebutDesc(String utilisateurId);

    List<RendezVous> findByUtilisateur_IdAndActifTrueAndArchiveFalseOrderByDateDescHeureDebutDesc(String utilisateurId);
}