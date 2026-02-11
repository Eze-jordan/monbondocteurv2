package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.*;
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
    @GetMapping(value = "/medecin/{medecinId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AgendaMedecinDto>> getByMedecin(@PathVariable String medecinId) {
        return ResponseEntity.ok(service.getAgendasRecentsByMedecin(medecinId, java.time.LocalDate.now()));
    }


    @Operation(
            summary = "Lister les agendas d’une structure sanitaire",
            description = "Retourne tous les agendas associés à une structure"
    )
    @GetMapping(value = "/structure/{structureId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AgendaMedecinDto>> getByStructure(@PathVariable String structureId) {
        return ResponseEntity.ok(service.getAgendasRecentsByStructure(structureId, java.time.LocalDate.now()));
    }


    /**
     * ✅ Met à jour la semaine en cours (effectiveFrom = lundi de la semaine courante).
     * Règle métier (dans le service) :
     * - s'il existe des RDV sur la semaine => exception (409) demandant de fermer les journées d'activité
     * - sinon => update des plages horaires
     */
    @PutMapping("/week/current")
    public ResponseEntity<List<AgendaMedecinDto>> updateWeekCurrent(
            @RequestBody AgendaSemaineRequest request
    ) {
        List<AgendaMedecinDto> result = service.updateWeekCurrent(request);
        return ResponseEntity.ok(result);
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
       ============================================================
    @PutMapping("/week")
    public ResponseEntity<List<AgendaMedecinDto>> updateWeek(
            @RequestBody AgendaSemaineRequest request
    ) {
        return ResponseEntity.ok(service.updateWeek(request));
    }
 */
    /**
     * ✅ Autoriser tous les jours de la semaine
     */
    @PutMapping("/week/autorise")
    @Operation(
            summary = "Mettre à jour l’autorisation de toute la semaine",
            description = "Met autorise=true ou false pour tous les jours"
    )
    public ResponseEntity<List<AgendaMedecinDto>> updateAutorisationSemaine(
            @RequestBody AgendaWeekStatusRequest request
    ) {
        return ResponseEntity.ok(service.updateWeekAutorisation(request));
    }
/*
    @PutMapping("/day/plages/autorise")
    public ResponseEntity<AgendaMedecinDto> updatePlagesAutorisationByDay(
            @RequestBody PlagesDayStatusRequest request
    ) {
        return ResponseEntity.ok(service.updatePlagesAutorisationByDay(request));
    }
*/
@PutMapping("/semaine/planifier")
@Operation(
        summary = "Planifier une mise à jour de la semaine (agenda versionné)",
        description = "Crée une nouvelle version d'agenda avec effectiveFrom. Peut décaler/annuler/refuser selon la policy."
)
public ResponseEntity<String> planifierSemaine(
        @RequestBody AgendaSemainePlanifieeRequest request
) {
    var start = service.planifierUpdateWeek(request);
    return ResponseEntity.ok("Semaine planifiée à partir de : " + start);
}

}
