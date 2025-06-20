package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.AgendaMedecinDto;
import com.esiitech.monbondocteurv2.mapper.AgendaMedecinMapper;
import com.esiitech.monbondocteurv2.model.AgendaMedecin;
import com.esiitech.monbondocteurv2.repository.AgendaMedecinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AgendaMedecinService {

    @Autowired private AgendaMedecinRepository repository;
    @Autowired private AgendaMedecinMapper mapper;

    public List<AgendaMedecinDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public Optional<AgendaMedecinDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    public AgendaMedecinDto save(AgendaMedecinDto dto) {
        AgendaMedecin entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
