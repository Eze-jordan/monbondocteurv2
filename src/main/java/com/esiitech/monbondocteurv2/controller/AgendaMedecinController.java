package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.AgendaMedecinDto;
import com.esiitech.monbondocteurv2.dto.AgendaSemainePlanifieeRequest;
import com.esiitech.monbondocteurv2.dto.AgendaSemaineRequest;
import com.esiitech.monbondocteurv2.dto.AgendaWeekStatusRequest;
import com.esiitech.monbondocteurv2.service.AgendaMedecinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/V2/agendas")
@Tag(name = "Agendas des Médecins", description = "Gestion des agendas et disponibilités des médecins")
public class AgendaMedecinController {

    private final AgendaMedecinService service;

    public AgendaMedecinController(AgendaMedecinService service) {
        this.service = service;
    }

    // ============================================================
    // CRÉATION / MODIFICATION
    // ============================================================

    @Operation(
            summary = "Créer ou modifier un agenda",
            description = "Crée ou met à jour un agenda pour un médecin dans une structure."
    )
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AgendaMedecinDto> save(@RequestBody AgendaMedecinDto dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @Operation(
            summary = "Créer ou modifier un agenda pour une semaine",
            description = "Crée ou met à jour les agendas d'un médecin pour une semaine complète."
    )
    @PostMapping(
            value = "/semaine",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<AgendaMedecinDto>> creerAgendaSemaine(
            @RequestBody AgendaSemaineRequest request
    ) {
        return ResponseEntity.ok(service.saveWeek(request));
    }

    // ============================================================
    // LECTURE
    // ============================================================

    @Operation(
            summary = "Lister tous les agendas récents d'un médecin",
            description = "Retourne les agendas récents d'un médecin à partir de la date du jour."
    )
    @GetMapping(value = "/medecin/{medecinId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AgendaMedecinDto>> getByMedecin(
            @PathVariable String medecinId
    ) {
        return ResponseEntity.ok(
                service.getAgendasRecentsByMedecin(medecinId, LocalDate.now())
        );
    }

    @Operation(
            summary = "Lister les agendas récents d'une structure sanitaire",
            description = "Retourne les agendas récents associés à une structure à partir de la date du jour."
    )
    @GetMapping(value = "/structure/{structureId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AgendaMedecinDto>> getByStructure(
            @PathVariable String structureId
    ) {
        return ResponseEntity.ok(
                service.getAgendasRecentsByStructure(structureId, LocalDate.now())
        );
    }

    // ============================================================
    // MODIFICATION D'UN JOUR
    // ============================================================

    @Operation(
            summary = "Modifier un jour précis",
            description = "Met à jour un agenda précis et ses plages horaires."
    )
    @PutMapping(
            value = "/{agendaId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AgendaMedecinDto> updateDay(
            @PathVariable String agendaId,
            @RequestBody AgendaMedecinDto dto
    ) {
        dto.setId(agendaId);
        return ResponseEntity.ok(service.updateDay(dto));
    }

    // ============================================================
    // MODIFICATION DE SEMAINE
    // ============================================================

    @Operation(
            summary = "Mettre à jour la semaine en cours",
            description = "Met à jour la semaine courante. Le service vérifie les règles métier et les rendez-vous existants."
    )
    @PutMapping(
            value = "/week/current",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<AgendaMedecinDto>> updateWeekCurrent(
            @RequestBody AgendaSemaineRequest request
    ) {
        return ResponseEntity.ok(service.updateWeekCurrent(request));
    }

    @Operation(
            summary = "Activer ou désactiver tous les jours de la semaine",
            description = "Met autorise=true ou autorise=false sur tous les jours de la semaine demandée."
    )
    @PutMapping(
            value = "/week/autorise",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<AgendaMedecinDto>> updateAutorisationSemaine(
            @RequestBody AgendaWeekStatusRequest request
    ) {
        return ResponseEntity.ok(service.updateWeekAutorisation(request));
    }

    @Operation(
            summary = "Planifier une mise à jour future de la semaine",
            description = "Crée une nouvelle version d'agenda avec effectiveFrom. Peut décaler, annuler ou refuser selon la policy."
    )
    @PutMapping(
            value = "/semaine/planifier",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> planifierSemaine(
            @RequestBody AgendaSemainePlanifieeRequest request
    ) {
        LocalDate start = service.planifierUpdateWeek(request);
        return ResponseEntity.ok("Semaine planifiée à partir de : " + start);
    }

    // ============================================================
    // SUPPRESSION
    // ============================================================

    @Operation(
            summary = "Supprimer un agenda",
            description = "Supprime définitivement un agenda par son ID."
    )
    @DeleteMapping("/{agendaId}")
    public ResponseEntity<Void> delete(
            @PathVariable String agendaId
    ) {
        service.delete(agendaId);
        return ResponseEntity.noContent().build();
    }
}