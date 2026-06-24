package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.model.Formules;
import com.esiitech.monbondocteurv2.repository.FormulesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class FormulesService {

    private final FormulesRepository repository;

    public FormulesService(FormulesRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Formules create(Formules f) {
        validate(f);

        if (f.getId() == null || f.getId().isBlank()) {
            f.setId(UUID.randomUUID().toString());
        }

        return repository.save(f);
    }

    @Transactional(readOnly = true)
    public Formules getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formule introuvable: " + id));
    }

    @Transactional(readOnly = true)
    public List<Formules> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Formules> getAllActives() {
        return repository.findByActifTrue();
    }

    @Transactional
    public Formules update(String id, Formules payload) {
        Formules existing = getById(id);

        if (payload.getNomFormule() != null) {
            existing.setNomFormule(payload.getNomFormule());
        }

        if (payload.getDescriptionFormule() != null) {
            existing.setDescriptionFormule(payload.getDescriptionFormule());
        }

        if (payload.getNombreDeMois() != 0) {
            existing.setNombreDeMois(payload.getNombreDeMois());
        }

        if (payload.getMontantTTC() != null) {
            existing.setMontantTTC(payload.getMontantTTC());
        }

        existing.setActif(payload.isActif());

        validate(existing);

        return repository.save(existing);
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Formule introuvable: " + id);
        }

        repository.deleteById(id);
    }

    private void validate(Formules f) {
        if (f.getNomFormule() == null || f.getNomFormule().isBlank()) {
            throw new IllegalArgumentException("Le nom de la formule est obligatoire.");
        }

        if (f.getNombreDeMois() <= 0) {
            throw new IllegalArgumentException("Le nombre de mois doit être supérieur à 0.");
        }

        if (f.getMontantTTC() == null) {
            throw new IllegalArgumentException("Le montant TTC est obligatoire.");
        }

        if (f.getMontantTTC().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le montant TTC ne peut pas être négatif.");
        }
    }
}