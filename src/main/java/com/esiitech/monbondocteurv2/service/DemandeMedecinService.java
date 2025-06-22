package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.DemandeMedecinDTO;
import com.esiitech.monbondocteurv2.mapper.DemandeMedecinMapper;
import com.esiitech.monbondocteurv2.model.DemandeMedecin;
import com.esiitech.monbondocteurv2.repository.DemandeMedecinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DemandeMedecinService {

    @Autowired
    private DemandeMedecinRepository repository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private DemandeMedecinMapper mapper;

    public DemandeMedecinDTO create(DemandeMedecinDTO dto) {
        if (repository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Un médecin avec cet email existe déjà.");
        }
        DemandeMedecin saved = repository.save(mapper.toEntity(dto));

        notificationService.envoyerConfirmationDemandeMedecin(saved);

        return mapper.toDto(saved);
    }

    public List<DemandeMedecinDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<DemandeMedecinDTO> getById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
