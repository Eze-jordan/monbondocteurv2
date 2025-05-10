package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.DateRdvDto;
import com.esiitech.monbondocteurv2.mapper.DateRdvMapper;
import com.esiitech.monbondocteurv2.model.DateRdv;
import com.esiitech.monbondocteurv2.repository.DateRdvRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DateRdvService {

    @Autowired private DateRdvRepository repository;
    @Autowired private DateRdvMapper mapper;

    public List<DateRdvDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public Optional<DateRdvDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    public DateRdvDto save(DateRdvDto dto) {
        DateRdv entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public List<DateRdvDto> findByMedecinId(Long medecinId) {
        return repository.findByAgendaMedecin_Medecin_Id(medecinId)
                .stream().map(mapper::toDto).collect(Collectors.toList());
    }
}