package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.AgendaMedecinDto;
import com.esiitech.monbondocteurv2.dto.AgendaSemainePlanifieeRequest;
import com.esiitech.monbondocteurv2.dto.AgendaSemaineRequest;
import com.esiitech.monbondocteurv2.dto.AgendaWeekStatusRequest;
import com.esiitech.monbondocteurv2.enums.AgendaUpdatePolicy;
import com.esiitech.monbondocteurv2.enums.JourSemaine;
import com.esiitech.monbondocteurv2.enums.PeriodeJournee;
import com.esiitech.monbondocteurv2.enums.StatutJournee;
import com.esiitech.monbondocteurv2.exception.AccesRefuseException;
import com.esiitech.monbondocteurv2.exception.AgendaIntrouvableException;
import com.esiitech.monbondocteurv2.exception.AgendaNonModifiableException;
import com.esiitech.monbondocteurv2.exception.SemaineNonModifiableException;
import com.esiitech.monbondocteurv2.mapper.AgendaMedecinMapper;
import com.esiitech.monbondocteurv2.model.AgendaMedecin;
import com.esiitech.monbondocteurv2.model.JourneeActivite;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.PlageHoraire;
import com.esiitech.monbondocteurv2.model.RendezVous;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.repository.AgendaMedecinRepository;
import com.esiitech.monbondocteurv2.repository.JourneeActiviteRepository;
import com.esiitech.monbondocteurv2.repository.MedecinRepository;
import com.esiitech.monbondocteurv2.repository.RendezVousRepository;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AgendaMedecinService {

    @Autowired
    private AgendaMedecinRepository repository;

    @Autowired
    private AgendaMedecinMapper mapper;

    @Autowired
    private JourneeActiviteRepository journeeActiviteRepository;

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private MedecinRepository medecinRepository;

    @Autowired
    private StructureSanitaireRepository structureSanitaireRepository;

    @Autowired
    private AbonnementStructureService abonnementStructureService;

    /* =========================
       CRÉATION / MODIFICATION
       ========================= */

    @Transactional
    public AgendaMedecinDto save(AgendaMedecinDto dto) {
        verifierAccesStructure(dto.getStructureSanitaireId());

        if (dto.getId() == null) {
            dto.setId(generateAgendaId());
        }

        AgendaMedecin agenda = mapper.toEntity(dto);

        if (agenda.getEffectiveFrom() == null) {
            agenda.setEffectiveFrom(lundi(LocalDate.now()));
        }

        if (agenda.getPlages() != null) {
            agenda.getPlages().forEach(plage -> {
                plage.setAgenda(agenda);

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
        verifierAccesStructure(request.getStructureSanitaireId());

        return request.getAgendas().stream().map(dto -> {
            dto.setId(generateAgendaId());
            dto.setMedecinId(request.getMedecinId());
            dto.setStructureSanitaireId(request.getStructureSanitaireId());

            AgendaMedecin agenda = mapper.toEntity(dto);

            if (agenda.getEffectiveFrom() == null) {
                agenda.setEffectiveFrom(lundi(LocalDate.now()));
            }

            if (agenda.getPlages() != null) {
                agenda.getPlages().forEach(plage -> {
                    plage.setAgenda(agenda);

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
        verifierAccesStructure(structureId);

        return repository.findByStructureSanitaireId(structureId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    /* =========================
       SUPPRESSION
       ========================= */

    @Transactional
    public void delete(String agendaId) {
        AgendaMedecin agenda = repository.findById(agendaId)
                .orElseThrow(() -> new RuntimeException("Agenda introuvable"));

        verifierAccesStructure(agenda.getStructureSanitaire().getId());

        repository.delete(agenda);
    }

    /* =========================
       UPDATE JOUR
       ========================= */

    @Transactional
    public AgendaMedecinDto updateDay(AgendaMedecinDto dto) {
        checkIfUserIsMedecin();

        AgendaMedecin agenda = repository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Agenda introuvable"));

        verifierAccesStructure(agenda.getStructureSanitaire().getId());
        verifierJourneeModifiable(agenda);

        agenda.setAutorise(dto.isAutorise());

        if (dto.getPlages() != null && !dto.getPlages().isEmpty()) {
            Map<String, PlageHoraire> existingById = agenda.getPlages()
                    .stream()
                    .filter(plage -> plage.getId() != null)
                    .collect(Collectors.toMap(PlageHoraire::getId, plage -> plage, (a, b) -> a));

            List<PlageHoraire> nouvellesPlages = new ArrayList<>();

            for (var pDto : dto.getPlages()) {
                if (pDto.getId() != null && !pDto.getId().isBlank()) {
                    PlageHoraire existing = existingById.get(pDto.getId());

                    if (existing == null) {
                        throw new RuntimeException("Plage introuvable (id=" + pDto.getId() + ") pour cet agenda.");
                    }

                    existing.setHeureDebut(pDto.getHeureDebut());
                    existing.setHeureFin(pDto.getHeureFin());
                    existing.setNombrePatients(pDto.getNombrePatients());
                    existing.setAutorise(pDto.isAutorise());
                    existing.setPeriode(determinerPeriode(pDto.getHeureDebut()));
                    existing.setArchive(false);

                    nouvellesPlages.add(existing);
                } else {
                    PlageHoraire nouvellePlage = new PlageHoraire();
                    nouvellePlage.setId(UUID.randomUUID().toString());
                    nouvellePlage.setAgenda(agenda);
                    nouvellePlage.setHeureDebut(pDto.getHeureDebut());
                    nouvellePlage.setHeureFin(pDto.getHeureFin());
                    nouvellePlage.setNombrePatients(pDto.getNombrePatients());
                    nouvellePlage.setAutorise(pDto.isAutorise());
                    nouvellePlage.setPeriode(determinerPeriode(pDto.getHeureDebut()));
                    nouvellePlage.setArchive(false);

                    nouvellesPlages.add(nouvellePlage);
                }
            }

            agenda.setPlages(nouvellesPlages);
        }

        return mapper.toDto(repository.save(agenda));
    }

    /* =========================
       UPDATE SEMAINE COURANTE
       ========================= */

    @Transactional
    public List<AgendaMedecinDto> updateWeekCurrent(AgendaSemaineRequest request) {
        checkIfUserIsMedecin();

        String medecinId = request.getMedecinId();
        String structureId = request.getStructureSanitaireId();

        verifierAccesStructure(structureId);

        LocalDate start = lundi(LocalDate.now());
        LocalDate end = start.plusDays(6);

        int rdvSemaine = rendezVousRepository
                .countByMedecin_IdAndStructureSanitaire_IdAndDateBetweenAndActifTrueAndArchiveFalse(
                        medecinId,
                        structureId,
                        start,
                        end
                );

        if (rdvSemaine > 0) {
            throw new SemaineNonModifiableException(
                    "Modification refusée : des rendez-vous existent sur la semaine en cours (" + start + ")."
            );
        }

        return request.getAgendas().stream().map(dto -> {
            AgendaMedecin agenda = repository
                    .findFirstByMedecin_IdAndStructureSanitaire_IdAndJourAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                            medecinId,
                            structureId,
                            dto.getJour(),
                            LocalDate.now()
                    )
                    .orElseThrow(() -> new AgendaIntrouvableException(
                            "Agenda introuvable pour " + dto.getJour()
                    ));

            journeeActiviteRepository.findByAgenda_Id(agenda.getId()).ifPresent(journee -> {
                if (journee.isAutorise()) {
                    throw new AgendaNonModifiableException(
                            "Impossible de modifier : la journée d'activité est ouverte pour " + dto.getJour()
                    );
                }
            });

            agenda.setAutorise(dto.isAutorise());

            Map<String, PlageHoraire> existingById = agenda.getPlages()
                    .stream()
                    .filter(plage -> plage.getId() != null)
                    .collect(Collectors.toMap(PlageHoraire::getId, plage -> plage, (a, b) -> a));

            for (var pDto : dto.getPlages()) {
                if (pDto.getId() == null || pDto.getId().isBlank()) {
                    throw new IllegalArgumentException("Modification refusée : plage sans id.");
                }

                PlageHoraire existing = existingById.get(pDto.getId());

                if (existing == null) {
                    throw new AgendaIntrouvableException(
                            "Plage introuvable (id=" + pDto.getId() + ") pour " + dto.getJour()
                    );
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

    public List<AgendaMedecinDto> updateWeekAutorisation(AgendaWeekStatusRequest request) {
        verifierAccesStructure(request.getStructureSanitaireId());

        List<AgendaMedecin> agendas = repository.findByMedecin_IdAndStructureSanitaire_Id(
                request.getMedecinId(),
                request.getStructureSanitaireId()
        );

        agendas.forEach(agenda -> agenda.setAutorise(request.isAutorise()));

        repository.saveAll(agendas);

        return agendas.stream()
                .map(mapper::toDto)
                .toList();
    }

    /* =========================
       PLANIFICATION SEMAINE FUTURE
       ========================= */

    @Transactional
    public LocalDate planifierUpdateWeek(AgendaSemainePlanifieeRequest request) {
        checkIfUserIsMedecin();

        String medecinId = request.getMedecinId();
        String structureId = request.getStructureSanitaireId();

        verifierAccesStructure(structureId);

        AgendaUpdatePolicy policy = request.getPolicy() != null
                ? request.getPolicy()
                : AgendaUpdatePolicy.SHIFT_TO_NEXT_FREE_WEEK;

        LocalDate baseDate = request.getWeekStart() != null
                ? request.getWeekStart()
                : LocalDate.now();

        LocalDate start = lundi(baseDate);
        LocalDate end = start.plusDays(6);

        while (true) {
            int rdv = rendezVousRepository
                    .countByMedecin_IdAndStructureSanitaire_IdAndDateBetweenAndActifTrueAndArchiveFalse(
                            medecinId,
                            structureId,
                            start,
                            end
                    );

            if (rdv == 0) {
                break;
            }

            if (policy == AgendaUpdatePolicy.REFUSE_IF_CONFLICT) {
                throw new RuntimeException("Impossible : il existe des RDV sur la semaine " + start);
            }

            if (policy == AgendaUpdatePolicy.CANCEL_RDV_AND_APPLY) {
                annulerRdvsSemaine(medecinId, structureId, start, end);
                break;
            }

            start = start.plusWeeks(1);
            end = start.plusDays(6);
        }

        for (AgendaMedecinDto dto : request.getAgendas()) {
            var opt = repository.findByMedecin_IdAndStructureSanitaire_IdAndJourAndEffectiveFrom(
                    medecinId,
                    structureId,
                    dto.getJour(),
                    start
            );

            AgendaMedecin agenda;

            if (opt.isPresent()) {
                agenda = opt.get();
            } else {
                Medecin medecin = medecinRepository.findById(medecinId)
                        .orElseThrow(() -> new RuntimeException("Médecin introuvable: " + medecinId));

                StructureSanitaire structureSanitaire = structureSanitaireRepository.findById(structureId)
                        .orElseThrow(() -> new RuntimeException("Structure introuvable: " + structureId));

                agenda = new AgendaMedecin();
                agenda.setId(generateAgendaId());
                agenda.setEffectiveFrom(start);
                agenda.setJour(dto.getJour());
                agenda.setAutorise(dto.isAutorise());
                agenda.setMedecin(medecin);
                agenda.setStructureSanitaire(structureSanitaire);
                agenda.setPlages(new ArrayList<>());
            }

            agenda.setAutorise(dto.isAutorise());

            List<PlageHoraire> existingPlages = agenda.getPlages() != null
                    ? agenda.getPlages()
                    : new ArrayList<>();

            Map<String, PlageHoraire> existingById = existingPlages.stream()
                    .filter(plage -> plage.getId() != null)
                    .collect(Collectors.toMap(PlageHoraire::getId, plage -> plage, (a, b) -> a));

            List<PlageHoraire> finalPlages = new ArrayList<>();

            for (var pDto : dto.getPlages()) {
                PlageHoraire matched = null;

                if (pDto.getId() != null && !pDto.getId().isBlank()) {
                    matched = existingById.get(pDto.getId());
                }

                if (matched != null) {
                    boolean used = rendezVousRepository.existsByPlageHoraire_Id(matched.getId());

                    if (used) {
                        finalPlages.add(matched);
                    } else {
                        matched.setHeureDebut(pDto.getHeureDebut());
                        matched.setHeureFin(pDto.getHeureFin());
                        matched.setNombrePatients(pDto.getNombrePatients());
                        matched.setAutorise(pDto.isAutorise());
                        matched.setPeriode(determinerPeriode(pDto.getHeureDebut()));
                        matched.setArchive(false);

                        finalPlages.add(matched);
                    }
                } else {
                    PlageHoraire nouvellePlage = new PlageHoraire();
                    nouvellePlage.setId(UUID.randomUUID().toString());
                    nouvellePlage.setAgenda(agenda);
                    nouvellePlage.setHeureDebut(pDto.getHeureDebut());
                    nouvellePlage.setHeureFin(pDto.getHeureFin());
                    nouvellePlage.setNombrePatients(pDto.getNombrePatients());
                    nouvellePlage.setAutorise(pDto.isAutorise());
                    nouvellePlage.setPeriode(determinerPeriode(pDto.getHeureDebut()));
                    nouvellePlage.setArchive(false);

                    finalPlages.add(nouvellePlage);
                }
            }

            Set<String> keptIds = finalPlages.stream()
                    .map(PlageHoraire::getId)
                    .collect(Collectors.toSet());

            for (PlageHoraire old : existingPlages) {
                if (old.getId() == null) {
                    continue;
                }

                if (!keptIds.contains(old.getId())) {
                    old.setArchive(true);
                    old.setAutorise(false);
                    finalPlages.add(old);
                }
            }

            agenda.setPlages(finalPlages);

            repository.save(agenda);
        }

        return start;
    }

    /* =========================
       LECTURE DES AGENDAS RÉCENTS
       ========================= */

    public List<AgendaMedecinDto> getAgendasRecents(String medecinId, String structureId, LocalDate dateRef) {
        List<AgendaMedecin> all = repository
                .findByMedecin_IdAndStructureSanitaire_IdAndEffectiveFromLessThanEqual(
                        medecinId,
                        structureId,
                        dateRef
                );

        Map<JourSemaine, AgendaMedecin> latestByDay = new EnumMap<>(JourSemaine.class);

        for (AgendaMedecin agenda : all) {
            JourSemaine day = agenda.getJour();
            AgendaMedecin existing = latestByDay.get(day);

            if (existing == null || agenda.getEffectiveFrom().isAfter(existing.getEffectiveFrom())) {
                latestByDay.put(day, agenda);
            }
        }

        return latestByDay.values()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AgendaMedecinDto> getAgendasRecentsByMedecin(String medecinId, LocalDate dateRef) {
        LocalDate ref = dateRef != null ? dateRef : LocalDate.now();

        List<AgendaMedecin> all = repository
                .findByMedecin_IdAndEffectiveFromIsNotNullAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                        medecinId,
                        ref
                );

        Map<JourSemaine, AgendaMedecin> latestByDay = new EnumMap<>(JourSemaine.class);

        for (AgendaMedecin agenda : all) {
            latestByDay.putIfAbsent(agenda.getJour(), agenda);
        }

        return latestByDay.values()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AgendaMedecinDto> getAgendasRecentsByStructure(String structureId, LocalDate dateRef) {
        verifierAccesStructure(structureId);

        LocalDate ref = dateRef != null ? dateRef : LocalDate.now();

        List<AgendaMedecin> all = repository
                .findByStructureSanitaire_IdAndEffectiveFromIsNotNullAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                        structureId,
                        ref
                );

        Map<String, Map<JourSemaine, AgendaMedecin>> latest = new HashMap<>();

        for (AgendaMedecin agenda : all) {
            String medecinId = agenda.getMedecin().getId();

            latest.putIfAbsent(medecinId, new EnumMap<>(JourSemaine.class));

            Map<JourSemaine, AgendaMedecin> byDay = latest.get(medecinId);
            byDay.putIfAbsent(agenda.getJour(), agenda);
        }

        return latest.values()
                .stream()
                .flatMap(map -> map.values().stream())
                .map(mapper::toDto)
                .toList();
    }

    /* =========================
       LIAISON ADMIN / STRUCTURE
       ========================= */

    @Transactional
    public List<AgendaMedecinDto> saveWeekForLinking(AgendaSemaineRequest request) {
        return saveWeekInternal(request);
    }

    private List<AgendaMedecinDto> saveWeekInternal(AgendaSemaineRequest request) {
        return request.getAgendas().stream().map(dto -> {
            dto.setId(generateAgendaId());
            dto.setMedecinId(request.getMedecinId());
            dto.setStructureSanitaireId(request.getStructureSanitaireId());

            AgendaMedecin agenda = mapper.toEntity(dto);

            if (agenda.getEffectiveFrom() == null) {
                agenda.setEffectiveFrom(lundi(LocalDate.now()));
            }

            if (agenda.getPlages() != null) {
                agenda.getPlages().forEach(plage -> {
                    plage.setAgenda(agenda);

                    if (plage.getPeriode() == null && plage.getHeureDebut() != null) {
                        plage.setPeriode(determinerPeriode(plage.getHeureDebut()));
                    }
                });
            }

            return mapper.toDto(repository.save(agenda));
        }).toList();
    }

    /* =========================
       UTILITAIRES PRIVÉS
       ========================= */

    private PeriodeJournee determinerPeriode(LocalTime heureDebut) {
        return heureDebut.isBefore(LocalTime.NOON)
                ? PeriodeJournee.MATIN
                : PeriodeJournee.SOIR;
    }

    private void checkIfUserIsMedecin() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Medecin medecin = medecinRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Accès refusé"));

        if (!"MEDECIN".equals(medecin.getRole().name())) {
            throw new AccesRefuseException("Seul un médecin peut gérer son agenda");
        }
    }

    private void verifierJourneeModifiable(AgendaMedecin agenda) {
        journeeActiviteRepository.findByAgenda_Id(agenda.getId()).ifPresent(journee -> {
            if (journee.isAutorise()) {
                throw new AgendaNonModifiableException(
                        "Impossible de modifier : la journée est ouverte"
                );
            }

            int rdvActifs = rendezVousRepository.countByJourneeActivite_IdAndActifTrue(journee.getId());

            if (rdvActifs > 0) {
                throw new RuntimeException(
                        "Impossible de modifier : des rendez-vous sont encore actifs"
                );
            }
        });
    }

    private void annulerRdvsSemaine(String medecinId, String structureId, LocalDate start, LocalDate end) {
        List<JourneeActivite> journees = journeeActiviteRepository
                .findByMedecin_IdAndStructureSanitaire_IdAndDateBetween(
                        medecinId,
                        structureId,
                        start,
                        end
                );

        for (JourneeActivite journee : journees) {
            List<RendezVous> rdvs = rendezVousRepository.findByJourneeActivite_Id(journee.getId());

            rdvs.forEach(rdv -> {
                rdv.setActif(false);
                rdv.setArchive(true);
            });

            rendezVousRepository.saveAll(rdvs);

            journee.setAutorise(false);
            journee.setStatut(StatutJournee.FERMEE);

            journeeActiviteRepository.save(journee);
        }
    }

    private LocalDate lundi(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private String generateAgendaId() {
        return "AGENDA-" + UUID.randomUUID();
    }

    private void verifierAccesStructure(String structureId) {
        if (structureId == null || structureId.isBlank()) {
            throw new IllegalArgumentException("structureId est obligatoire pour vérifier l'abonnement.");
        }

        abonnementStructureService.verifierAccesAbonnement(structureId);
    }
}