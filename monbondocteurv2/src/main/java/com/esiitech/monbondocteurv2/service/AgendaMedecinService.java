package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.AgendaMedecinDto;
import com.esiitech.monbondocteurv2.mapper.AgendaMedecinMapper;
import com.esiitech.monbondocteurv2.model.AgendaMedecin;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.RefSpecialite;
import com.esiitech.monbondocteurv2.repository.AgendaMedecinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        checkIfUserIsMedecin();  // 🔒 Protection accès

        if (dto.getStructureSanitaireId() == null) {
            throw new RuntimeException("L'identifiant de la structure sanitaire est obligatoire.");
        }

        if (dto.getStructureSanitaireId() == null) {
            throw new RuntimeException("L'identifiant de la structure sanitaire est obligatoire.");
        }

        if (existeAgendaExact(dto.getMedecinId(), dto.getDate(), dto.getHeureDebut(), dto.getHeureFin())) {
            throw new RuntimeException("Un agenda existe déjà pour ce médecin à cette date et heure exacte.");
        }

        // Vérification avant enregistrement
        if (existeAgendaConcurrent(
                dto.getMedecinId(),
                dto.getDate(),
                dto.getHeureDebut(),
                dto.getHeureFin(),
                dto.getStructureSanitaireId()
        )) {
            throw new RuntimeException("Ce médecin a déjà une disponibilité sur ce créneau horaire dans une autre structure.");
        }



        AgendaMedecin entity = mapper.toEntity(dto);
        if (dto.getId() == null) {
            dto.setId(generateAgendaId());
        }
        return mapper.toDto(repository.save(entity));

    }

    private String generateAgendaId() {
        return "AgendaMedecin-" + java.util.UUID.randomUUID();
    }

    public boolean existeAgendaConcurrent(String medecinId, LocalDate date, LocalTime heureDebut, LocalTime heureFin, String structureId) {
        return repository.findByMedecinId(medecinId).stream()
                .filter(AgendaMedecin::isActif)
                .filter(agenda -> !agenda.getStructureSanitaire().getId().equals(structureId))
                .filter(agenda -> agenda.getDate().equals(date))
                .anyMatch(agenda ->
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


}
