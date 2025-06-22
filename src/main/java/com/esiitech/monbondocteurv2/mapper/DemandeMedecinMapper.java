package com.esiitech.monbondocteurv2.mapper;

import com.esiitech.monbondocteurv2.dto.DemandeMedecinDTO;
import com.esiitech.monbondocteurv2.model.DemandeMedecin;
import org.springframework.stereotype.Component;

@Component
public class DemandeMedecinMapper {

    public DemandeMedecinDTO toDto(DemandeMedecin entity) {
        DemandeMedecinDTO dto = new DemandeMedecinDTO();
        dto.setId(entity.getId());
        dto.setNomMedecin(entity.getNomMedecin());
        dto.setPrenomMedecin(entity.getPrenomMedecin());
        dto.setRefGrade(entity.getRefGrade());
        dto.setRefSpecialite(entity.getRefSpecialite());
        dto.setEmail(entity.getEmail());
        dto.setMatricule(entity.getMatricule());
        return dto;
    }

    public DemandeMedecin toEntity(DemandeMedecinDTO dto) {
        DemandeMedecin entity = new DemandeMedecin();
        entity.setId(dto.getId());
        entity.setNomMedecin(dto.getNomMedecin());
        entity.setPrenomMedecin(dto.getPrenomMedecin());
        entity.setRefGrade(dto.getRefGrade());
        entity.setRefSpecialite(dto.getRefSpecialite());
        entity.setEmail(dto.getEmail());
        entity.setMatricule(dto.getMatricule());
        return entity;
    }
}
