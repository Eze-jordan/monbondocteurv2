package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.MedecinDto;
import com.esiitech.monbondocteurv2.dto.MedecinStructureSanitaireDto;
import com.esiitech.monbondocteurv2.mapper.MedecinMapper;
import com.esiitech.monbondocteurv2.mapper.MedecinStructureSanitaireMapper;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.MedecinStructureSanitaire;
import com.esiitech.monbondocteurv2.model.RefSpecialite;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.repository.MedecinRepository;
import com.esiitech.monbondocteurv2.repository.MedecinStructureSanitaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedecinStructureSanitaireService {

    @Autowired private MedecinStructureSanitaireRepository repository;
    @Autowired private MedecinStructureSanitaireMapper mapper;
    @Autowired private MedecinRepository medecinRepository;
    @Autowired private MedecinMapper medecinMapper;

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

    /**
     * Retourne tous les médecins rattachés à la structure donnée (sans pagination).
     * @param structureId identifiant de la structure (ici String pour correspondre à repository.findByStructureSanitaireId)
     * @return liste de MedecinDto (peut être vide si aucun médecin)
     */
    public List<MedecinDto> getAllMedecinsByStructure(String structureId) {
        if (structureId == null || structureId.isBlank()) {
            return Collections.emptyList();
        }

        List<MedecinStructureSanitaire> relations = repository.findByStructureSanitaireId(structureId);
        return relations.stream()
                .map(MedecinStructureSanitaire::getMedecin)   // récupère l'entité Medecin depuis la relation
                .filter(Objects::nonNull)                     // sécurité si relation mal peuplée
                .map(medecinMapper::toDto)                   // conversion en DTO
                .collect(Collectors.toList());
    }

    public List<MedecinDto> getActiveMedecinsByStructure(String structureId) {
        if (structureId == null || structureId.isBlank()) {
            return Collections.emptyList();
        }

        List<MedecinStructureSanitaire> relations = repository.findByStructureSanitaireIdAndActifTrue(structureId);
        return relations.stream()
                .map(MedecinStructureSanitaire::getMedecin)
                .filter(Objects::nonNull)
                .map(medecinMapper::toDto)
                .collect(Collectors.toList());
    }

}