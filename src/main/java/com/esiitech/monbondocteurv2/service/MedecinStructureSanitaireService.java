package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.MedecinStructureSanitaireDto;
import com.esiitech.monbondocteurv2.mapper.MedecinStructureSanitaireMapper;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.MedecinStructureSanitaire;
import com.esiitech.monbondocteurv2.model.RefSpecialite;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.repository.MedecinRepository;
import com.esiitech.monbondocteurv2.repository.MedecinStructureSanitaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedecinStructureSanitaireService {

    @Autowired private MedecinStructureSanitaireRepository repository;
    @Autowired private MedecinStructureSanitaireMapper mapper;
    @Autowired private MedecinRepository medecinRepository;

    public List<MedecinStructureSanitaireDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public Optional<MedecinStructureSanitaireDto> findById(String id) {
        return repository.findById(id).map(mapper::toDto);
    }

    public MedecinStructureSanitaireDto save(MedecinStructureSanitaireDto dto) {
        MedecinStructureSanitaire entity = mapper.toEntity(dto);

        if (entity.getId() == null) {
            entity.setId(generateMedecinStructureSanitaireId());
        }
        return mapper.toDto(repository.save(entity));
    }
    private String generateMedecinStructureSanitaireId() {
        return " MedecinStructureSanitaire-" + java.util.UUID.randomUUID();
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }

    public List<Medecin> getMedecinsByStructureAndSpecialite(Long structureId, RefSpecialite specialite) {
        return repository.findMedecinsByStructureAndSpecialite(structureId, specialite);
    }

    public StructureSanitaire getStructureSanitaireActifByMedecin(Medecin medecin) {
        return repository.findByMedecinAndActifTrue(medecin)
                .map(MedecinStructureSanitaire::getStructureSanitaire)
                .orElse(null); // ou gérer avec exception si nécessaire
    }
    public Optional<Medecin> getMedecinByEmail(String email) {
        return medecinRepository.findByEmail(email);
    }



}