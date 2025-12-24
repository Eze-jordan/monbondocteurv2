package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.AgendaMedecinDto;
import com.esiitech.monbondocteurv2.dto.AgendaSemaineRequest;
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

            throw new RuntimeException(
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
            throw new RuntimeException("Seul un médecin peut gérer son agenda");
        }
    }

    private String generateAgendaId() {
        return "AGENDA-" + java.util.UUID.randomUUID();
    }
    @Transactional
    public AgendaMedecinDto updateDay(AgendaMedecinDto dto) {

        checkIfUserIsMedecin();

        AgendaMedecin agenda = repository
                .findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Agenda introuvable"));

        // 🔒 VÉRIFICATION MÉTIER
        verifierJourneeModifiable(agenda);

        agenda.setAutorise(dto.isAutorise());

        /* 🗂️ ARCHIVER les anciennes plages (PAS DE DELETE) */
        agenda.getPlages().forEach(plage -> {
            plage.setAutorise(false);
            plage.setArchive(true);
        });

        /* ➕ CRÉER les nouvelles plages */
        dto.getPlages().forEach(pDto -> {
            PlageHoraire plage = new PlageHoraire();
            plage.setAgenda(agenda);
            plage.setHeureDebut(pDto.getHeureDebut());
            plage.setHeureFin(pDto.getHeureFin());
            plage.setNombrePatients(pDto.getNombrePatients());
            plage.setAutorise(pDto.isAutorise());
            plage.setArchive(false);
            plage.setPeriode(determinerPeriode(pDto.getHeureDebut()));

            agenda.getPlages().add(plage);
        });

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

            // 🔒 VÉRIFICATION MÉTIER
            verifierJourneeModifiable(agenda);

            agenda.setAutorise(dto.isAutorise());

            /* 🗂️ ARCHIVER anciennes plages */
            agenda.getPlages().forEach(plage -> {
                plage.setAutorise(false);
                plage.setArchive(true);
            });

            /* ➕ NOUVELLES plages */
            dto.getPlages().forEach(pDto -> {
                PlageHoraire plage = new PlageHoraire();
                plage.setAgenda(agenda);
                plage.setHeureDebut(pDto.getHeureDebut());
                plage.setHeureFin(pDto.getHeureFin());
                plage.setNombrePatients(pDto.getNombrePatients());
                plage.setAutorise(pDto.isAutorise());
                plage.setArchive(false);
                plage.setPeriode(determinerPeriode(pDto.getHeureDebut()));

                agenda.getPlages().add(plage);
            });

            return mapper.toDto(repository.save(agenda));

        }).toList();
    }

    private void verifierJourneeModifiable(AgendaMedecin agenda) {

        journeeActiviteRepository
                .findByAgenda_Id(agenda.getId())
                .ifPresent(journee -> {

                    // ❌ Journée ouverte
                    if (journee.isAutorise()) {
                        throw new RuntimeException(
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

}
