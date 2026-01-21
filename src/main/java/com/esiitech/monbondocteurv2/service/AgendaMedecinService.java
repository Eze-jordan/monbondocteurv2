package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.AgendaMedecinDto;
import com.esiitech.monbondocteurv2.dto.AgendaSemaineRequest;
import com.esiitech.monbondocteurv2.dto.AgendaWeekStatusRequest;
import com.esiitech.monbondocteurv2.exception.AccesRefuseException;
import com.esiitech.monbondocteurv2.exception.AgendaExisteDejaException;
import com.esiitech.monbondocteurv2.exception.AgendaNonModifiableException;
import com.esiitech.monbondocteurv2.exception.DisponibiliteConflitException;
import com.esiitech.monbondocteurv2.mapper.AgendaMedecinMapper;
import com.esiitech.monbondocteurv2.model.*;
import com.esiitech.monbondocteurv2.repository.AgendaMedecinRepository;
import com.esiitech.monbondocteurv2.repository.JourneeActiviteRepository;
import com.esiitech.monbondocteurv2.repository.RendezVousRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalTime;
import java.util.List;

@Service
public class AgendaMedecinService {

    @Autowired private AgendaMedecinRepository repository;
    @Autowired private AgendaMedecinMapper mapper;
    @Autowired private MedecinStructureSanitaireService medecinStructureSanitaireService;
    @Autowired private JourneeActiviteRepository journeeActiviteRepository;
    @Autowired private RendezVousRepository rendezVousRepository;

    /* =========================
       CRÉATION / MODIFICATION
       ========================= */
    @Transactional
    public AgendaMedecinDto save(AgendaMedecinDto dto) {

        checkIfUserIsMedecin();

        if (repository.existsByMedecinIdAndStructureSanitaireIdAndJour(
                dto.getMedecinId(),
                dto.getStructureSanitaireId(),
                dto.getJour())) {

            throw new AgendaExisteDejaException(
                    "Un agenda existe déjà pour ce jour dans cette structure"
            );
        }

        if (dto.getId() == null) {
            dto.setId(generateAgendaId());
        }

        // Convertir DTO en entité
        AgendaMedecin agenda = mapper.toEntity(dto);

        // Remplir la période pour chaque plage horaire
        if (agenda.getPlages() != null) {
            agenda.getPlages().forEach(plage -> {
                if (plage.getPeriode() == null && plage.getHeureDebut() != null) {
                    plage.setPeriode(determinerPeriode(plage.getHeureDebut()));
                }
            });
        }

        AgendaMedecin saved = repository.save(agenda);
        return mapper.toDto(saved);
    }

    @Transactional
    public List<AgendaMedecinDto> saveWeek(AgendaSemaineRequest request) {

        checkIfUserIsMedecin();

        return request.getAgendas().stream().map(dto -> {


            dto.setId(generateAgendaId());
            dto.setMedecinId(request.getMedecinId());
            dto.setStructureSanitaireId(request.getStructureSanitaireId());

            AgendaMedecin agenda = mapper.toEntity(dto);

            // Déterminer MATIN / SOIR automatiquement
            if (agenda.getPlages() != null) {
                agenda.getPlages().forEach(plage -> {
                    if (plage.getPeriode() == null && plage.getHeureDebut() != null) {
                        plage.setPeriode(determinerPeriode(plage.getHeureDebut()));
                    }
                });
            }

            return mapper.toDto(repository.save(agenda));

        }).toList();
    }




    /* =========================
       LECTURE
       ========================= */
    public List<AgendaMedecinDto> getAllByMedecin(String medecinId) {
        return repository.findByMedecinId(medecinId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<AgendaMedecinDto> getByStructure(String structureId) {
        return repository.findByStructureSanitaireId(structureId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    /* =========================
       SUPPRESSION
       ========================= */
    public void delete(String agendaId) {
        repository.deleteById(agendaId);
    }

    /* =========================
       UTILITAIRE PRIVÉ
       ========================= */
    private PeriodeJournee determinerPeriode(LocalTime heureDebut) {
        return heureDebut.isBefore(LocalTime.NOON)
                ? PeriodeJournee.MATIN
                : PeriodeJournee.SOIR;
    }

    private void checkIfUserIsMedecin() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Medecin medecin = medecinStructureSanitaireService
                .getMedecinByEmail(email)
                .orElseThrow(() -> new RuntimeException("Accès refusé"));

        if (!"MEDECIN".equals(medecin.getRole().name())) {
            throw new AccesRefuseException("Seul un médecin peut gérer son agenda");
        }
    }

    private String generateAgendaId() {
        return "AGENDA-" + java.util.UUID.randomUUID();
    }
    @Transactional
    public AgendaMedecinDto updateDay(AgendaMedecinDto dto) {

        checkIfUserIsMedecin();

        AgendaMedecin agenda = repository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Agenda introuvable"));

        verifierJourneeModifiable(agenda);

        agenda.setAutorise(dto.isAutorise());

        // Indexer les plages existantes par id
        var existingById = agenda.getPlages().stream()
                .collect(java.util.stream.Collectors.toMap(
                        PlageHoraire::getId,
                        p -> p
                ));

        // ✅ On UPDATE uniquement
        for (var pDto : dto.getPlages()) {

            if (pDto.getId() == null || pDto.getId().isBlank()) {
                throw new RuntimeException("Modification refusée : une plage sans id créerait une nouvelle entrée.");
            }

            PlageHoraire existing = existingById.get(pDto.getId());
            if (existing == null) {
                throw new RuntimeException("Plage introuvable (id=" + pDto.getId() + ") pour cet agenda.");
            }

            existing.setHeureDebut(pDto.getHeureDebut());
            existing.setHeureFin(pDto.getHeureFin());
            existing.setNombrePatients(pDto.getNombrePatients());
            existing.setAutorise(pDto.isAutorise());

            // si tu veux recalculer la période automatiquement :
            existing.setPeriode(determinerPeriode(pDto.getHeureDebut()));

            // surtout ne pas toucher archive ici (tu peux même supprimer le concept)
            existing.setArchive(false);
        }

        return mapper.toDto(repository.save(agenda));
    }


    @Transactional
    public List<AgendaMedecinDto> updateWeek(AgendaSemaineRequest request) {

        checkIfUserIsMedecin();

        return request.getAgendas().stream().map(dto -> {

            AgendaMedecin agenda = repository
                    .findByMedecinIdAndStructureSanitaireIdAndJour(
                            request.getMedecinId(),
                            request.getStructureSanitaireId(),
                            dto.getJour()
                    )
                    .orElseThrow(() ->
                            new RuntimeException("Agenda inexistant pour " + dto.getJour())
                    );

            verifierJourneeModifiable(agenda);

            agenda.setAutorise(dto.isAutorise());

            var existingById = agenda.getPlages().stream()
                    .collect(java.util.stream.Collectors.toMap(PlageHoraire::getId, p -> p));

            for (var pDto : dto.getPlages()) {

                if (pDto.getId() == null || pDto.getId().isBlank()) {
                    throw new RuntimeException("Modification refusée : une plage sans id créerait une nouvelle entrée.");
                }

                PlageHoraire existing = existingById.get(pDto.getId());
                if (existing == null) {
                    throw new RuntimeException("Plage introuvable (id=" + pDto.getId() + ") pour " + dto.getJour());
                }

                existing.setHeureDebut(pDto.getHeureDebut());
                existing.setHeureFin(pDto.getHeureFin());
                existing.setNombrePatients(pDto.getNombrePatients());
                existing.setAutorise(pDto.isAutorise());
                existing.setPeriode(determinerPeriode(pDto.getHeureDebut()));
                existing.setArchive(false);
            }

            return mapper.toDto(repository.save(agenda));

        }).toList();
    }


    private void verifierJourneeModifiable(AgendaMedecin agenda) {

        journeeActiviteRepository
                .findByAgenda_Id(agenda.getId())
                .ifPresent(journee -> {

                    // ❌ Journée ouverte
                    if (journee.isAutorise()) {
                        throw new AgendaNonModifiableException(
                                "Impossible de modifier : la journée est ouverte"
                        );
                    }

                    // ❌ RDV actifs existants
                    int rdvActifs = rendezVousRepository
                            .countByJourneeActivite_IdAndActifTrue(journee.getId());

                    if (rdvActifs > 0) {
                        throw new RuntimeException(
                                "Impossible de modifier : des rendez-vous sont encore actifs"
                        );
                    }
                });
    }
    public List<AgendaMedecinDto> updateWeekAutorisation(AgendaWeekStatusRequest request) {

        List<AgendaMedecin> agendas = repository
                .findByMedecin_IdAndStructureSanitaire_Id(
                        request.getMedecinId(),
                        request.getStructureSanitaireId()
                );

        agendas.forEach(agenda -> agenda.setAutorise(request.isAutorise()));

        repository.saveAll(agendas);

        return agendas.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public AgendaMedecinDto updatePlagesAutorisationByDay(com.esiitech.monbondocteurv2.dto.PlagesDayStatusRequest request) {

        checkIfUserIsMedecin();

        AgendaMedecin agenda = repository
                .findByMedecinIdAndStructureSanitaireIdAndJour(
                        request.getMedecinId(),
                        request.getStructureSanitaireId(),
                        request.getJour()
                )
                .orElseThrow(() -> new RuntimeException("Agenda introuvable pour " + request.getJour()));

        // si tu veux empêcher modification quand journée ouverte / rdv actifs
        verifierJourneeModifiable(agenda);

        if (agenda.getPlages() == null || agenda.getPlages().isEmpty()) {
            return mapper.toDto(agenda); // rien à modifier
        }

        // ✅ Mettre à jour toutes les plages de ce jour
        agenda.getPlages().forEach(plage -> plage.setAutorise(request.isAutorise()));

        // Optionnel : si tu veux aussi couper la journée entière
        // agenda.setAutorise(request.isAutorise());

        return mapper.toDto(repository.save(agenda));
    }


}
