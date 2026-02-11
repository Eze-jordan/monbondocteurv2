package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.JourneeActivite;
import com.esiitech.monbondocteurv2.model.StatutJournee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JourneeActiviteRepository  extends JpaRepository<JourneeActivite, String> {
    Optional<JourneeActivite> findByDateAndMedecinIdAndStructureSanitaireId(
            LocalDate date,
            String medecinId,
            String structureSanitaireId);
    List<JourneeActivite>
    findByDateBeforeAndStatutNot(LocalDate date, StatutJournee statut);

    Optional<JourneeActivite> findByAgenda_Id(String agendaId);


    List<JourneeActivite> findByMedecin_IdOrderByDateDesc(String medecinId);



    List<JourneeActivite> findByMedecin_IdAndStructureSanitaire_IdAndDateBetween(String medecinId, String structureId, LocalDate start, LocalDate end);

}
