package com.esiitech.monbondocteurv2.mapper;

import com.esiitech.monbondocteurv2.dto.MedecinStructureSanitaireDto;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.MedecinStructureSanitaire;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.repository.MedecinRepository;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MedecinStructureSanitaireMapper {

    @Autowired private MedecinRepository medecinRepository;
    @Autowired private StructureSanitaireRepository structureSanitaireRepository;

    public MedecinStructureSanitaireDto toDto(MedecinStructureSanitaire entity) {
        MedecinStructureSanitaireDto dto = new MedecinStructureSanitaireDto();
        dto.setId(entity.getId());
        dto.setMedecinId(entity.getMedecin().getId());
        dto.setStructureSanitaireId(entity.getStructureSanitaire().getId());
        dto.setActif(entity.isActif());
        return dto;
    }

    public MedecinStructureSanitaire toEntity(MedecinStructureSanitaireDto dto) {
        MedecinStructureSanitaire entity = new MedecinStructureSanitaire();
        entity.setId(dto.getId());
        entity.setActif(dto.isActif());
        Medecin medecin = medecinRepository.findById(dto.getMedecinId()).orElseThrow();
        StructureSanitaire structure = structureSanitaireRepository.findById(dto.getStructureSanitaireId()).orElseThrow();
        entity.setMedecin(medecin);
        entity.setStructureSanitaire(structure);
        return entity;
    }
}
