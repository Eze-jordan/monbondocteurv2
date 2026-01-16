package com.esiitech.monbondocteurv2.mapper;

import com.esiitech.monbondocteurv2.dto.AgendaMedecinDto;
import com.esiitech.monbondocteurv2.dto.PlageHoraireDto;
import com.esiitech.monbondocteurv2.model.*;
import com.esiitech.monbondocteurv2.repository.MedecinRepository;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AgendaMedecinMapper {

    @Autowired
    private MedecinRepository medecinRepository;

    @Autowired
    private StructureSanitaireRepository structureSanitaireRepository;

    /* =========================
       ENTITY → DTO
       ========================= */
    public AgendaMedecinDto toDto(AgendaMedecin entity) {
        AgendaMedecinDto dto = new AgendaMedecinDto();

        dto.setId(entity.getId());
        dto.setJour(entity.getJour());
        dto.setAutorise(entity.isAutorise());

        dto.setMedecinId(entity.getMedecin().getId());
        dto.setStructureSanitaireId(entity.getStructureSanitaire().getId());

        // ✅ ICI : filtrer AVANT le mapping vers DTO
        dto.setPlages(
                entity.getPlages()
                        .stream()
                        .filter(p -> !p.isArchive())   // 👈 uniquement les plages actives
                        .map(this::toPlageDto)
                        .toList()
        );

        return dto;
    }


    private PlageHoraireDto toPlageDto(PlageHoraire plage) {
        PlageHoraireDto dto = new PlageHoraireDto();

        dto.setId(plage.getId());
        dto.setPeriode(plage.getPeriode());
        dto.setHeureDebut(plage.getHeureDebut());
        dto.setHeureFin(plage.getHeureFin());
        dto.setNombrePatients(plage.getNombrePatients());
        dto.setAutorise(plage.isAutorise());

        return dto;

    }
    /* =========================
       DTO → ENTITY
       ========================= */
    public AgendaMedecin toEntity(AgendaMedecinDto dto) {
        AgendaMedecin agenda = new AgendaMedecin();

        agenda.setId(dto.getId());
        agenda.setJour(dto.getJour());
        agenda.setAutorise(dto.isAutorise());

        // Médecin
        Medecin medecin = medecinRepository.findById(dto.getMedecinId())
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));
        agenda.setMedecin(medecin);

        // Structure sanitaire
        StructureSanitaire structure = structureSanitaireRepository
                .findById(dto.getStructureSanitaireId())
                .orElseThrow(() -> new RuntimeException("Structure sanitaire introuvable"));
        agenda.setStructureSanitaire(structure);

        // Plages horaires
        agenda.setPlages(
                dto.getPlages()
                        .stream()
                        .map(p -> toPlageEntity(p, agenda))
                        .toList()
        );

        return agenda;
    }


    private PlageHoraire toPlageEntity(PlageHoraireDto dto, AgendaMedecin agenda) {
        PlageHoraire plage = new PlageHoraire();

        plage.setId(dto.getId());
        plage.setAgenda(agenda);
        plage.setPeriode(dto.getPeriode());
        plage.setHeureDebut(dto.getHeureDebut());
        plage.setHeureFin(dto.getHeureFin());
        plage.setNombrePatients(dto.getNombrePatients());
        plage.setAutorise(dto.isAutorise());

        return plage;
    }
}
