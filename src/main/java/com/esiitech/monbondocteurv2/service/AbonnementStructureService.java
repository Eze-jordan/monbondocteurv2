package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.ActivationFormuleStructureRequest;
import com.esiitech.monbondocteurv2.model.Formules;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.repository.FormulesRepository;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.esiitech.monbondocteurv2.exception.AbonnementExpireException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@Service
public class AbonnementStructureService {

    private final StructureSanitaireRepository structureRepository;
    private final FormulesRepository formulesRepository;

    public AbonnementStructureService(
            StructureSanitaireRepository structureRepository,
            FormulesRepository formulesRepository
    ) {
        this.structureRepository = structureRepository;
        this.formulesRepository = formulesRepository;
    }

    @Transactional
    public StructureSanitaire activerOuProlonger(ActivationFormuleStructureRequest request) {
        validateRequest(request);

        StructureSanitaire structure = structureRepository.findById(request.getStructureId())
                .orElseThrow(() -> new IllegalArgumentException("Structure sanitaire introuvable: " + request.getStructureId()));

        Formules formule = formulesRepository.findById(request.getFormuleId())
                .orElseThrow(() -> new IllegalArgumentException("Formule introuvable: " + request.getFormuleId()));

        Date now = new Date();

        Date ancienneDateFin = structure.getDateFinAbonnement();

        Date baseDate;

        if (ancienneDateFin != null && ancienneDateFin.after(now)) {
            baseDate = ancienneDateFin;
        } else {
            baseDate = now;
            structure.setDateDebutAbonnement(now);
        }

        Date nouvelleDateFin = addMonths(baseDate, formule.getNombreDeMois());

        structure.setFormuleAbonnement(formule);
        structure.setDateFinAbonnement(nouvelleDateFin);
        structure.setAbonneExpire(false);
        structure.setActif(true);

        return structureRepository.save(structure);
    }

    @Transactional(readOnly = true)
    public boolean abonnementActif(String structureId) {
        StructureSanitaire structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new IllegalArgumentException("Structure sanitaire introuvable: " + structureId));

        Date now = new Date();

        return structure.isActif()
                && !structure.isAbonneExpire()
                && structure.getDateFinAbonnement() != null
                && structure.getDateFinAbonnement().after(now);
    }

    @Transactional
    public StructureSanitaire verifierEtMarquerExpiration(String structureId) {
        StructureSanitaire structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new IllegalArgumentException("Structure sanitaire introuvable: " + structureId));

        Date now = new Date();

        if (structure.getDateFinAbonnement() == null || !structure.getDateFinAbonnement().after(now)) {
            structure.setAbonneExpire(true);
            structure.setActif(false);
        }

        return structureRepository.save(structure);
    }

    private Date addMonths(Date date, int months) {
        ZonedDateTime zonedDateTime = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .plusMonths(months);

        return Date.from(zonedDateTime.toInstant());
    }

    private void validateRequest(ActivationFormuleStructureRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Le body est obligatoire.");
        }

        if (request.getStructureId() == null || request.getStructureId().isBlank()) {
            throw new IllegalArgumentException("structureId est obligatoire.");
        }

        if (request.getFormuleId() == null || request.getFormuleId().isBlank()) {
            throw new IllegalArgumentException("formuleId est obligatoire.");
        }
    }

    public void verifierAccesAbonnement(String structureId) {
        StructureSanitaire structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new IllegalArgumentException("Structure sanitaire introuvable"));

        Date now = new Date();

        boolean abonnementValide = structure.isActif()
                && !structure.isAbonneExpire()
                && structure.getDateFinAbonnement() != null
                && structure.getDateFinAbonnement().after(now);

        if (!abonnementValide) {
            structure.setActif(false);
            structure.setAbonneExpire(true);
            structureRepository.save(structure);

            throw new AbonnementExpireException(
                    "Votre abonnement est expiré. Veuillez renouveler votre formule pour continuer."
            );
        }
    }

}