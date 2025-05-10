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

    @Autowired private MedecinRepository medecinRepository;
    @Autowired private StructureSanitaireRepository structureSanitaireRepository;

    public AgendaMedecinDto toDto(AgendaMedecin entity) {
        AgendaMedecinDto dto = new AgendaMedecinDto();
        dto.setId(entity.getId());
        dto.setMedecinId(entity.getMedecin().getId());
        dto.setStructureSanitaireId(entity.getStructureSanitaire().getId());
        dto.setActif(entity.isActif());
        return dto;
    }

    public AgendaMedecin toEntity(AgendaMedecinDto dto) {
        AgendaMedecin entity = new AgendaMedecin();
        entity.setId(dto.getId());
        entity.setActif(dto.isActif());
        Medecin medecin = medecinRepository.findById(dto.getMedecinId()).orElseThrow();
        StructureSanitaire structure = structureSanitaireRepository.findById(dto.getStructureSanitaireId()).orElseThrow();
        entity.setMedecin(medecin);
        entity.setStructureSanitaire(structure);
        return entity;
    }
}