package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.RefSpecialite;
import com.esiitech.monbondocteurv2.model.RendezVous;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RendezVousRepository extends JpaRepository<RendezVous, String> {
    int countByAgendaMedecinIdAndEmail(String agendaId, String email);
    // 🔍 Par spécialité
    List<RendezVous> findByRefSpecialitesContaining(RefSpecialite specialite);

    // 🔍 Par structure sanitaire
    List<RendezVous> findByStructureSanitaire(StructureSanitaire structureSanitaire);
    int countByJourneeActivite_IdAndActifTrue(String journeeId);

    // 🔍 Par médecin
    List<RendezVous> findByMedecin(Medecin medecin);

    List<RendezVous> findByMedecin_Id(String medecinId);
    List<RendezVous> findByAgendaMedecin_Id(String agendaId);

    int countByJourneeActivite_IdAndEmail(String journeeId, String email);
    int countByJourneeActivite_Id(String journeeId);


    List<RendezVous> findByJourneeActivite_Id(String journeeId);

    @Query("""
SELECT r FROM RendezVous r
JOIN r.refSpecialites sp
WHERE r.structureSanitaire.id = :structureId
AND r.statut = com.esiitech.monbondocteurv2.model.StatutRendezVous.EN_ATTENTE
AND (:specialite IS NULL OR lower(sp) = lower(:specialite))
""")
    List<RendezVous> findEnAttenteByStructureAndService(String structureId, String specialite);
    List<RendezVous> findByJourneeActivite_IdOrderByHeureDebutAsc(String journeeId);
    // ✅ Nouveau : utilisé pour calculer la capacité par créneau dans une journée
    int countByJourneeActivite_IdAndPlageHoraire_IdAndActifTrueAndArchiveFalse(
            String journeeId,
            String plageId
    );

    int countByJourneeActivite_IdAndActifTrueAndArchiveFalse(String journeeId);

    int countByJourneeActivite_IdAndEmailAndActifTrueAndArchiveFalse(String journeeId, String email);

}


