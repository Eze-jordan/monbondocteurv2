package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.AgendaMedecinDto;
import com.esiitech.monbondocteurv2.dto.AgendaSemaineRequest;
import com.esiitech.monbondocteurv2.dto.MedecinDto;
import com.esiitech.monbondocteurv2.dto.MedecinStructureSanitaireDto;
import com.esiitech.monbondocteurv2.dto.PlageHoraireDto;
import com.esiitech.monbondocteurv2.dto.SpecialiteDetailDto;
import com.esiitech.monbondocteurv2.dto.StructureSpecialitesStatsDto;
import com.esiitech.monbondocteurv2.enums.JourSemaine;
import com.esiitech.monbondocteurv2.enums.PeriodeJournee;
import com.esiitech.monbondocteurv2.enums.RefSpecialite;
import com.esiitech.monbondocteurv2.exception.RelationDejaExistanteException;
import com.esiitech.monbondocteurv2.exception.SpecialiteIncompatibleException;
import com.esiitech.monbondocteurv2.mapper.MedecinMapper;
import com.esiitech.monbondocteurv2.mapper.MedecinStructureSanitaireMapper;
import com.esiitech.monbondocteurv2.model.AgendaMedecin;
import com.esiitech.monbondocteurv2.model.JourneeActivite;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.MedecinStructureSanitaire;
import com.esiitech.monbondocteurv2.model.PlageHoraire;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.repository.AgendaMedecinRepository;
import com.esiitech.monbondocteurv2.repository.MedecinRepository;
import com.esiitech.monbondocteurv2.repository.MedecinStructureSanitaireRepository;
import com.esiitech.monbondocteurv2.repository.RendezVousRepository;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MedecinStructureSanitaireService {

    @Autowired
    private MedecinStructureSanitaireRepository repository;

    @Autowired
    private MedecinStructureSanitaireMapper mapper;

    @Autowired
    private MedecinRepository medecinRepository;

    @Autowired
    private MedecinMapper medecinMapper;

    @Autowired
    private StructureSanitaireRepository structureSanitaireRepository;

    @Autowired
    private AgendaMedecinRepository agendaMedecinRepository;

    @Autowired
    private AgendaMedecinService agendaMedecinService;

    @Autowired
    private AvisService avisService;

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private JourneeActiviteService journeeActiviteService;

    // ==================== CRUD RELATION MÉDECIN / STRUCTURE ====================

    public List<MedecinStructureSanitaireDto> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<MedecinStructureSanitaireDto> findById(String id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Transactional
    public MedecinStructureSanitaireDto save(MedecinStructureSanitaireDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("dto cannot be null");
        }

        if (dto.getStructureSanitaireId() == null || dto.getMedecinId() == null) {
            throw new IllegalArgumentException("structureSanitaireId and medecinId are required");
        }

        String structureId = dto.getStructureSanitaireId();
        String medecinId = dto.getMedecinId();

        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new IllegalArgumentException("Médecin introuvable: " + medecinId));

        StructureSanitaire structure = structureSanitaireRepository.findById(structureId)
                .orElseThrow(() -> new IllegalArgumentException("Structure sanitaire introuvable: " + structureId));

        String medecinSpec = medecin.getRefSpecialite();

        if (medecinSpec == null || medecinSpec.isBlank()) {
            throw new SpecialiteIncompatibleException("Le médecin n'a pas de spécialité définie.");
        }

        String medSpecNorm = medecinSpec.trim().toLowerCase();

        Set<String> structSpecs = structure.getRefSpecialites();

        if (structSpecs == null || structSpecs.isEmpty()) {
            throw new SpecialiteIncompatibleException(
                    "La structure n'a pas de spécialités définies. Impossible d'attacher un médecin."
            );
        }

        boolean match = structSpecs.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toLowerCase)
                .anyMatch(specialite -> specialite.equals(medSpecNorm));

        if (!match) {
            throw new SpecialiteIncompatibleException(
                    "Incompatibilité de spécialité : le médecin (" + medecinSpec + ") ne figure pas dans les spécialités de la structure."
            );
        }

        if (repository.existsByStructureSanitaireIdAndMedecinId(structureId, medecinId)) {
            throw new RelationDejaExistanteException(
                    "Le médecin " + medecinId + " est déjà rattaché à la structure " + structureId
            );
        }

        MedecinStructureSanitaire entity = mapper.toEntity(dto);

        if (entity.getId() == null) {
            entity.setId(generateMedecinStructureSanitaireId());
        }

        MedecinStructureSanitaire saved;

        try {
            saved = repository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new RelationDejaExistanteException(
                    "Relation déjà existante (conflit détecté lors de l'enregistrement)"
            );
        }

        agendaMedecinService.saveWeekForLinking(buildDefaultWeekRequest(medecinId, structureId));

        return mapper.toDto(saved);
    }

    private String generateMedecinStructureSanitaireId() {
        return "MedecinStructureSanitaire-" + java.util.UUID.randomUUID();
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }

    // ==================== RECHERCHE MÉDECIN / STRUCTURE ====================

    public List<Medecin> getMedecinsByStructureAndSpecialite(Long structureId, RefSpecialite specialite) {
        return repository.findMedecinsByStructureAndSpecialite(structureId, specialite);
    }

    public List<StructureSanitaire> getStructuresSanitairesActivesByMedecin(Medecin medecin) {
        return repository.findByMedecinAndActifTrue(medecin)
                .stream()
                .map(MedecinStructureSanitaire::getStructureSanitaire)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public StructureSanitaire getUneStructureSanitaireActiveByMedecin(Medecin medecin) {
        return repository.findFirstByMedecinAndActifTrue(medecin)
                .map(MedecinStructureSanitaire::getStructureSanitaire)
                .orElseThrow(() -> new RuntimeException("Aucune structure active trouvée pour ce médecin"));
    }

    public Optional<Medecin> getMedecinByEmail(String email) {
        return medecinRepository.findByEmail(email);
    }

    public List<MedecinDto> getAllMedecinsByStructure(String structureId) {
        if (structureId == null || structureId.isBlank()) {
            return Collections.emptyList();
        }

        List<MedecinStructureSanitaire> relations = repository.findByStructureSanitaireId(structureId);

        return relations.stream()
                .map(MedecinStructureSanitaire::getMedecin)
                .filter(Objects::nonNull)
                .map(medecinMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<MedecinDto> getActiveMedecinsByStructure(String structureId) {
        if (structureId == null || structureId.isBlank()) {
            return Collections.emptyList();
        }

        List<MedecinStructureSanitaire> relations = repository.findByStructureSanitaireIdAndActifTrue(structureId);

        return relations.stream()
                .map(MedecinStructureSanitaire::getMedecin)
                .filter(Objects::nonNull)
                .map(medecinMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<String> getStructureIdsForMedecin(String medecinId) {
        if (medecinId == null || medecinId.isBlank()) {
            return Collections.emptyList();
        }

        return repository.findByMedecinIdAndActifTrue(medecinId)
                .stream()
                .map(MedecinStructureSanitaire::getStructureSanitaire)
                .filter(Objects::nonNull)
                .map(StructureSanitaire::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<MedecinDto> getMedecinsByStructureAndSpecialite(String structureId, String specialite) {
        if (structureId == null || structureId.isBlank() || specialite == null || specialite.isBlank()) {
            return Collections.emptyList();
        }

        return repository.findMedecinsActifsByStructureAndSpecialite(
                        structureId.trim(),
                        specialite.trim()
                )
                .stream()
                .map(medecinMapper::toDto)
                .collect(Collectors.toList());
    }

    // ==================== STATISTIQUES PAR SPÉCIALITÉ ====================

    public SpecialiteDetailDto getSpecialiteDetail(String structureId, String specialite) {
        SpecialiteDetailDto dto = new SpecialiteDetailDto();
        dto.setSpecialite(specialite);

        StructureSanitaire structure = structureSanitaireRepository.findById(structureId)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        dto.setStructureNom(structure.getNomStructureSanitaire());

        boolean hasSpecialite = structure.getRefSpecialites()
                .stream()
                .anyMatch(s -> s.equalsIgnoreCase(specialite));

        if (!hasSpecialite) {
            throw new RuntimeException("Cette spécialité n'est pas disponible dans cette structure");
        }

        List<Medecin> medecins = repository.findMedecinsByStructureIdAndSpecialite(structureId, specialite);

        int nombreMedecins = medecins.size();
        dto.setNombreMedecins(nombreMedecins);

        if (medecins.isEmpty()) {
            dto.setMedecins(Collections.emptyList());
            dto.setNoteMoyenne(0.0);
            dto.setTotalAvis(0L);
            dto.setNombreTotalRdv(0L);
            dto.setTauxOccupation(0.0);
            dto.setCreneauxDisponiblesSemaine(0);
            dto.setMedecinsDisponiblesAujourdhui(0);
            return dto;
        }

        Double noteMoyenne = avisService.getNoteMoyenneSpecialite(structureId, specialite);
        dto.setNoteMoyenne(noteMoyenne != null ? Math.round(noteMoyenne * 10.0) / 10.0 : 0.0);

        Long totalAvis = avisService.countAvisByStructureAndSpecialite(structureId, specialite);
        dto.setTotalAvis(totalAvis != null ? totalAvis : 0L);

        Long totalRdv = countTotalRdvBySpecialite(structureId, specialite);
        dto.setNombreTotalRdv(totalRdv != null ? totalRdv : 0L);

        double tauxOccupation = calculateTauxOccupationForSpecialite(structureId, specialite);
        dto.setTauxOccupation(Math.round(tauxOccupation * 10.0) / 10.0);

        int creneauxDisponiblesSemaine = countCreneauxDisponiblesSemaine(structureId, specialite);
        dto.setCreneauxDisponiblesSemaine(creneauxDisponiblesSemaine);

        int medecinsDisponiblesAujourdhui = countMedecinsDisponiblesAujourdhui(structureId, specialite);
        dto.setMedecinsDisponiblesAujourdhui(medecinsDisponiblesAujourdhui);

        LocalDateTime prochainCreneau = findProchainCreneauDisponible(structureId, specialite);
        dto.setProchainCreneau(prochainCreneau);

        if (prochainCreneau != null) {
            long delaiJours = java.time.Duration.between(LocalDateTime.now(), prochainCreneau).toDays();
            dto.setDelaiMoyenAttenteJours(Math.max(delaiJours, 0L));
        } else {
            dto.setDelaiMoyenAttenteJours(0L);
        }

        List<SpecialiteDetailDto.MedecinSpecialiteDetailDto> medecinDtos = new ArrayList<>();

        for (Medecin medecin : medecins) {
            SpecialiteDetailDto.MedecinSpecialiteDetailDto medDto =
                    new SpecialiteDetailDto.MedecinSpecialiteDetailDto();

            medDto.setId(medecin.getId());
            medDto.setNom(medecin.getNomMedecin());
            medDto.setPrenom(medecin.getPrenomMedecin());
            medDto.setPhotoPath(medecin.getPhotoPath());
            medDto.setGrade(medecin.getRefGrade());

            Double noteMedecin = avisService.getNoteMoyenneByMedecinAndStructure(medecin.getId(), structureId);
            medDto.setNoteMoyenne(noteMedecin != null ? Math.round(noteMedecin * 10.0) / 10.0 : 0.0);

            Long nbAvis = avisService.countAvisByMedecinAndStructure(medecin.getId(), structureId);
            medDto.setNombreAvis(nbAvis != null ? nbAvis : 0L);

            Long nbRdv = countRdvByMedecinAndStructure(medecin.getId(), structureId);
            medDto.setNombreRdvEffectues(nbRdv != null ? nbRdv : 0L);

            boolean dispoAujourdhui = isMedecinDisponibleAujourdhui(medecin.getId(), structureId);
            medDto.setDisponibleAujourdhui(dispoAujourdhui);

            LocalDateTime prochaineDispo = findProchaineDisponibiliteMedecin(medecin.getId(), structureId);
            medDto.setProchaineDisponibilite(prochaineDispo);

            medDto.setCreneauxDisponibles(getCreneauxDisponibles(medecin.getId(), structureId));

            medecinDtos.add(medDto);
        }

        medecinDtos.sort((a, b) -> Double.compare(b.getNoteMoyenne(), a.getNoteMoyenne()));
        dto.setMedecins(medecinDtos);

        int rang = calculerRangSpecialite(structureId, specialite);
        dto.setRang(rang);
        dto.setEstMeilleure(rang == 1);

        return dto;
    }

    public StructureSpecialitesStatsDto getStructureSpecialitesStats(String structureId) {
        StructureSanitaire structure = structureSanitaireRepository.findById(structureId)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        Set<String> specialites = structure.getRefSpecialites();

        List<SpecialiteDetailDto> details = new ArrayList<>();
        Map<String, Long> rdvParSpecialite = new LinkedHashMap<>();
        Map<String, Integer> medecinsParSpecialite = new LinkedHashMap<>();

        for (String specialite : specialites) {
            SpecialiteDetailDto detail = getSpecialiteDetail(structureId, specialite);
            details.add(detail);
            rdvParSpecialite.put(specialite, detail.getNombreTotalRdv());
            medecinsParSpecialite.put(specialite, detail.getNombreMedecins());
        }

        String meilleureSpecialite = details.stream()
                .max(Comparator.comparingDouble(detail -> {
                    Double note = detail.getNoteMoyenne();
                    double scoreNote = (note != null ? note : 0.0) / 5.0 * 60.0;
                    double scoreOccupation = Math.min(detail.getTauxOccupation(), 100.0) * 0.4;
                    return scoreNote + scoreOccupation;
                }))
                .map(SpecialiteDetailDto::getSpecialite)
                .orElse(null);

        List<SpecialiteDetailDto> sortedDetails = new ArrayList<>(details);

        sortedDetails.sort((a, b) -> {
            double scoreA = calculerScoreCombine(a);
            double scoreB = calculerScoreCombine(b);
            return Double.compare(scoreB, scoreA);
        });

        for (int i = 0; i < sortedDetails.size(); i++) {
            SpecialiteDetailDto detail = sortedDetails.get(i);
            detail.setRang(i + 1);
            detail.setEstMeilleure(detail.getSpecialite().equals(meilleureSpecialite));
        }

        long nombreTotalRdv = details.stream()
                .mapToLong(SpecialiteDetailDto::getNombreTotalRdv)
                .sum();

        return StructureSpecialitesStatsDto.builder()
                .structureId(structureId)
                .nomStructure(structure.getNomStructureSanitaire())
                .nombreTotalSpecialites(specialites.size())
                .nombreTotalMedecins(countTotalMedecins(structureId))
                .nombreTotalRdv(nombreTotalRdv)
                .meilleureSpecialite(meilleureSpecialite)
                .specialites(sortedDetails)
                .rdvParSpecialite(rdvParSpecialite)
                .medecinsParSpecialite(medecinsParSpecialite)
                .build();
    }

    public List<SpecialiteDetailDto.MedecinSpecialiteDetailDto> getMedecinsDisponiblesAujourdhui(
            String structureId,
            String specialite
    ) {
        StructureSanitaire structure = structureSanitaireRepository.findById(structureId)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        boolean hasSpecialite = structure.getRefSpecialites()
                .stream()
                .anyMatch(s -> s.equalsIgnoreCase(specialite));

        if (!hasSpecialite) {
            throw new RuntimeException("Cette spécialité n'est pas disponible dans cette structure");
        }

        List<Medecin> medecins = repository.findMedecinsActifsByStructureAndSpecialite(structureId, specialite);

        if (medecins.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate today = LocalDate.now();
        JourSemaine jour = JourSemaine.valueOf(today.getDayOfWeek().name());

        List<SpecialiteDetailDto.MedecinSpecialiteDetailDto> result = new ArrayList<>();

        for (Medecin medecin : medecins) {
            Optional<AgendaMedecin> agendaOpt = agendaMedecinRepository
                    .findFirstByMedecinIdAndStructureSanitaireIdAndJourOrderByEffectiveFromDesc(
                            medecin.getId(),
                            structureId,
                            jour
                    );

            boolean disponibleAujourdhui = false;

            if (agendaOpt.isPresent()) {
                AgendaMedecin agenda = agendaOpt.get();

                if (agenda.isAutorise()) {
                    JourneeActivite journeeActivite = journeeActiviteService.getOrCreate(today, agenda);

                    if (journeeActivite.isAutorise()) {
                        for (PlageHoraire plage : agenda.getPlages()) {
                            if (!plage.isAutorise()) {
                                continue;
                            }

                            LocalTime heure = plage.getHeureDebut();
                            LocalTime fin = plage.getHeureFin();
                            int capacite = plage.getNombrePatients() != null ? plage.getNombrePatients() : 0;

                            while (heure.isBefore(fin)) {
                                int rdvPris = rendezVousRepository.countByJourneeActivite_IdAndPlageHoraire_IdAndHeureDebut(
                                        journeeActivite.getId(),
                                        plage.getId(),
                                        heure
                                );

                                if (rdvPris < capacite) {
                                    disponibleAujourdhui = true;
                                    break;
                                }

                                heure = heure.plusMinutes(30);
                            }

                            if (disponibleAujourdhui) {
                                break;
                            }
                        }
                    }
                }
            }

            if (disponibleAujourdhui) {
                SpecialiteDetailDto.MedecinSpecialiteDetailDto medDto =
                        new SpecialiteDetailDto.MedecinSpecialiteDetailDto();

                medDto.setId(medecin.getId());
                medDto.setNom(medecin.getNomMedecin());
                medDto.setPrenom(medecin.getPrenomMedecin());
                medDto.setPhotoPath(medecin.getPhotoPath());
                medDto.setGrade(medecin.getRefGrade());
                medDto.setDisponibleAujourdhui(true);

                Double noteMedecin = avisService.getNoteMoyenneByMedecinAndStructure(medecin.getId(), structureId);
                medDto.setNoteMoyenne(noteMedecin != null ? Math.round(noteMedecin * 10.0) / 10.0 : 0.0);

                Long nbRdv = rendezVousRepository.countByMedecinIdAndStructureSanitaireId(medecin.getId(), structureId);
                medDto.setNombreRdvEffectues(nbRdv != null ? nbRdv : 0L);

                result.add(medDto);
            }
        }

        result.sort((a, b) -> Double.compare(b.getNoteMoyenne(), a.getNoteMoyenne()));

        return result;
    }

    // ==================== MÉTHODES INTERNES STATISTIQUES ====================

    private Long countTotalRdvBySpecialite(String structureId, String specialite) {
        List<Medecin> medecins = repository.findMedecinsByStructureIdAndSpecialite(structureId, specialite);

        if (medecins.isEmpty()) {
            return 0L;
        }

        Long total = 0L;

        for (Medecin medecin : medecins) {
            total += rendezVousRepository.countByMedecinIdAndStructureSanitaireId(medecin.getId(), structureId);
        }

        return total;
    }

    private Long countRdvByMedecinAndStructure(String medecinId, String structureId) {
        return rendezVousRepository.countByMedecinIdAndStructureSanitaireId(medecinId, structureId);
    }

    private double calculateTauxOccupationForSpecialite(String structureId, String specialite) {
        LocalDate now = LocalDate.now();
        LocalDate debutMois = now.withDayOfMonth(1);
        LocalDate finMois = now.withDayOfMonth(now.lengthOfMonth());

        List<Medecin> medecins = repository.findMedecinsByStructureIdAndSpecialite(structureId, specialite);

        if (medecins.isEmpty()) {
            return 0.0;
        }

        long rdvCeMois = 0;

        for (Medecin medecin : medecins) {
            Long count = rendezVousRepository.countByMedecinIdAndStructureIdAndDateBetween(
                    medecin.getId(),
                    structureId,
                    debutMois,
                    finMois
            );

            rdvCeMois += count != null ? count : 0;
        }

        if (rdvCeMois == 0) {
            return 0.0;
        }

        int totalCreneaux = 0;

        for (Medecin medecin : medecins) {
            totalCreneaux += countCreneauxMensuelsMedecin(medecin.getId(), structureId, now);
        }

        if (totalCreneaux == 0) {
            return 0.0;
        }

        double taux = (double) rdvCeMois / totalCreneaux * 100;

        return Math.min(taux, 100.0);
    }

    private int countCreneauxMensuelsMedecin(String medecinId, String structureId, LocalDate now) {
        List<AgendaMedecin> agendas = agendaMedecinRepository
                .findByMedecinIdAndStructureSanitaireId(medecinId, structureId);

        if (agendas.isEmpty()) {
            return 0;
        }

        LocalDate debutMois = now.withDayOfMonth(1);
        LocalDate finMois = now.withDayOfMonth(now.lengthOfMonth());

        int totalCreneaux = 0;

        for (AgendaMedecin agenda : agendas) {
            if (!agenda.isAutorise()) {
                continue;
            }

            JourSemaine jour = agenda.getJour();
            int occurrences = 0;

            for (LocalDate date = debutMois; !date.isAfter(finMois); date = date.plusDays(1)) {
                if (date.getDayOfWeek().name().equals(jour.name())) {
                    occurrences++;
                }
            }

            int creneauxParJour = agenda.getPlages()
                    .stream()
                    .filter(PlageHoraire::isAutorise)
                    .mapToInt(plage -> plage.getNombrePatients() != null ? plage.getNombrePatients() : 0)
                    .sum();

            totalCreneaux += creneauxParJour * occurrences;
        }

        return totalCreneaux;
    }

    private int countCreneauxDisponiblesSemaine(String structureId, String specialite) {
        LocalDate now = LocalDate.now();
        LocalDate debutSemaine = now.with(java.time.DayOfWeek.MONDAY);
        LocalDate finSemaine = debutSemaine.plusDays(6);

        List<Medecin> medecins = repository.findMedecinsByStructureIdAndSpecialite(structureId, specialite);

        int totalCreneaux = 0;

        for (Medecin medecin : medecins) {
            for (LocalDate date = debutSemaine; !date.isAfter(finSemaine); date = date.plusDays(1)) {
                totalCreneaux += countCreneauxForDate(medecin.getId(), structureId, date);
            }
        }

        return totalCreneaux;
    }

    private int countCreneauxForDate(String medecinId, String structureId, LocalDate date) {
        JourSemaine jour = JourSemaine.valueOf(date.getDayOfWeek().name());

        List<AgendaMedecin> agendas = agendaMedecinRepository
                .findByMedecinIdAndStructureSanitaireIdAndJourAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                        medecinId,
                        structureId,
                        jour,
                        date
                );

        if (agendas.isEmpty()) {
            return 0;
        }

        AgendaMedecin agenda = agendas.get(0);

        if (!agenda.isAutorise()) {
            return 0;
        }

        JourneeActivite journeeActivite = journeeActiviteService.getOrCreate(date, agenda);

        if (!journeeActivite.isAutorise()) {
            return 0;
        }

        int creneauxDisponibles = 0;

        for (PlageHoraire plage : agenda.getPlages()) {
            if (!plage.isAutorise()) {
                continue;
            }

            LocalTime heure = plage.getHeureDebut();
            LocalTime fin = plage.getHeureFin();
            int capacite = plage.getNombrePatients() != null ? plage.getNombrePatients() : 0;

            while (heure.isBefore(fin)) {
                int rdvPris = rendezVousRepository.countByJourneeActivite_IdAndPlageHoraire_IdAndHeureDebut(
                        journeeActivite.getId(),
                        plage.getId(),
                        heure
                );

                if (rdvPris < capacite) {
                    creneauxDisponibles++;
                }

                heure = heure.plusMinutes(30);
            }
        }

        return creneauxDisponibles;
    }

    private int countMedecinsDisponiblesAujourdhui(String structureId, String specialite) {
        List<Medecin> medecins = repository.findMedecinsByStructureIdAndSpecialite(structureId, specialite);

        return (int) medecins.stream()
                .filter(medecin -> isMedecinDisponibleAujourdhui(medecin.getId(), structureId))
                .count();
    }

    public boolean isMedecinDisponibleAujourdhui(String medecinId, String structureId) {
        LocalDate today = LocalDate.now();
        JourSemaine jour = JourSemaine.valueOf(today.getDayOfWeek().name());

        List<AgendaMedecin> agendas = agendaMedecinRepository
                .findByMedecinIdAndStructureSanitaireIdAndJourAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                        medecinId,
                        structureId,
                        jour,
                        today
                );

        if (agendas.isEmpty()) {
            return false;
        }

        AgendaMedecin agenda = agendas.get(0);

        if (!agenda.isAutorise()) {
            return false;
        }

        JourneeActivite journeeActivite = journeeActiviteService.getOrCreate(today, agenda);

        if (!journeeActivite.isAutorise()) {
            return false;
        }

        for (PlageHoraire plage : agenda.getPlages()) {
            if (!plage.isAutorise()) {
                continue;
            }

            LocalTime heure = plage.getHeureDebut();
            LocalTime fin = plage.getHeureFin();
            int capacite = plage.getNombrePatients() != null ? plage.getNombrePatients() : 0;

            while (heure.isBefore(fin)) {
                int rdvPris = rendezVousRepository.countByJourneeActivite_IdAndPlageHoraire_IdAndHeureDebut(
                        journeeActivite.getId(),
                        plage.getId(),
                        heure
                );

                if (rdvPris < capacite) {
                    return true;
                }

                heure = heure.plusMinutes(30);
            }
        }

        return false;
    }

    private LocalDateTime findProchainCreneauDisponible(String structureId, String specialite) {
        List<Medecin> medecins = repository.findMedecinsByStructureIdAndSpecialite(structureId, specialite);

        LocalDateTime plusProche = null;

        for (Medecin medecin : medecins) {
            LocalDateTime dispo = findProchaineDisponibiliteMedecin(medecin.getId(), structureId);

            if (dispo != null && (plusProche == null || dispo.isBefore(plusProche))) {
                plusProche = dispo;
            }
        }

        return plusProche;
    }

    private LocalDateTime findProchaineDisponibiliteMedecin(String medecinId, String structureId) {
        LocalDate now = LocalDate.now();
        LocalDate maxDate = now.plusDays(30);

        for (LocalDate date = now; !date.isAfter(maxDate); date = date.plusDays(1)) {
            JourSemaine jour = JourSemaine.valueOf(date.getDayOfWeek().name());

            List<AgendaMedecin> agendas = agendaMedecinRepository
                    .findByMedecinIdAndStructureSanitaireIdAndJourAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                            medecinId,
                            structureId,
                            jour,
                            date
                    );

            if (agendas.isEmpty()) {
                continue;
            }

            AgendaMedecin agenda = agendas.get(0);

            if (!agenda.isAutorise()) {
                continue;
            }

            JourneeActivite journeeActivite = journeeActiviteService.getOrCreate(date, agenda);

            if (!journeeActivite.isAutorise()) {
                continue;
            }

            for (PlageHoraire plage : agenda.getPlages()) {
                if (!plage.isAutorise()) {
                    continue;
                }

                LocalTime heure = plage.getHeureDebut();
                LocalTime fin = plage.getHeureFin();
                int capacite = plage.getNombrePatients() != null ? plage.getNombrePatients() : 0;

                while (heure.isBefore(fin)) {
                    int rdvPris = rendezVousRepository.countByJourneeActivite_IdAndPlageHoraire_IdAndHeureDebut(
                            journeeActivite.getId(),
                            plage.getId(),
                            heure
                    );

                    if (rdvPris < capacite) {
                        return LocalDateTime.of(date, heure);
                    }

                    heure = heure.plusMinutes(30);
                }
            }
        }

        return null;
    }

    private List<SpecialiteDetailDto.CreneauDto> getCreneauxDisponibles(String medecinId, String structureId) {
        List<SpecialiteDetailDto.CreneauDto> creneaux = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate maxDate = now.plusDays(30);

        List<AgendaMedecin> agendas = agendaMedecinRepository
                .findByMedecinIdAndStructureSanitaireId(medecinId, structureId);

        for (LocalDate date = now; !date.isAfter(maxDate); date = date.plusDays(1)) {
            JourSemaine jour = JourSemaine.valueOf(date.getDayOfWeek().name());

            AgendaMedecin agenda = agendas.stream()
                    .filter(a -> a.getJour() == jour && a.isAutorise())
                    .findFirst()
                    .orElse(null);

            if (agenda == null) {
                continue;
            }

            JourneeActivite journeeActivite = journeeActiviteService.getOrCreate(date, agenda);

            if (!journeeActivite.isAutorise()) {
                continue;
            }

            for (PlageHoraire plage : agenda.getPlages()) {
                if (!plage.isAutorise()) {
                    continue;
                }

                LocalTime heure = plage.getHeureDebut();
                LocalTime fin = plage.getHeureFin();
                int capacite = plage.getNombrePatients() != null ? plage.getNombrePatients() : 0;

                while (heure.isBefore(fin)) {
                    int rdvPris = rendezVousRepository.countByJourneeActivite_IdAndPlageHoraire_IdAndHeureDebut(
                            journeeActivite.getId(),
                            plage.getId(),
                            heure
                    );

                    if (rdvPris < capacite) {
                        SpecialiteDetailDto.CreneauDto creneau = new SpecialiteDetailDto.CreneauDto();
                        creneau.setDate(date.toString());
                        creneau.setHeure(heure.toString());
                        creneaux.add(creneau);
                    }

                    heure = heure.plusMinutes(30);
                }
            }
        }

        creneaux.sort((a, b) -> {
            int dateCompare = a.getDate().compareTo(b.getDate());

            if (dateCompare != 0) {
                return dateCompare;
            }

            return a.getHeure().compareTo(b.getHeure());
        });

        return creneaux.stream()
                .limit(10)
                .collect(Collectors.toList());
    }

    private int calculerRangSpecialite(String structureId, String specialite) {
        StructureSanitaire structure = structureSanitaireRepository.findById(structureId)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        Set<String> specialites = structure.getRefSpecialites();

        List<Map.Entry<String, Double>> scores = new ArrayList<>();

        for (String spec : specialites) {
            Double note = avisService.getNoteMoyenneSpecialite(structureId, spec);
            double taux = calculateTauxOccupationForSpecialite(structureId, spec);

            double scoreNote = (note != null ? note : 0.0) / 5.0 * 60.0;
            double scoreOccupation = Math.min(taux, 100.0) * 0.4;
            double score = scoreNote + scoreOccupation;

            scores.add(new AbstractMap.SimpleEntry<>(spec, score));
        }

        scores.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        for (int i = 0; i < scores.size(); i++) {
            if (scores.get(i).getKey().equalsIgnoreCase(specialite)) {
                return i + 1;
            }
        }

        return specialites.size();
    }

    private double calculerScoreCombine(SpecialiteDetailDto detail) {
        Double note = detail.getNoteMoyenne();
        double scoreNote = (note != null ? note : 0.0) / 5.0 * 60.0;
        double scoreOccupation = Math.min(detail.getTauxOccupation(), 100.0) * 0.4;

        return scoreNote + scoreOccupation;
    }

    private int countTotalMedecins(String structureId) {
        List<MedecinStructureSanitaire> relations = repository.findByStructureSanitaireIdAndActifTrue(structureId);
        return relations.size();
    }

    // ==================== AGENDA PAR DÉFAUT ====================

    private AgendaSemaineRequest buildDefaultWeekRequest(String medecinId, String structureId) {
        AgendaSemaineRequest req = new AgendaSemaineRequest();
        req.setMedecinId(medecinId);
        req.setStructureSanitaireId(structureId);

        req.setAgendas(List.of(
                buildAgendaDto(JourSemaine.MONDAY),
                buildAgendaDto(JourSemaine.TUESDAY),
                buildAgendaDto(JourSemaine.WEDNESDAY),
                buildAgendaDto(JourSemaine.THURSDAY),
                buildAgendaDto(JourSemaine.FRIDAY),
                buildAgendaDto(JourSemaine.SATURDAY),
                buildAgendaDto(JourSemaine.SUNDAY)
        ));

        return req;
    }

    private AgendaMedecinDto buildAgendaDto(JourSemaine jour) {
        AgendaMedecinDto dto = new AgendaMedecinDto();
        dto.setJour(jour);
        dto.setAutorise(false);

        dto.setPlages(List.of(
                buildPlageDto(LocalTime.of(8, 0), LocalTime.of(12, 0), 10),
                buildPlageDto(LocalTime.of(14, 0), LocalTime.of(18, 0), 10)
        ));

        return dto;
    }

    private PlageHoraireDto buildPlageDto(LocalTime debut, LocalTime fin, int nbPatients) {
        PlageHoraireDto plage = new PlageHoraireDto();
        plage.setHeureDebut(debut);
        plage.setHeureFin(fin);
        plage.setNombrePatients(nbPatients);
        plage.setAutorise(true);
        plage.setPeriode(debut.isBefore(LocalTime.NOON) ? PeriodeJournee.MATIN : PeriodeJournee.SOIR);

        return plage;
    }
}