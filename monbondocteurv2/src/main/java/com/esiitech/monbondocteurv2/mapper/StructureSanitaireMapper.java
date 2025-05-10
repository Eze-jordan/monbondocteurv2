package com.esiitech.monbondocteurv2.mapper;

import com.esiitech.monbondocteurv2.dto.StructureSanitaireDto;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import org.springframework.stereotype.Component;

@Component
public class StructureSanitaireMapper {

    public StructureSanitaireDto toDto(StructureSanitaire entity) {
        StructureSanitaireDto dto = new StructureSanitaireDto();
        dto.setId(entity.getId());
        dto.setNomStructureSanitaire(entity.getNomStructureSanitaire());
        dto.setAdresse(entity.getAdresse());
        dto.setEmail(entity.getEmail());
        dto.setMotDePasse(entity.getMotDePasse());
        dto.setNumeroTelephone(entity.getNumeroTelephone());
        dto.setLogoPath(entity.getLogoPath());
        dto.setVille(entity.getVille());
        dto.setRefType(entity.getRefType());
        dto.setGpsLatitude(entity.getGpsLatitude());
        dto.setGpsLongitude(entity.getGpsLongitude());
        dto.setRefSpecialites(entity.getRefSpecialites());
        return dto;
    }

    public StructureSanitaire toEntity(StructureSanitaireDto dto) {
        StructureSanitaire entity = new StructureSanitaire();
        entity.setId(dto.getId());
        entity.setNomStructureSanitaire(dto.getNomStructureSanitaire());
        entity.setAdresse(dto.getAdresse());
        entity.setEmail(dto.getEmail());
        entity.setMotDePasse(dto.getMotDePasse());
        entity.setNumeroTelephone(dto.getNumeroTelephone());
        entity.setLogoPath(dto.getLogoPath());
        entity.setVille(dto.getVille());
        entity.setRefType(dto.getRefType());
        entity.setGpsLatitude(dto.getGpsLatitude());
        entity.setGpsLongitude(dto.getGpsLongitude());
        entity.setRefSpecialites(dto.getRefSpecialites());
        return entity;
    }
}
