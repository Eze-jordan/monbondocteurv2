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
        dto.setNumeroTelephone(entity.getNumeroTelephone());
        dto.setPhotoPath(entity.getPhotoPath());
        dto.setVille(entity.getVille());
        dto.setRefType(entity.getRefType());
        dto.setGpsLatitude(entity.getGpsLatitude());
        dto.setGpsLongitude(entity.getGpsLongitude());
        dto.setRefSpecialites(entity.getRefSpecialites());
        dto.setUrldocument(entity.getUrldocument());
        dto.setDateDebutAbonnement(entity.getDateDebutAbonnement());
        dto.setDateFinAbonnement(entity.getDateFinAbonnement());
        dto.setAbonneExpire(entity.isAbonneExpire());
        dto.setActif(entity.isActif());
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
        entity.setPhotoPath(dto.getPhotoPath());
        entity.setVille(dto.getVille());
        entity.setRefType(dto.getRefType());
        entity.setGpsLatitude(dto.getGpsLatitude());
        entity.setGpsLongitude(dto.getGpsLongitude());
        entity.setRefSpecialites(dto.getRefSpecialites());
        entity.setUrldocument(dto.getUrldocument());
        entity.setDateDebutAbonnement(dto.getDateDebutAbonnement());
        entity.setDateFinAbonnement(dto.getDateFinAbonnement());
        entity.setAbonneExpire(dto.isAbonneExpire());
        entity.setActif(dto.isActif());
        return entity;
    }
}
