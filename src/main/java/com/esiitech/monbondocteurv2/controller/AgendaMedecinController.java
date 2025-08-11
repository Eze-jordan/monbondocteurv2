package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.AgendaMedecinDto;
import com.esiitech.monbondocteurv2.model.RefSpecialite;
import com.esiitech.monbondocteurv2.service.AgendaMedecinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/V2/agendas")
@Tag(name = "Agendas des Médecins", description = "Gestion des agendas (disponibilités) des médecins")
public class AgendaMedecinController {

    @Autowired
    private AgendaMedecinService service;

    @Operation(
            tags = "Agendas des Médecins",
            summary = "Lister tous les agendas",
            description = "Retourne la liste de tous les agendas de médecins."
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AgendaMedecinDto>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(
            tags = "Agendas des Médecins",
            summary = "Récupérer un agenda par ID",
            description = "Retourne un agenda spécifique à partir de son identifiant."
    )
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AgendaMedecinDto> getById(@PathVariable String id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            tags = "Agendas des Médecins",
            summary = "Créer un nouvel agenda",
            description = "Crée un agenda pour un médecin."
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AgendaMedecinDto> create(@RequestBody AgendaMedecinDto dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @Operation(
            tags = "Agendas des Médecins",
            summary = "Mettre à jour un agenda existant",
            description = "Met à jour un agenda à partir de son identifiant."
    )
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AgendaMedecinDto> update(@PathVariable String id, @RequestBody AgendaMedecinDto dto) {
        dto.setId(id);
        return ResponseEntity.ok(service.save(dto));
    }

    @Operation(
            tags = "Agendas des Médecins",
            summary = "Supprimer un agenda par ID",
            description = "Supprime un agenda à partir de son identifiant."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            tags = "Agendas des Médecins",
            summary = "Lister les agendas d’un médecin dans une structure donnée",
            description = "Retourne tous les agendas d’un médecin pour une structure précise."
    )
    @GetMapping(value = "/medecin/{medecinId}/structure/{structureId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AgendaMedecinDto>> getByMedecinAndStructure(
            @PathVariable String medecinId,
            @PathVariable Long structureId) {
        return ResponseEntity.ok(service.findByMedecinAndStructure(medecinId, structureId));
    }

    @Operation(
            tags = "Agendas des Médecins",
            summary = "Lister les agendas d’une structure pour une spécialité donnée",
            description = "Retourne les agendas d’une structure filtrés par spécialité."
    )
    @GetMapping(value = "/structure/{structureId}/specialite/{specialite}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AgendaMedecinDto>> getByStructureAndSpecialite(
            @PathVariable Long structureId,
            @PathVariable RefSpecialite specialite) {
        return ResponseEntity.ok(service.getAgendasByStructureAndSpecialite(structureId, specialite));
    }

    @Operation(
            tags = "Agendas des Médecins",
            summary = "Désactiver un agenda par ID",
            description = "Désactive un agenda (indisponible à la prise de rendez-vous)."
    )
    @PutMapping(value = "/{id}/desactiver", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> desactiverAgenda(@PathVariable String id) {
        boolean result = service.desactiverAgenda(id);
        if (result) {
            return ResponseEntity.ok("Agenda désactivé avec succès.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Agenda introuvable.");
        }
    }
}
