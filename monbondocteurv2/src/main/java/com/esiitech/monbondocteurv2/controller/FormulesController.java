package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.model.Formules;
import com.esiitech.monbondocteurv2.service.FormulesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/V2/formules")
@Tag(name = "Formules", description = "Gestion des formules d’abonnement")
public class FormulesController {

    private final FormulesService service;

    public FormulesController(FormulesService service) {
        this.service = service;
    }

    // ---- CREATE (ADMIN) ----
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer une formule")
    public ResponseEntity<Formules> create(@RequestBody Formules payload) {
        Formules created = service.create(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ---- READ ----
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une formule par ID")
    public ResponseEntity<Formules> getOne(@PathVariable String id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les formules")
    public ResponseEntity<List<Formules>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // ---- UPDATE (ADMIN) ----
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour une formule")
    public ResponseEntity<Formules> update(@PathVariable String id, @RequestBody Formules payload) {
        return ResponseEntity.ok(service.update(id, payload));
    }

    // ---- DELETE (ADMIN) ----
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer une formule")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
