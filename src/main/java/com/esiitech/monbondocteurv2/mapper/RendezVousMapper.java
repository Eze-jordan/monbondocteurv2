package com.esiitech.monbondocteurv2.mapper;

import com.esiitech.monbondocteurv2.dto.RendezVousDTO;
import com.esiitech.monbondocteurv2.model.RendezVous;
import org.springframework.stereotype.Component;

@Component
public class RendezVousMapper {

    public RendezVousDTO toDTO(RendezVous entity) {
        RendezVousDTO dto = new RendezVousDTO();
        dto.setId(entity.getId());

        if (entity.getStructureSanitaire() != null) {
            dto.setStructureSanitaireId(entity.getStructureSanitaire().getId());
        }

        if (entity.getMedecin() != null) {
            dto.setMedecinId(entity.getMedecin().getId());
        }

        if (entity.getAgendaMedecin() != null) {
            dto.setAgendaMedecinId(entity.getAgendaMedecin().getId());
        }

        if (entity.getDateRdv() != null) {
            dto.setDateRdvId(entity.getDateRdv().getId());
        }

        if (entity.getHoraireRdv() != null) {
            dto.setHoraireRdvId(entity.getHoraireRdv().getId());
        }

        dto.setRefSpecialite(entity.getRefSpecialite());
        dto.setNom(entity.getNom());
        dto.setPrenom(entity.getPrenom());
        dto.setEmail(entity.getEmail());
        dto.setSexe(entity.getSexe());
        dto.setAge(entity.getAge());
        dto.setMotif(entity.getMotif());

        return dto;
    }

    public RendezVous toEntity(RendezVousDTO dto) {
        RendezVous entity = new RendezVous();
        entity.setId(dto.getId());
        entity.setRefSpecialite(dto.getRefSpecialite());
        entity.setNom(dto.getNom());
        entity.setPrenom(dto.getPrenom());
        entity.setEmail(dto.getEmail());
        entity.setSexe(dto.getSexe());
        entity.setAge(dto.getAge());
        entity.setMotif(dto.getMotif());

        // Note : les relations avec les entités (medecin, date, horaire, etc.)
        // doivent être mises en place dans le service, pas ici
        return entity;
    }
}
