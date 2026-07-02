package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.AttributionRdvRequest;
import com.esiitech.monbondocteurv2.dto.PriseRdvRequest;
import com.esiitech.monbondocteurv2.dto.RendezVousDTO;
import com.esiitech.monbondocteurv2.service.RendezVousService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/V2/rendezvous")
@Tag(name = "Rendez-vous", description = "Gestion des rendez-vous médicaux")
public class RendezVousController {

    private final RendezVousService rendezVousService;

    public RendezVousController(RendezVousService rendezVousService) {
        this.rendezVousService = rendezVousService;
    }

    // ============================================================
    // CRÉATION
    // ============================================================

    @Operation(summary = "Créer un rendez-vous interne")
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RendezVousDTO> creer(@RequestBody RendezVousDTO dto) {
        return ResponseEntity.ok(rendezVousService.creerRendezVous(dto));
    }

    @PostMapping("/prendre")
    @Operation(summary = "Prendre un rendez-vous patient connecté")
    public ResponseEntity<RendezVousDTO> prendreRendezVous(
            @Valid @RequestBody PriseRdvRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rendezVousService.prendreRendezVous(request));
    }

    @PostMapping("/demande/service")
    @Operation(summary = "Créer une demande de rendez-vous par structure et service")
    public ResponseEntity<RendezVousDTO> creerDemandeParService(
            @Valid @RequestBody RendezVousDTO dto
    ) {
        return ResponseEntity.ok(
                rendezVousService.creerDemandeRdvStructureParService(dto)
        );
    }

    // ============================================================
    // ATTRIBUTION
    // ============================================================

    @PutMapping("/{rdvId}/attribuer")
    @Operation(summary = "Attribuer un rendez-vous en attente à un médecin")
    public ResponseEntity<RendezVousDTO> attribuer(
            @PathVariable String rdvId,
            @Valid @RequestBody AttributionRdvRequest req
    ) {
        return ResponseEntity.ok(rendezVousService.attribuerRdv(rdvId, req));
    }

    // ============================================================
    // LECTURE
    // ============================================================

    @Operation(summary = "Lister tous les rendez-vous")
    @GetMapping
    public ResponseEntity<List<RendezVousDTO>> listerTous() {
        return ResponseEntity.ok(rendezVousService.listerTous());
    }

    @Operation(summary = "Rendez-vous par ID")
    @GetMapping("/{id}")
    public ResponseEntity<RendezVousDTO> getById(@PathVariable String id) {
        return rendezVousService.trouverParId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================================
    // FILTRES
    // ============================================================

    @Operation(summary = "Rendez-vous par structure nom")
    @GetMapping("/structure")
    public ResponseEntity<List<RendezVousDTO>> getByStructure(
            @RequestParam String nom
    ) {
        return ResponseEntity.ok(rendezVousService.trouverParStructure(nom));
    }

    @Operation(summary = "Rendez-vous par médecin ID")
    @GetMapping("/medecin/{medecinId}")
    public ResponseEntity<List<RendezVousDTO>> getByMedecin(
            @PathVariable String medecinId
    ) {
        return ResponseEntity.ok(rendezVousService.trouverParMedecinId(medecinId));
    }

    @Operation(summary = "Rendez-vous par agenda ID")
    @GetMapping("/agenda/{agendaId}")
    public ResponseEntity<List<RendezVousDTO>> getByAgenda(
            @PathVariable String agendaId
    ) {
        return ResponseEntity.ok(rendezVousService.trouverParAgendaId(agendaId));
    }

    @Operation(summary = "Rendez-vous par médecin et structure")
    @GetMapping("/medecin/{medecinId}/structure/{structureId}")
    public ResponseEntity<List<RendezVousDTO>> getByMedecinAndStructure(
            @PathVariable String medecinId,
            @PathVariable String structureId
    ) {
        return ResponseEntity.ok(
                rendezVousService.trouverParMedecinIdEtStructureId(medecinId, structureId)
        );
    }

    // ============================================================
    // STRUCTURE : DEMANDES / DISPONIBILITÉS
    // ============================================================

    @GetMapping("/structure/{structureId}/en-attente")
    @Operation(summary = "Lister les demandes de rendez-vous en attente")
    public ResponseEntity<List<RendezVousDTO>> listerEnAttente(
            @PathVariable String structureId,
            @RequestParam(required = false) String specialite
    ) {
        return ResponseEntity.ok(
                rendezVousService.listerDemandesEnAttente(structureId, specialite)
        );
    }

    @GetMapping("/structure/{structureId}/medecins-disponibles")
    @Operation(summary = "Médecins disponibles avec créneaux pour une spécialité et une date")
    public ResponseEntity<List<Map<String, Object>>> getMedecinsDisponibles(
            @PathVariable String structureId,
            @RequestParam String specialite,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(
                rendezVousService.getMedecinsDisponibles(structureId, specialite, date)
        );
    }

    // ============================================================
    // PATIENT
    // ============================================================

    @GetMapping({
            "/patient/{patientId}",
            "/rendezvous/patient/{patientId}"
    })
    @Operation(summary = "Rendez-vous d'un patient")
    public ResponseEntity<List<RendezVousDTO>> getRendezVousParPatient(
            @PathVariable String patientId
    ) {
        return ResponseEntity.ok(
                rendezVousService.recupererRendezVousParPatientId(patientId)
        );
    }

    @GetMapping({
            "/patient/{patientId}/actifs",
            "/rendezvous/patient/{patientId}/actifs"
    })
    @Operation(summary = "Rendez-vous actifs d'un patient")
    public ResponseEntity<List<RendezVousDTO>> getRendezVousActifsParPatient(
            @PathVariable String patientId
    ) {
        return ResponseEntity.ok(
                rendezVousService.recupererRendezVousActifsParPatientId(patientId)
        );
    }

    // ============================================================
    // MODIFICATION STATUT
    // ============================================================

    @PutMapping("/{id}/statut")
    @Operation(summary = "Modifier le statut actif d'un rendez-vous")
    public ResponseEntity<RendezVousDTO> modifierStatut(
            @PathVariable("id") String rdvId,
            @RequestParam("actif") boolean actif
    ) {
        return ResponseEntity.ok(rendezVousService.modifierStatut(rdvId, actif));
    }

    @PutMapping("/journee/{journeeId}/statut")
    @Operation(summary = "Modifier le statut de tous les rendez-vous d'une journée")
    public ResponseEntity<List<RendezVousDTO>> modifierStatutTousParJournee(
            @PathVariable String journeeId,
            @RequestParam("actif") boolean actif
    ) {
        return ResponseEntity.ok(
                rendezVousService.modifierStatutTousParJournee(journeeId, actif)
        );
    }

    @PutMapping("/agenda/{agendaId}/statut")
    @Operation(summary = "Modifier le statut de tous les rendez-vous d'un agenda")
    public ResponseEntity<List<RendezVousDTO>> modifierStatutTousParAgenda(
            @PathVariable String agendaId,
            @RequestParam("actif") boolean actif
    ) {
        return ResponseEntity.ok(
                rendezVousService.modifierStatutTousParAgenda(agendaId, actif)
        );
    }

    // ============================================================
    // SUPPRESSION
    // ============================================================

    @Operation(summary = "Supprimer un rendez-vous")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable String id) {
        rendezVousService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}