package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.AgendaMedecinDto;
import com.esiitech.monbondocteurv2.dto.AgendaSemaineRequest;
import com.esiitech.monbondocteurv2.dto.AgendaStatusRequest;
import com.esiitech.monbondocteurv2.dto.AgendaWeekStatusRequest;
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

    private final AgendaMedecinService service;
    @Autowired
    private AgendaMedecinService agendaMedecinService;

    @Autowired
    public AgendaMedecinController(AgendaMedecinService service) {
        this.service = service;
    }

    /* =========================
       CRÉATION / MODIFICATION
       ========================= */

    @Operation(
            summary = "Créer ou modifier un agenda",
            description = "Crée ou met à jour un agenda pour un médecin dans une structure"
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AgendaMedecinDto> save(
            @RequestBody AgendaMedecinDto dto
    ) {
        return ResponseEntity.ok(service.save(dto));
    }

    @Operation(
            summary = "Créer ou modifier un agenda pour une semaine",
            description = "Crée ou met à jour un agenda pour un médecin dans une structure"
    )
    @PostMapping("/semaine")
    public List<AgendaMedecinDto> creerAgendaSemaine(
            @RequestBody AgendaSemaineRequest request) {
        return service.saveWeek(request);
    }

    /* =========================
       LECTURE
       ========================= */

    @Operation(
            summary = "Lister tous les agendas d’un médecin",
            description = "Retourne tous les agendas (actifs et inactifs) d’un médecin"
    )
    @GetMapping(value = "/medecin/{medecinId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AgendaMedecinDto>> getByMedecin(
            @PathVariable String medecinId
    ) {
        return ResponseEntity.ok(service.getAllByMedecin(medecinId));
    }

    @Operation(
            summary = "Lister les agendas d’une structure sanitaire",
            description = "Retourne tous les agendas associés à une structure"
    )
    @GetMapping(value = "/structure/{structureId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AgendaMedecinDto>> getByStructure(
            @PathVariable String structureId
    ) {
        return ResponseEntity.ok(service.getByStructure(structureId));
    }

    /* =========================
       SUPPRESSION
       ========================= */

    @Operation(
            summary = "Supprimer un agenda",
            description = "Supprime définitivement un agenda par son ID"
    )
    @DeleteMapping("/{agendaId}")
    public ResponseEntity<Void> delete(
            @PathVariable String agendaId
    ) {
        service.delete(agendaId);
        return ResponseEntity.noContent().build();
    }
    /* ============================================================
    MODIFICATION D’UN JOUR PRÉCIS
    ============================================================ */
    @PutMapping("/{agendaId}")
    public ResponseEntity<AgendaMedecinDto> updateDay(
            @PathVariable String agendaId,
            @RequestBody AgendaMedecinDto dto
    ) {
        dto.setId(agendaId);
        return ResponseEntity.ok(service.updateDay(dto));
    }

    /* ============================================================
       MODIFICATION DE TOUTE LA SEMAINE
       ============================================================ */
    @PutMapping("/week")
    public ResponseEntity<List<AgendaMedecinDto>> updateWeek(
            @RequestBody AgendaSemaineRequest request
    ) {
        return ResponseEntity.ok(service.updateWeek(request));
    }

    /**
     * 🔄 Modifier un seul jour de l'agenda
     */
    @PutMapping("/day")
    public ResponseEntity<AgendaMedecinDto> updateDay(
            @RequestBody AgendaMedecinDto dto
    ) {
        AgendaMedecinDto updated = service.updateDay(dto);
        return ResponseEntity.ok(updated);
    }
    /**
     * ✅ Autoriser tous les jours de la semaine
     */
    @PutMapping("/week/autorise")
    @Operation(
            summary = "Autoriser toute la semaine",
            description = "Met autorise=true pour tous les jours de l’agenda d’un médecin"
    )
    public ResponseEntity<List<AgendaMedecinDto>> autoriserTouteLaSemaine(
            @RequestBody AgendaWeekStatusRequest request
    ) {
        request.setAutorise(true);
        return ResponseEntity.ok(service.updateWeekAutorisation(request));
    }


}
