package com.esiitech.monbondocteurv2.mapper;

import com.esiitech.monbondocteurv2.dto.RendezVousDTO;
import com.esiitech.monbondocteurv2.model.RendezVous;
import org.springframework.stereotype.Component;
@Component
public class RendezVousMapper {

    public RendezVousDTO toDTO(RendezVous entity) {
        RendezVousDTO dto = new RendezVousDTO();
        dto.setId(entity.getId());
        dto.setNom(entity.getNom());
        dto.setPrenom(entity.getPrenom());
        dto.setEmail(entity.getEmail());
        dto.setTelephone(entity.getTelephone());
        dto.setAdresse(entity.getAdresse());
        dto.setSexe(entity.getSexe());
        dto.setAge(entity.getAge());
        dto.setMotif(entity.getMotif());
        dto.setDate(entity.getDate());
        dto.setRefSpecialites(entity.getRefSpecialites());
        dto.setHeureDebut(entity.getHeureDebut());
        dto.setActif(entity.isActif());
        dto.setStatut(entity.getStatut());

        if (entity.getAgendaMedecin() != null) {
            dto.setAgendaId(entity.getAgendaMedecin().getId());
        }

        // (optionnel) structureId
        if (entity.getStructureSanitaire() != null) {
            dto.setStructureId(entity.getStructureSanitaire().getId());
        }
        return dto;
    }

    public RendezVous toEntity(RendezVousDTO dto) {
        RendezVous entity = new RendezVous();
        entity.setId(dto.getId());
        entity.setNom(dto.getNom());
        entity.setPrenom(dto.getPrenom());
        entity.setEmail(dto.getEmail());
        entity.setTelephone(dto.getTelephone());
        entity.setAdresse(dto.getAdresse());
        entity.setSexe(dto.getSexe());
        entity.setAge(dto.getAge());
        entity.setMotif(dto.getMotif());
        entity.setDate(dto.getDate());
        entity.setAgendaId(dto.getAgendaId());
        entity.setRefSpecialites(dto.getRefSpecialites());
        entity.setHeureDebut(dto.getHeureDebut());
        entity.setActif(dto.isActif());
        entity.setStatut(dto.getStatut());


        // Note : les relations avec les entités (medecin, date, horaire, etc.)
        // doivent être mises en place dans le service, pas ici
        return entity;
    }
}
