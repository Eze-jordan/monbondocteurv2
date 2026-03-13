package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.*;
import com.esiitech.monbondocteurv2.exception.RelationDejaExistanteException;
import com.esiitech.monbondocteurv2.exception.SpecialiteIncompatibleException;
import com.esiitech.monbondocteurv2.mapper.MedecinMapper;
import com.esiitech.monbondocteurv2.mapper.MedecinStructureSanitaireMapper;
import com.esiitech.monbondocteurv2.model.*;
import com.esiitech.monbondocteurv2.repository.AgendaMedecinRepository;
import com.esiitech.monbondocteurv2.repository.MedecinRepository;
import com.esiitech.monbondocteurv2.repository.MedecinStructureSanitaireRepository;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedecinStructureSanitaireService {

    @Autowired private MedecinStructureSanitaireRepository repository;
    @Autowired private MedecinStructureSanitaireMapper mapper;
    @Autowired private MedecinRepository medecinRepository;
    @Autowired private MedecinMapper medecinMapper;
    @Autowired private StructureSanitaireRepository structureSanitaireRepository;
    @Autowired private AgendaMedecinRepository agendaMedecinRepository;
    @Autowired
    private AgendaMedecinService agendaMedecinService;


    public List<MedecinStructureSanitaireDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public Optional<MedecinStructureSanitaireDto> findById(String id) {
        return repository.findById(id).map(mapper::toDto);
    }


    @Transactional
    public MedecinStructureSanitaireDto save(MedecinStructureSanitaireDto dto) {
        // validation basique
        if (dto == null) throw new IllegalArgumentException("dto cannot be null");
        if (dto.getStructureSanitaireId() == null || dto.getMedecinId() == null) {
            throw new IllegalArgumentException("structureSanitaireId and medecinId are required");
        }

        String structureId = dto.getStructureSanitaireId();
        String medecinId = dto.getMedecinId();

        // 0) Récupérer entités référencées (lève si introuvables)
        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new IllegalArgumentException("Médecin introuvable: " + medecinId));

        StructureSanitaire structure = structureSanitaireRepository.findById(structureId)
                .orElseThrow(() -> new IllegalArgumentException("Structure sanitaire introuvable: " + structureId));

        // ---------- validation métier : spécialité du médecin doit appartenir à la structure ----------
        String medecinSpec = medecin.getRefSpecialite(); // peut être null
        if (medecinSpec == null || medecinSpec.isBlank()) {
            throw new SpecialiteIncompatibleException("Le médecin n'a pas de spécialité définie.");
        }
        String medSpecNorm = medecinSpec.trim().toLowerCase();

        // récupère les spécialités de la structure (Set<String>)
        var structSpecs = structure.getRefSpecialites();
        if (structSpecs == null || structSpecs.isEmpty()) {
            throw new SpecialiteIncompatibleException("La structure n'a pas de spécialités définies. Impossible d'attacher un médecin.");
        }

        boolean match = structSpecs.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toLowerCase)
                .anyMatch(s -> s.equals(medSpecNorm));

        if (!match) {
            throw new SpecialiteIncompatibleException(
                    "Incompatibilité de spécialité : le médecin (" + medecinSpec + ") ne figure pas dans les spécialités de la structure."
            );
        }

        // 1) check appli -> renvoie 409 si déjà lié
        if (repository.existsByStructureSanitaireIdAndMedecinId(structureId, medecinId)) {
            throw new RelationDejaExistanteException(
                    "Le médecin " + medecinId + " est déjà rattaché à la structure " + structureId
            );
        }

        // 2) mapping et sauvegarde (avec try/catch pour attraper une violation DB si course)
        MedecinStructureSanitaire entity = mapper.toEntity(dto);
        if (entity.getId() == null) {
            entity.setId(generateMedecinStructureSanitaireId());
        }

        MedecinStructureSanitaire saved;
        try {
            saved = repository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new RelationDejaExistanteException("Relation déjà existante (conflit détecté lors de l'enregistrement)");
        }

        // ensuite, hors try/catch :
        agendaMedecinService.saveWeekForLinking(buildDefaultWeekRequest(medecinId, structureId));
        return mapper.toDto(saved);

    }


    private String generateMedecinStructureSanitaireId() {
        return " MedecinStructureSanitaire-" + java.util.UUID.randomUUID();
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }

    public List<Medecin> getMedecinsByStructureAndSpecialite(Long structureId, RefSpecialite specialite) {
        return repository.findMedecinsByStructureAndSpecialite(structureId, specialite);
    }

    public List<StructureSanitaire> getStructuresSanitairesActivesByMedecin(Medecin medecin) {
        return repository.findByMedecinAndActifTrue(medecin).stream()
                .map(MedecinStructureSanitaire::getStructureSanitaire)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public StructureSanitaire getUneStructureSanitaireActiveByMedecin(Medecin medecin) {
        return repository.findFirstByMedecinAndActifTrue(medecin).stream()
                .map(MedecinStructureSanitaire::getStructureSanitaire)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("Aucune structure active trouvée pour ce médecin")
                );
    }

    public Optional<Medecin> getMedecinByEmail(String email) {
        return medecinRepository.findByEmail(email);
    }

    /**
     * Retourne tous les médecins rattachés à la structure donnée (sans pagination).
     * @param structureId identifiant de la structure (ici String pour correspondre à repository.findByStructureSanitaireId)
     * @return liste de MedecinDto (peut être vide si aucun médecin)
     */
    public List<MedecinDto> getAllMedecinsByStructure(String structureId) {
        if (structureId == null || structureId.isBlank()) {
            return Collections.emptyList();
        }

        List<MedecinStructureSanitaire> relations = repository.findByStructureSanitaireId(structureId);
        return relations.stream()
                .map(MedecinStructureSanitaire::getMedecin)   // récupère l'entité Medecin depuis la relation
                .filter(Objects::nonNull)                     // sécurité si relation mal peuplée
                .map(medecinMapper::toDto)                   // conversion en DTO
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
        if (medecinId == null || medecinId.isBlank()) return Collections.emptyList();

        return repository.findByMedecinIdAndActifTrue(medecinId).stream()
                .map(rel -> rel.getStructureSanitaire())
                .filter(Objects::nonNull)
                .map(struct -> struct.getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    private void creerAgendasParDefautSiAbsents(Medecin medecin, StructureSanitaire structure) {

        LocalDate effectiveFrom = lundi(LocalDate.now()); // semaine courante

        for (JourSemaine jour : JourSemaine.values()) {

            boolean existe = agendaMedecinRepository
                    .existsByMedecinIdAndStructureSanitaireIdAndJourAndEffectiveFrom(
                            medecin.getId(),
                            structure.getId(),
                            jour,
                            effectiveFrom
                    );

            if (existe) continue;

            AgendaMedecin agenda = new AgendaMedecin();
            agenda.setId("AGENDA-" + java.util.UUID.randomUUID());
            agenda.setMedecin(medecin);
            agenda.setStructureSanitaire(structure);
            agenda.setJour(jour);
            agenda.setAutorise(false);
            agenda.setEffectiveFrom(effectiveFrom); // ✅ IMPORTANT

            List<PlageHoraire> plages = List.of(
                    buildPlage(agenda, LocalTime.of(8, 0),  LocalTime.of(12, 0), 10),
                    buildPlage(agenda, LocalTime.of(14, 0), LocalTime.of(18, 0), 10)
            );

            agenda.setPlages(plages);

            agendaMedecinRepository.save(agenda);
        }
    }



    private LocalDate lundi(LocalDate d) {
        return d.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
    }

    private PlageHoraire buildPlage(AgendaMedecin agenda, LocalTime debut, LocalTime fin, int nbPatients) {
        PlageHoraire p = new PlageHoraire();
        p.setAgenda(agenda);
        p.setHeureDebut(debut);
        p.setHeureFin(fin);
        p.setNombrePatients(nbPatients);
        p.setAutorise(true);
        p.setArchive(false);
        p.setPeriode(debut.isBefore(LocalTime.NOON) ? PeriodeJournee.MATIN : PeriodeJournee.SOIR);
        return p;
    }
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
        PlageHoraireDto p = new PlageHoraireDto();
        p.setHeureDebut(debut);
        p.setHeureFin(fin);
        p.setNombrePatients(nbPatients);
        p.setAutorise(true);
        // periode sera calculée dans saveWeekInternal (ou tu peux la mettre ici)
        return p;
    }
    public List<MedecinDto> getMedecinsByStructureAndSpecialite(String structureId, String specialite) {
        if (structureId == null || structureId.isBlank() || specialite == null || specialite.isBlank()) {
            return Collections.emptyList();
        }

        return repository.findMedecinsActifsByStructureAndSpecialite(structureId, specialite).stream()
                .map(medecinMapper::toDto)
                .collect(Collectors.toList());
    }

}