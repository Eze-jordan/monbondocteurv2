// com.esiitech.monbondocteurv2.service.PaiementsService.java
package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.PaiementCreateRequest;
import com.esiitech.monbondocteurv2.model.Formules;
import com.esiitech.monbondocteurv2.model.Paiements;
import com.esiitech.monbondocteurv2.model.Statut;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.repository.FormulesRepository;
import com.esiitech.monbondocteurv2.repository.PaiementsRepository;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class PaiementsService {

    private final PaiementsRepository paiementsRepository;
    private final StructureSanitaireRepository structureRepository;
    private final FormulesRepository formulesRepository;

    public PaiementsService(PaiementsRepository paiementsRepository,
                            StructureSanitaireRepository structureRepository,
                            FormulesRepository formulesRepository) {
        this.paiementsRepository = paiementsRepository;
        this.structureRepository = structureRepository;
        this.formulesRepository = formulesRepository;
    }

    @Transactional
    public Paiements create(PaiementCreateRequest req) {
        StructureSanitaire ss = structureRepository.findById(req.getStructureId())
                .orElseThrow(() -> new IllegalArgumentException("StructureSanitaire introuvable"));

        Formules f = formulesRepository.findById(req.getFormulesId())
                .orElseThrow(() -> new IllegalArgumentException("Formule introuvable"));

        Paiements p = new Paiements();
        p.setId(generatePaymentId());
        p.setStructureSanitaire(ss);
        p.setFormules(f);
        p.setReference(req.getReference());
        p.setServicePaiement(req.getServicePaiement());
        p.setCompteDebite(req.getCompteDebite());
        p.setMontantPaye(req.getMontantPaye());

        Paiements saved = paiementsRepository.save(p);

        // === Mise à jour de l'abonnement de la structure ===
        // Point de départ: si la structure a déjà un abonnement en cours (DateFin future),
        // on prolonge à partir de DateFin ; sinon, on démarre maintenant.
        Date now = new Date();
        Date start;
        if (ss.getDateFinAbonnement() != null && ss.getDateFinAbonnement().after(now)) {
            start = ss.getDateFinAbonnement();
        } else {
            start = now;
            ss.setDateDebutAbonnement(start);
        }

        Date fin = addMonths(start, f.getNombreDeMois());
        ss.setDateFinAbonnement(fin);
        ss.setAbonneExpire(false);
        // Optionnel : activer le statut si paiement validé
        ss.setStatut(Statut.ACTIF);

        structureRepository.save(ss);

        return saved;
    }

    public Paiements findById(String id) {
        return paiementsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paiement introuvable"));
    }

    public List<Paiements> listAll() {
        return paiementsRepository.findAll();
    }

    public List<Paiements> listByStructure(String structureId) {
        return paiementsRepository.findByStructureSanitaire_Id(structureId);
    }

    public void delete(String id) {
        paiementsRepository.deleteById(id);
    }

    // Helpers

    private String generatePaymentId() {
        return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private Date addMonths(Date start, int months) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate base = start.toInstant().atZone(zone).toLocalDate();
        LocalDate fin = base.plusMonths(months);
        return Date.from(fin.atStartOfDay(zone).toInstant());
    }
}
