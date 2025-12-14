package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.AgendaMedecinDto;
import com.esiitech.monbondocteurv2.exception.DisponibiliteConflitException;
import com.esiitech.monbondocteurv2.mapper.AgendaMedecinMapper;
import com.esiitech.monbondocteurv2.model.AgendaMedecin;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.RefSpecialite;
import com.esiitech.monbondocteurv2.repository.AgendaMedecinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AgendaMedecinService {

    @Autowired
    private AgendaMedecinRepository repository;

    @Autowired
    private AgendaMedecinMapper mapper;

    @Autowired
    private MedecinStructureSanitaireService medecinStructureSanitaireService;

    public List<AgendaMedecinDto> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<AgendaMedecinDto> findById(String id) {
        return repository.findById(id)
                .map(mapper::toDto);
    }

    public AgendaMedecinDto save(AgendaMedecinDto dto) {

        checkIfUserIsMedecin();

        if (dto.getStructureSanitaireId() == null) {
            throw new RuntimeException("L'identifiant de la structure sanitaire est obligatoire.");
        }

        if (existeAgendaExact(dto.getMedecinId(), dto.getDate(), dto.getHeureDebut(), dto.getHeureFin())) {
            throw new DisponibiliteConflitException("Un agenda existe déjà pour ce médecin à cette date et heure exacte.");
        }

        if (existeAgendaConcurrent(
                dto.getMedecinId(),
                dto.getDate(),
                dto.getHeureDebut(),
                dto.getHeureFin(),
                dto.getStructureSanitaireId())) {

            throw new DisponibiliteConflitException("Ce médecin a déjà une disponibilité sur ce créneau horaire dans une autre structure.");
        }

        // 👉 corriger ici : générer l’ID AVANT le mapping
        if (dto.getId() == null || dto.getId().isEmpty()) {
            dto.setId(generateAgendaId());
        }

        // mapper après l'assignation de l'ID
        AgendaMedecin entity = mapper.toEntity(dto);

        // sauvegarder normalement
        AgendaMedecin saved = repository.save(entity);

        return mapper.toDto(saved);
    }


    private String generateAgendaId() {
        return "AgendaMedecin-" + java.util.UUID.randomUUID();
    }

    public boolean existeAgendaConcurrent(String medecinId, LocalDate date, LocalTime heureDebut, LocalTime heureFin, String structureId) {
        // 1) récupère les IDs des structures auxquelles le médecin est activement rattaché
        List<String> linkedStructureIds = medecinStructureSanitaireService.getStructureIdsForMedecin(medecinId);

        if (linkedStructureIds == null || linkedStructureIds.isEmpty()) {
            // s'il n'est lié à aucune structure active -> pas de concurrence à vérifier
            return false;
        }

        // 2) récupère tous les agendas du médecin (optimisable via repo JPQL si nécessaire)
        return repository.findByMedecinId(medecinId).stream()
                .filter(AgendaMedecin::isActif)                                  // seulement actifs
                .filter(agenda -> agenda.getDate().equals(date))                 // même date
                .filter(agenda -> linkedStructureIds.contains(agenda.getStructureSanitaire().getId())) // seulement structures liées
                .filter(agenda -> !agenda.getStructureSanitaire().getId().equals(structureId)) // autre structure que celle en cours
                .anyMatch(agenda ->
                        // overlap check : startA < endB && endA > startB
                        agenda.getHeureDebut().isBefore(heureFin) &&
                                agenda.getHeureFin().isAfter(heureDebut)
                );
    }


    private void checkIfUserIsMedecin() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();

        Medecin medecin = medecinStructureSanitaireService.getMedecinByEmail(email)
                .orElseThrow(() -> new RuntimeException("Seul un utilisateur ayant le rôle MEDECIN peut créer un agenda"));

        if (!medecin.getRole().name().equals("MEDECIN")) {
            throw new RuntimeException("Seul un utilisateur ayant le rôle MEDECIN peut créer un agenda");
        }
    }


    public boolean existeAgendaExact(String medecinId, LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        return repository.findByMedecinId(medecinId).stream()
                .filter(AgendaMedecin::isActif)
                .filter(agenda -> agenda.getDate().equals(date))
                .anyMatch(agenda ->
                        agenda.getHeureDebut().equals(heureDebut) &&
                                agenda.getHeureFin().equals(heureFin)
                );
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }

    public List<AgendaMedecinDto> findByMedecinAndStructure(String medecinId, Long structureId) {
        return repository.findByMedecinAndStructure(medecinId, structureId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public List<AgendaMedecinDto> getAgendasByStructureAndSpecialite(Long structureId, RefSpecialite specialite) {
        List<Medecin> medecins = medecinStructureSanitaireService.getMedecinsByStructureAndSpecialite(structureId, specialite);
        return medecins.stream()
                .flatMap(medecin -> repository.findByMedecinId(medecin.getId()).stream())
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public boolean desactiverAgenda(String agendaId) {
        return repository.findById(agendaId).map(agenda -> {
            agenda.setActif(false);
            repository.save(agenda);
            return true;
        }).orElse(false);
    }

    @Transactional
    public AgendaMedecinDto updateAgendaStatus(String agendaId, boolean actif) {
        AgendaMedecin agenda = repository.findById(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("Agenda introuvable: " + agendaId));

        // Récupère l'email de l'utilisateur connecté
        String emailConnecte = SecurityContextHolder.getContext().getAuthentication().getName();

        // Récupère le médecin connecté (s'il existe)
        Optional<Medecin> medecinOpt = medecinStructureSanitaireService.getMedecinByEmail(emailConnecte);

        boolean estProprietaire = medecinOpt
                .map(m -> m.getId() != null && m.getId().equals(agenda.getMedecin().getId()))
                .orElse(false);

        // Autorisation : soit le médecin propriétaire, soit un admin (vérification simple de rôle)
        boolean estAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!estProprietaire && !estAdmin) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier le statut de cet agenda.");
        }

        // change le statut et sauvegarde
        agenda.setActif(actif);
        AgendaMedecin saved = repository.save(agenda);
        return mapper.toDto(saved);
    }

    public List<AgendaMedecinDto> getAllAgendasByMedecinId(String medecinId) {
        return repository.findByMedecinId(medecinId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

}
