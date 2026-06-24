package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.ActivationFormuleStructureRequest;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.service.AbonnementStructureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/V2/abonnements")
@Tag(name = "Abonnements", description = "Gestion des abonnements des structures sanitaires")
public class AbonnementStructureController {

    private final AbonnementStructureService service;

    public AbonnementStructureController(AbonnementStructureService service) {
        this.service = service;
    }

    @PostMapping("/activation-test")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activer ou prolonger une formule sans paiement")
    public ResponseEntity<StructureSanitaire> activerTest(@RequestBody ActivationFormuleStructureRequest request) {
        return ResponseEntity.ok(service.activerOuProlonger(request));
    }

    @GetMapping("/structure/{structureId}/actif")
    @Operation(summary = "Vérifier si une structure a un abonnement actif")
    public ResponseEntity<Boolean> abonnementActif(@PathVariable String structureId) {
        return ResponseEntity.ok(service.abonnementActif(structureId));
    }

    @PutMapping("/structure/{structureId}/verifier-expiration")
    @Operation(summary = "Vérifier et marquer l'expiration d'un abonnement")
    public ResponseEntity<StructureSanitaire> verifierExpiration(@PathVariable String structureId) {
        return ResponseEntity.ok(service.verifierEtMarquerExpiration(structureId));
    }
}