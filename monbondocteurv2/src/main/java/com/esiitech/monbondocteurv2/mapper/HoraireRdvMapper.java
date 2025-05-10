package com.esiitech.monbondocteurv2.mapper;

import com.esiitech.monbondocteurv2.dto.HoraireRdvDto;
import com.esiitech.monbondocteurv2.model.DateRdv;
import com.esiitech.monbondocteurv2.model.HoraireRdv;
import com.esiitech.monbondocteurv2.repository.DateRdvRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HoraireRdvMapper {

    @Autowired
    private DateRdvRepository dateRdvRepository;

    public HoraireRdvDto toDto(HoraireRdv entity) {
        HoraireRdvDto dto = new HoraireRdvDto();
        dto.setId(entity.getId());
        dto.setDateRdvId(entity.getDateRdv().getId());
        dto.setHeureDebut(entity.getHeureDebut());
        dto.setHeureFin(entity.getHeureFin());
        return dto;
    }

    public HoraireRdv toEntity(HoraireRdvDto dto) {
        HoraireRdv entity = new HoraireRdv();
        entity.setId(dto.getId());
        entity.setHeureDebut(dto.getHeureDebut());
        entity.setHeureFin(dto.getHeureFin());
        DateRdv dateRdv = dateRdvRepository.findById(dto.getDateRdvId()).orElseThrow();
        entity.setDateRdv(dateRdv);
        return entity;
    }
}