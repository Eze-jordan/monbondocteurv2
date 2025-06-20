package com.esiitech.monbondocteurv2.mapper;

import com.esiitech.monbondocteurv2.dto.DateRdvDto;
import com.esiitech.monbondocteurv2.model.AgendaMedecin;
import com.esiitech.monbondocteurv2.model.DateRdv;
import com.esiitech.monbondocteurv2.repository.AgendaMedecinRepository;
import com.esiitech.monbondocteurv2.repository.DateRdvRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DateRdvMapper {
    @Autowired
    private AgendaMedecinRepository agendaMedecinRepository;
    @Autowired
    private DateRdvRepository dateRdvRepository;

    public DateRdvDto toDto(DateRdv entity) {
        DateRdvDto dto = new DateRdvDto();
        dto.setId(entity.getId());
        dto.setAgendaMedecinId(entity.getAgendaMedecin().getId());
        dto.setNombrePatient(entity.getNombrePatient());
        dto.setRdvPris(entity.getRdvPris());
        dto.setDate(entity.getDate());
        return dto;
    }

    public DateRdv toEntity(DateRdvDto dto) {
        DateRdv entity = new DateRdv();
        entity.setId(dto.getId());
        entity.setNombrePatient(dto.getNombrePatient());
        entity.setRdvPris(dto.getRdvPris());
        AgendaMedecin agenda = agendaMedecinRepository.findById(dto.getAgendaMedecinId()).orElseThrow();
        entity.setAgendaMedecin(agenda);
        entity.setDate(dto.getDate());
        entity.setNombrePatient(dto.getNombrePatient());
        return entity;
    }
}