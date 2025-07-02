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


        // Note : les relations avec les entités (medecin, date, horaire, etc.)
        // doivent être mises en place dans le service, pas ici
        return entity;
    }
}
