package com.esiitech.monbondocteurv2.mapper;

import com.esiitech.monbondocteurv2.dto.AgendaMedecinDto;
import com.esiitech.monbondocteurv2.model.AgendaMedecin;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
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

    public AgendaMedecinDto toDto(AgendaMedecin entity) {
        AgendaMedecinDto dto = new AgendaMedecinDto();
        dto.setId(entity.getId());
        dto.setMedecinId(entity.getMedecin().getId());
        dto.setDate(entity.getDate());
        dto.setHeureDebut(entity.getHeureDebut());
        dto.setHeureFin(entity.getHeureFin());
        dto.setNombrePatient(entity.getNombrePatient());
        dto.setRdvPris(entity.getRdvPris());
        dto.setActif(entity.isActif());
        dto.setStructureSanitaireId(entity.getStructureSanitaire().getId()); // ✅ On retourne seulement l'ID
        return dto;
    }

    public AgendaMedecin toEntity(AgendaMedecinDto dto) {
        AgendaMedecin entity = new AgendaMedecin();
        entity.setId(dto.getId());
        entity.setActif(dto.isActif());

        Medecin medecin = medecinRepository.findById(dto.getMedecinId())
                .orElseThrow(() -> new RuntimeException("Médecin introuvable avec l'ID " + dto.getMedecinId()));
        entity.setMedecin(medecin);

        StructureSanitaire structure = structureSanitaireRepository.findById(dto.getStructureSanitaireId())
                .orElseThrow(() -> new RuntimeException("Structure introuvable avec l'ID " + dto.getStructureSanitaireId()));
        entity.setStructureSanitaire(structure);

        entity.setDate(dto.getDate());
        entity.setHeureDebut(dto.getHeureDebut());
        entity.setHeureFin(dto.getHeureFin());
        entity.setNombrePatient(dto.getNombrePatient());
        entity.setRdvPris(dto.getRdvPris());
        return entity;
    }
}