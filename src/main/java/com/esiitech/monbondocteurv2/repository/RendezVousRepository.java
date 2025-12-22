package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.RefSpecialite;
import com.esiitech.monbondocteurv2.model.RendezVous;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import org.springframework.data.jpa.repository.JpaRepository;

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
}


