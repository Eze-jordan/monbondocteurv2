// com.esiitech.monbondocteurv2.controller.PaiementsController.java
package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.PaiementCreateRequest;
import com.esiitech.monbondocteurv2.model.Paiements;
import com.esiitech.monbondocteurv2.service.PaiementsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/V2/paiements")
@Tag(name = "Paiements", description = "Gestion des paiements et des abonnements structures")
public class PaiementsController {

    private final PaiementsService paiementsService;

    public PaiementsController(PaiementsService paiementsService) {
        this.paiementsService = paiementsService;
    }

    @PostMapping
    @Operation(summary = "Créer un paiement",
            description = "Crée un paiement et met à jour l'abonnement de la structure (dates, abonneExpire, statut).")
    public ResponseEntity<Paiements> create(@RequestBody PaiementCreateRequest req) {
        Paiements saved = paiementsService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d’un paiement")
    public ResponseEntity<Paiements> get(@PathVariable String id) {
        return ResponseEntity.ok(paiementsService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Lister tous les paiements")
    public ResponseEntity<List<Paiements>> all() {
        return ResponseEntity.ok(paiementsService.listAll());
    }

    @GetMapping("/structure/{structureId}")
    @Operation(summary = "Lister les paiements d’une structure")
    public ResponseEntity<List<Paiements>> byStructure(@PathVariable String structureId) {
        return ResponseEntity.ok(paiementsService.listByStructure(structureId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un paiement")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        paiementsService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
