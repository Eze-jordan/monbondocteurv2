package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.AgendaMedecin;
import com.esiitech.monbondocteurv2.model.JourSemaine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface AgendaMedecinRepository extends JpaRepository<AgendaMedecin, String> {
    List<AgendaMedecin> findByMedecinId(String medecinId);

    boolean existsByMedecinIdAndStructureSanitaireIdAndJour(
            String medecinId,
            String structureId,
            JourSemaine jour
    );
    Optional<AgendaMedecin> findByMedecin_IdAndStructureSanitaire_IdAndJour(
            String medecinId,
            String structureSanitaireId,
            JourSemaine jour
    );

    Optional<AgendaMedecin> findByMedecinIdAndStructureSanitaireIdAndJour(
            String medecinId,
            String structureSanitaireId,
            JourSemaine jour
    );

    List<AgendaMedecin> findByStructureSanitaireId(String structureId);

    List<AgendaMedecin> findByMedecin_IdAndStructureSanitaire_Id(
            String medecinId,
            String structureSanitaireId
    );
}
