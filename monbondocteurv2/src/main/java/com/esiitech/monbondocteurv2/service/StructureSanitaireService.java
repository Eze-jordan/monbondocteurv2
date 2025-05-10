package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.StructureSanitaireDto;
import com.esiitech.monbondocteurv2.mapper.StructureSanitaireMapper;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StructureSanitaireService {

    @Autowired
    private StructureSanitaireRepository repository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private StructureSanitaireMapper mapper;

    public List<StructureSanitaireDto> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<StructureSanitaireDto> findById(Long id) {
        return repository.findById(id)
                .map(mapper::toDto);
    }

    public StructureSanitaireDto save(StructureSanitaireDto dto) {
        StructureSanitaire entity = mapper.toEntity(dto);
        entity.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        return mapper.toDto(repository.save(entity));
    }


    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
