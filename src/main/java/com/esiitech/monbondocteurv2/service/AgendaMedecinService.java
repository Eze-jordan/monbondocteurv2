package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.AgendaMedecinDto;
import com.esiitech.monbondocteurv2.dto.AgendaSemainePlanifieeRequest;
import com.esiitech.monbondocteurv2.dto.AgendaSemaineRequest;
import com.esiitech.monbondocteurv2.dto.AgendaWeekStatusRequest;
import com.esiitech.monbondocteurv2.exception.AccesRefuseException;
import com.esiitech.monbondocteurv2.exception.AgendaNonModifiableException;
import com.esiitech.monbondocteurv2.exception.SemaineNonModifiableException;
import com.esiitech.monbondocteurv2.mapper.AgendaMedecinMapper;
import com.esiitech.monbondocteurv2.model.*;
import com.esiitech.monbondocteurv2.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AgendaMedecinService {

    @Autowired private AgendaMedecinRepository repository;
    @Autowired private AgendaMedecinMapper mapper;
    @Autowired private JourneeActiviteRepository journeeActiviteRepository;
    @Autowired private RendezVousRepository rendezVousRepository;
    @Autowired private MedecinRepository medecinRepository;
    @Autowired private StructureSanitaireRepository structureSanitaireRepository;

    /* =========================
       CRÉATION / MODIFICATION
       ========================= */
    @Transactional
    public AgendaMedecinDto save(AgendaMedecinDto dto) {


        if (dto.getId() == null) {
            dto.setId(generateAgendaId());
        }

        // Convertir DTO en entité
        AgendaMedecin agenda = mapper.toEntity(dto);
        if (agenda.getEffectiveFrom() == null) {
            agenda.setEffectiveFrom(lundi(LocalDate.now()));
        }


        // Remplir la période pour chaque plage horaire
        if (agenda.getPlages() != null) {
            agenda.getPlages().forEach(plage -> {
                plage.setAgenda(agenda); // ✅ lien owning side
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
            if (agenda.getEffectiveFrom() == null) {
                agenda.setEffectiveFrom(lundi(LocalDate.now()));
            }

            // Déterminer MATIN / SOIR automatiquement
            if (agenda.getPlages() != null) {
                agenda.getPlages().forEach(plage -> {
                    plage.setAgenda(agenda); // ✅ lien owning side
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
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Medecin medecin = medecinRepository.findByEmail(email)
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
    public List<AgendaMedecinDto> updateWeekCurrent(AgendaSemaineRequest request) {

        checkIfUserIsMedecin();

        String medecinId = request.getMedecinId();
        String structureId = request.getStructureSanitaireId();

        LocalDate start = lundi(LocalDate.now());
        LocalDate end = start.plusDays(6);

        // 0) Si tu veux bloquer TOUTE la semaine dès qu'il y a 1 RDV quelque part :
        int rdvSemaine = rendezVousRepository
                .countByMedecin_IdAndStructureSanitaire_IdAndDateBetweenAndActifTrueAndArchiveFalse(
                        medecinId, structureId, start, end
                );
        if (rdvSemaine > 0) {
            throw new SemaineNonModifiableException(
                    "Modification refusée : des rendez-vous existent sur la semaine en cours (" + start + "). " +
                            "Veuillez fermer les journées d’activité concernées avant de modifier les plages."
            );
        }

        // 1) Sinon on modifie jour par jour
        return request.getAgendas().stream().map(dto -> {

            // agenda "version" semaine courante (si tu versionnes, ajoute effectiveFrom=start ici)
            AgendaMedecin agenda = repository
                    .findByMedecin_IdAndStructureSanitaire_IdAndJourAndEffectiveFrom(
                            medecinId, structureId, dto.getJour(), start
                    )
                    .orElseThrow(() -> new RuntimeException("Agenda introuvable pour " + dto.getJour() + " (semaine " + start + ")"));

            // 2) Double sécurité : si tu as une journée d’activité ouverte, tu bloques
            journeeActiviteRepository
                    .findByAgenda_Id(agenda.getId())
                    .ifPresent(journee -> {
                        if (journee.isAutorise()) {
                            throw new AgendaNonModifiableException(
                                    "Impossible de modifier : la journée d’activité est ouverte pour " + dto.getJour() +
                                            ". Fermez la journée d’activité puis réessayez."
                            );
                        }
                    });

            // 3) Update champs simples
            agenda.setAutorise(dto.isAutorise());

            // 4) Update plages (UPDATE uniquement, comme ton updateDay)
            var existingById = agenda.getPlages().stream()
                    .collect(java.util.stream.Collectors.toMap(PlageHoraire::getId, p -> p));

            for (var pDto : dto.getPlages()) {

                if (pDto.getId() == null || pDto.getId().isBlank()) {
                    throw new RuntimeException("Modification refusée : plage sans id (risque de création).");
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


   /* @Transactional
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

*/
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
/*
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

 */

    @Transactional
    public LocalDate planifierUpdateWeek(AgendaSemainePlanifieeRequest request) {

        checkIfUserIsMedecin();

        String medecinId = request.getMedecinId();
        String structureId = request.getStructureSanitaireId();

        AgendaUpdatePolicy policy =
                (request.getPolicy() != null) ? request.getPolicy() : AgendaUpdatePolicy.SHIFT_TO_NEXT_FREE_WEEK;

        LocalDate start = lundi(
                (request.getWeekStart() != null) ? request.getWeekStart() : LocalDate.now()
        ).plusWeeks(1);

        LocalDate end = start.plusDays(6);

        // 1) Trouver une semaine cible selon policy
        while (true) {
            int rdv = rendezVousRepository
                    .countByMedecin_IdAndStructureSanitaire_IdAndDateBetweenAndActifTrueAndArchiveFalse(
                            medecinId,
                            structureId,
                            start,
                            end
                    );

            if (rdv == 0) break;

            if (policy == AgendaUpdatePolicy.REFUSE_IF_CONFLICT) {
                throw new RuntimeException("Impossible : il existe des RDV sur la semaine " + start);
            }

            if (policy == AgendaUpdatePolicy.CANCEL_RDV_AND_APPLY) {
                annulerRdvsSemaine(medecinId, structureId, start, end);
                break;
            }

            // SHIFT
            start = start.plusWeeks(1);
            end = start.plusDays(6);
        }

        // 2) Pour chaque jour, UPSERT de la version (effectiveFrom = start)
        for (AgendaMedecinDto dto : request.getAgendas()) {

            // on cherche si une version existe déjà pour ce (jour, effectiveFrom=start)
            var opt = repository.findByMedecin_IdAndStructureSanitaire_IdAndJourAndEffectiveFrom(
                    medecinId, structureId, dto.getJour(), start
            );

            AgendaMedecin agenda;
            if (opt.isPresent()) {
                agenda = opt.get();
            } else {
                // charger les entités (pas de méthodes getById custom)
                Medecin med = medecinRepository.findById(medecinId)
                        .orElseThrow(() -> new RuntimeException("Médecin introuvable: " + medecinId));

                StructureSanitaire ss = structureSanitaireRepository.findById(structureId)
                        .orElseThrow(() -> new RuntimeException("Structure introuvable: " + structureId));

                agenda = new AgendaMedecin();
                agenda.setId(generateAgendaId());
                agenda.setEffectiveFrom(start);
                agenda.setJour(dto.getJour());
                agenda.setAutorise(dto.isAutorise());
                agenda.setMedecin(med);
                agenda.setStructureSanitaire(ss);

                // IMPORTANT: initialiser la liste si besoin
                agenda.setPlages(new java.util.ArrayList<>());
            }


            // mise à jour champs simples
            agenda.setAutorise(dto.isAutorise());

            // 3) MAJ plages SANS DELETE et SANS casser RDV
            // règle: si une plage existante est utilisée par un RDV => on ne la modifie pas, on l’archive si elle n’existe plus dans la nouvelle config
            // et on ajoute de nouvelles plages (nouveaux IDs)

            // index des plages existantes
            List<PlageHoraire> existingPlages = (agenda.getPlages() != null) ? agenda.getPlages() : new java.util.ArrayList<>();
            java.util.Map<String, PlageHoraire> existingById = existingPlages.stream()
                    .filter(p -> p.getId() != null)
                    .collect(java.util.stream.Collectors.toMap(PlageHoraire::getId, p -> p, (a,b) -> a));

            // on va reconstruire une liste "cible"
            java.util.List<PlageHoraire> finalPlages = new java.util.ArrayList<>();

            // a) traiter plages demandées
            for (var pDto : dto.getPlages()) {

                // si le front envoie un id, on tente de matcher
                PlageHoraire matched = null;
                if (pDto.getId() != null && !pDto.getId().isBlank()) {
                    matched = existingById.get(pDto.getId());
                }

                if (matched != null) {
                    // plage existante : on ne modifie QUE si pas utilisée par un RDV
                    boolean used = rendezVousRepository.existsByPlageHoraire_Id(matched.getId());

                    if (used) {
                        // immuable : on la garde telle quelle (optionnel: tu peux juste la laisser)
                        finalPlages.add(matched);
                    } else {
                        // modifiable : on update
                        matched.setHeureDebut(pDto.getHeureDebut());
                        matched.setHeureFin(pDto.getHeureFin());
                        matched.setNombrePatients(pDto.getNombrePatients());
                        matched.setAutorise(pDto.isAutorise());
                        matched.setPeriode(determinerPeriode(pDto.getHeureDebut()));
                        matched.setArchive(false);
                        finalPlages.add(matched);
                    }

                } else {
                    // nouvelle plage : toujours nouveau ID
                    PlageHoraire n = new PlageHoraire();
                    n.setId(java.util.UUID.randomUUID().toString());
                    n.setAgenda(agenda); // important
                    n.setHeureDebut(pDto.getHeureDebut());
                    n.setHeureFin(pDto.getHeureFin());
                    n.setNombrePatients(pDto.getNombrePatients());
                    n.setAutorise(pDto.isAutorise());
                    n.setPeriode(determinerPeriode(pDto.getHeureDebut()));
                    n.setArchive(false);
                    finalPlages.add(n);
                }
            }

            // b) archiver les anciennes plages non reprises
            java.util.Set<String> keptIds = finalPlages.stream()
                    .map(PlageHoraire::getId)
                    .collect(java.util.stream.Collectors.toSet());

            for (PlageHoraire old : existingPlages) {
                if (old.getId() == null) continue;
                if (!keptIds.contains(old.getId())) {
                    boolean used = rendezVousRepository.existsByPlageHoraire_Id(old.getId());
                    if (used) {
                        old.setArchive(true);
                        old.setAutorise(false);
                        finalPlages.add(old); // on la garde archivée pour historique
                    } else {
                        // PAS DE DELETE (Stratégie A) => on archive aussi
                        old.setArchive(true);
                        old.setAutorise(false);
                        finalPlages.add(old);
                    }
                }
            }

            agenda.setPlages(finalPlages);

            repository.save(agenda);
        }

        return start;
    }

    private void annulerRdvsSemaine(String medecinId, String structureId, LocalDate start, LocalDate end) {

        List<JourneeActivite> journees = journeeActiviteRepository
                .findByMedecin_IdAndStructureSanitaire_IdAndDateBetween(medecinId, structureId, start, end);

        for (JourneeActivite j : journees) {
            List<RendezVous> rdvs = rendezVousRepository.findByJourneeActivite_Id(j.getId());
            rdvs.forEach(r -> { r.setActif(false); r.setArchive(true); });
            rendezVousRepository.saveAll(rdvs);

            j.setAutorise(false);
            j.setStatut(StatutJournee.FERMEE);
            journeeActiviteRepository.save(j); // selon ton repo
        }
    }
    private LocalDate lundi(LocalDate d) {
        return d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }


    public List<AgendaMedecinDto> getAgendasRecents(String medecinId, String structureId, LocalDate dateRef) {

        List<AgendaMedecin> all = repository
                .findByMedecin_IdAndStructureSanitaire_IdAndEffectiveFromLessThanEqual(medecinId, structureId, dateRef);

        // garder le max effectiveFrom par jour
        java.util.Map<JourSemaine, AgendaMedecin> latestByDay = new java.util.EnumMap<>(JourSemaine.class);

        for (AgendaMedecin a : all) {
            JourSemaine day = a.getJour();
            AgendaMedecin existing = latestByDay.get(day);

            if (existing == null || a.getEffectiveFrom().isAfter(existing.getEffectiveFrom())) {
                latestByDay.put(day, a);
            }
        }

        return latestByDay.values().stream()
                .map(mapper::toDto)
                .toList();
    }
    @Transactional(readOnly = true)
    public List<AgendaMedecinDto> getAgendasRecentsByMedecin(String medecinId, LocalDate dateRef) {

        LocalDate ref = (dateRef != null) ? dateRef : LocalDate.now();

        List<AgendaMedecin> all = repository
                .findByMedecin_IdAndEffectiveFromIsNotNullAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(medecinId, ref);

        java.util.Map<JourSemaine, AgendaMedecin> latestByDay = new java.util.EnumMap<>(JourSemaine.class);
        for (AgendaMedecin a : all) {
            latestByDay.putIfAbsent(a.getJour(), a);
        }

        return latestByDay.values().stream()
                .map(mapper::toDto)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<AgendaMedecinDto> getAgendasRecentsByStructure(String structureId, LocalDate dateRef) {

        LocalDate ref = (dateRef != null) ? dateRef : LocalDate.now();

        List<AgendaMedecin> all = repository
                .findByStructureSanitaire_IdAndEffectiveFromIsNotNullAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(structureId, ref);

        java.util.Map<String, java.util.Map<JourSemaine, AgendaMedecin>> latest = new java.util.HashMap<>();

        for (AgendaMedecin a : all) {
            String medId = a.getMedecin().getId();

            latest.putIfAbsent(medId, new java.util.EnumMap<>(JourSemaine.class));
            var byDay = latest.get(medId);

            // comme all est trié DESC, le premier rencontré est le plus récent
            byDay.putIfAbsent(a.getJour(), a);
        }

        return latest.values().stream()
                .flatMap(m -> m.values().stream())
                .map(mapper::toDto)
                .toList();
    }
    // ✅ méthode utilisée lors de la liaison (admin, etc.)
    @Transactional
    public List<AgendaMedecinDto> saveWeekForLinking(AgendaSemaineRequest request) {
        return saveWeekInternal(request);
    }

    // 🔥 logique centrale (une seule source de vérité)
    private List<AgendaMedecinDto> saveWeekInternal(AgendaSemaineRequest request) {


        return request.getAgendas().stream().map(dto -> {

            dto.setId(generateAgendaId());
            dto.setMedecinId(request.getMedecinId());
            dto.setStructureSanitaireId(request.getStructureSanitaireId());

            AgendaMedecin agenda = mapper.toEntity(dto);
            // ✅ si non fourni, on met la semaine courante
            if (agenda.getEffectiveFrom() == null) {
                agenda.setEffectiveFrom(lundi(LocalDate.now()));
            }
            if (agenda.getPlages() != null) {
                agenda.getPlages().forEach(plage -> {
                    plage.setAgenda(agenda); // ✅ owning side
                    if (plage.getPeriode() == null && plage.getHeureDebut() != null) {
                        plage.setPeriode(determinerPeriode(plage.getHeureDebut()));
                    }
                });
            }

            return mapper.toDto(repository.save(agenda));

        }).toList();
    }

}
