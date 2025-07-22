package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.AgendaMedecinDto;
import com.esiitech.monbondocteurv2.model.RefSpecialite;
import com.esiitech.monbondocteurv2.service.AgendaMedecinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/V2/agendas")
@Tag(name = "Agendas des Médecins", description = "Gestion des agendas (disponibilités) des médecins")
public class AgendaMedecinController {

    @Autowired
    private AgendaMedecinService service;

    @Operation(summary = "Lister tous les agendas")
    @GetMapping
    public ResponseEntity<List<AgendaMedecinDto>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Récupérer un agenda par ID")
    @GetMapping("/{id}")
    public ResponseEntity<AgendaMedecinDto> getById(@PathVariable String id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Créer un nouvel agenda")
    @PostMapping
    public ResponseEntity<AgendaMedecinDto> create(@RequestBody AgendaMedecinDto dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @Operation(summary = "Mettre à jour un agenda existant")
    @PutMapping("/{id}")
    public ResponseEntity<AgendaMedecinDto> update(@PathVariable String id, @RequestBody AgendaMedecinDto dto) {
        dto.setId(id);
        return ResponseEntity.ok(service.save(dto));
    }

    @Operation(summary = "Supprimer un agenda par ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lister les agendas d’un médecin dans une structure donnée")
    @GetMapping("/medecin/{medecinId}/structure/{structureId}")
    public ResponseEntity<List<AgendaMedecinDto>> getByMedecinAndStructure(
            @PathVariable String medecinId,
            @PathVariable Long structureId) {
        return ResponseEntity.ok(service.findByMedecinAndStructure(medecinId, structureId));
    }

    @Operation(summary = "Lister les agendas d’une structure pour une spécialité donnée")
    @GetMapping("/structure/{structureId}/specialite/{specialite}")
    public ResponseEntity<List<AgendaMedecinDto>> getByStructureAndSpecialite(
            @PathVariable Long structureId,
            @PathVariable RefSpecialite specialite) {
        return ResponseEntity.ok(service.getAgendasByStructureAndSpecialite(structureId, specialite));
    }

    @Operation(summary = "Désactiver un agenda par ID")
    @PutMapping("/{id}/desactiver")
    public ResponseEntity<String> desactiverAgenda(@PathVariable String id) {
        boolean result = service.desactiverAgenda(id);
        if (result) {
            return ResponseEntity.ok("Agenda désactivé avec succès.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Agenda introuvable.");
        }
    }
}
