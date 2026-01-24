package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.JourneeActivite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JourneeActiviteRepository  extends JpaRepository<JourneeActivite, String> {
    Optional<JourneeActivite> findByDateAndMedecinIdAndStructureSanitaireId(
            LocalDate date,
            String medecinId,
            String structureSanitaireId);

    List<JourneeActivite> findByMedecinId(String medecinId);

    List<JourneeActivite> findByStructureSanitaireId(String structureId);
    Optional<JourneeActivite> findByDateAndAgenda_Id(LocalDate date, String agendaId);

    Optional<JourneeActivite> findByAgenda_Id(String agendaId);


    List<JourneeActivite> findByMedecin_IdOrderByDateDesc(String medecinId);

    // optionnel: filtrer par période
    List<JourneeActivite> findByMedecin_IdAndDateBetweenOrderByDateDesc(
            String medecinId, LocalDate start, LocalDate end
    );

}
