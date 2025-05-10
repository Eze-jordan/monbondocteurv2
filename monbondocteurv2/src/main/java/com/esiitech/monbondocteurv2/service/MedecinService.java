package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.MedecinDto;
import com.esiitech.monbondocteurv2.mapper.MedecinMapper;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.repository.MedecinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedecinService {

    @Autowired private MedecinRepository repository;
    @Autowired private MedecinMapper mapper;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    public List<MedecinDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public Optional<MedecinDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    public MedecinDto save(MedecinDto dto) {
        Medecin entity = mapper.toEntity(dto);
        entity.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        return mapper.toDto(repository.save(entity));
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}