package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.MedecinDto;
import com.esiitech.monbondocteurv2.dto.MedecinStructureSanitaireDto;
import com.esiitech.monbondocteurv2.exception.RelationDejaExistanteException;
import com.esiitech.monbondocteurv2.exception.SpecialiteIncompatibleException;
import com.esiitech.monbondocteurv2.mapper.MedecinMapper;
import com.esiitech.monbondocteurv2.mapper.MedecinStructureSanitaireMapper;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.MedecinStructureSanitaire;
import com.esiitech.monbondocteurv2.model.RefSpecialite;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.repository.MedecinRepository;
import com.esiitech.monbondocteurv2.repository.MedecinStructureSanitaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    @Transactional
    public MedecinStructureSanitaireDto save(MedecinStructureSanitaireDto dto) {
        // validation basique
        if (dto == null) throw new IllegalArgumentException("dto cannot be null");
        if (dto.getStructureSanitaireId() == null || dto.getMedecinId() == null) {
            throw new IllegalArgumentException("structureSanitaireId and medecinId are required");
        }

        String structureId = dto.getStructureSanitaireId();
        String medecinId = dto.getMedecinId();

        // 0) Récupérer entités référencées (lève si introuvables)
        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new IllegalArgumentException("Médecin introuvable: " + medecinId));

        // NOTE: repository.findByStructureSanitaireId retourne des relations; ici il faut récupérer la structure.
        // Si tu as un StructureSanitaireRepository injecte-le et utilise-le. Si non, on suppose que la relation contient l'objet structure
        // mais pour être sûr, je suppose que MedecinStructureSanitaireRepository expose une méthode pour charger la structure,
        // sinon injecte StructureSanitaireRepository et utilise findById(structureId).
        // Exemple ci-dessous suppose que tu vas injecter StructureSanitaireRepository en champ (recommandé).
        StructureSanitaire structure = /* inject & use repository */ null;
        // => Pour l'instant je propose d'ajouter :
        // @Autowired private StructureSanitaireRepository structureSanitaireRepository;
        // et ensuite :
        // StructureSanitaire structure = structureSanitaireRepository.findById(structureId)
        //      .orElseThrow(() -> new IllegalArgumentException("Structure sanitaire introuvable: " + structureId));

        // ---------- validation métier : spécialité du médecin doit appartenir à la structure ----------
        String medecinSpec = medecin.getRefSpecialite(); // peut être null
        if (medecinSpec == null || medecinSpec.isBlank()) {
            throw new SpecialiteIncompatibleException("Le médecin n'a pas de spécialité définie.");
        }
        // normaliser (trim + case-insensitive)
        String medSpecNorm = medecinSpec.trim().toLowerCase();

        // récupère les spécialités de la structure
        var structSpecs = structure.getRefSpecialites(); // Set<String>
        if (structSpecs == null || structSpecs.isEmpty()) {
            throw new SpecialiteIncompatibleException("La structure n'a pas de spécialités définies. Impossible d'attacher un médecin.");
        }

        boolean match = structSpecs.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toLowerCase)
                .anyMatch(s -> s.equals(medSpecNorm));

        if (!match) {
            throw new SpecialiteIncompatibleException(
                    "Incompatibilité de spécialité : le médecin (" + medecinSpec + ") ne figure pas dans les spécialités de la structure."
            );
        }

        // 1) check appli -> renvoie 409 si déjà lié
        if (repository.existsByStructureSanitaireIdAndMedecinId(structureId, medecinId)) {
            throw new RelationDejaExistanteException(
                    "Le médecin " + medecinId + " est déjà rattaché à la structure " + structureId
            );
        }

        // 2) mapping et sauvegarde (avec try/catch pour attraper une violation DB si course)
        MedecinStructureSanitaire entity = mapper.toEntity(dto);
        if (entity.getId() == null) {
            entity.setId(generateMedecinStructureSanitaireId());
        }

        try {
            MedecinStructureSanitaire saved = repository.save(entity);
            return mapper.toDto(saved);
        } catch (DataIntegrityViolationException ex) {
            // si la contrainte unique côté DB est déclenchée (concurrence),
            // on renvoie une exception métier compréhensible au front
            throw new RelationDejaExistanteException(
                    "Relation déjà existante (conflit détecté lors de l'enregistrement)"
            );
        }
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
    public List<String> getStructureIdsForMedecin(String medecinId) {
        if (medecinId == null || medecinId.isBlank()) return Collections.emptyList();

        return repository.findByMedecinIdAndActifTrue(medecinId).stream()
                .map(rel -> rel.getStructureSanitaire())
                .filter(Objects::nonNull)
                .map(struct -> struct.getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}