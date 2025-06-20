package com.esiitech.monbondocteurv2.mapper;

import com.esiitech.monbondocteurv2.dto.MedecinDto;
import com.esiitech.monbondocteurv2.model.Medecin;
import org.springframework.stereotype.Component;

@Component
public class MedecinMapper {
    /**
     * Convertir l'entité Medecin en DTO de réponse
     */
    public MedecinDto toDto(Medecin entity) {
        MedecinDto dto = new MedecinDto();
        dto.setId(entity.getId());
        dto.setNomMedecin(entity.getNomMedecin());
        dto.setPrenomMedecin(entity.getPrenomMedecin());
        dto.setRefGrade(entity.getRefGrade());
        dto.setRefSpecialite(entity.getRefSpecialite());
        dto.setEmail(entity.getEmail());
        dto.setPhotoPath(entity.getPhotoPath());
        dto.setActif(entity.isActif());
        return dto;
    }
    /**
     * Convertir le DTO de la demande medecin en entité Utilisateur
     */
    public Medecin toEntity(MedecinDto dto) {
        Medecin entity = new Medecin();
        entity.setId(dto.getId());
        entity.setNomMedecin(dto.getNomMedecin());
        entity.setPrenomMedecin(dto.getPrenomMedecin());
        entity.setRefGrade(dto.getRefGrade());
        entity.setRefSpecialite(dto.getRefSpecialite());
        entity.setEmail(dto.getEmail());
        entity.setMotDePasse(dto.getMotDePasse());
        entity.setPhotoPath(dto.getPhotoPath());
        entity.setActif(dto.isActif());
        return entity;
    }
}
