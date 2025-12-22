package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.RendezVousDTO;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.service.RendezVousService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/V2/rendezvous")
@Tag(name = "Rendez-vous", description = "Gestion des rendez-vous médicaux")
public class RendezVousController {

    private final RendezVousService rendezVousService;

    public RendezVousController(RendezVousService rendezVousService) {
        this.rendezVousService = rendezVousService;
    }

    /* =========================
       CRÉATION
       ========================= */
    @Operation(summary = "Créer un rendez-vous")
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RendezVousDTO> creer(@RequestBody RendezVousDTO dto) {
        return ResponseEntity.ok(rendezVousService.creerRendezVous(dto));
    }

    /* =========================
       LECTURE
       ========================= */
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

    /* =========================
       FILTRES
       ========================= */

    @Operation(summary = "Rendez-vous par structure (nom)")
    @GetMapping("/structure")
    public List<RendezVousDTO> getByStructure(
            @RequestParam String nom
    ) {
        return rendezVousService.trouverParStructure(nom);
    }

    @Operation(summary = "Rendez-vous par médecin (ID)")
    @GetMapping("/medecin/{medecinId}")
    public List<RendezVousDTO> getByMedecin(@PathVariable String medecinId) {
        return rendezVousService.trouverParMedecinId(medecinId);
    }

    @Operation(summary = "Rendez-vous par agenda (ID)")
    @GetMapping("/agenda/{agendaId}")
    public List<RendezVousDTO> getByAgenda(@PathVariable String agendaId) {
        return rendezVousService.trouverParAgendaId(agendaId);
    }

    /* =========================
       SUPPRESSION
       ========================= */
    @Operation(summary = "Supprimer un rendez-vous")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable String id) {
        rendezVousService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    // 🔹 Modifier le statut d'un RDV unique
    @PutMapping("/{id}/statut")
    public ResponseEntity<RendezVousDTO> modifierStatut(
            @PathVariable("id") String rdvId,
            @RequestParam("actif") boolean actif
    ) {
        RendezVousDTO updated = rendezVousService.modifierStatut(rdvId, actif);
        return ResponseEntity.ok(updated);
    }

    // 🔹 Modifier le statut de tous les RDV d'une journée
    @PutMapping("/journee/{journeeId}/statut")
    public ResponseEntity<List<RendezVousDTO>> modifierStatutTousParJournee(
            @PathVariable String journeeId,
            @RequestParam("actif") boolean actif
    ) {
        List<RendezVousDTO> updatedList = rendezVousService.modifierStatutTousParJournee(journeeId, actif);
        return ResponseEntity.ok(updatedList);
    }

    // 🔹 Modifier le statut de tous les RDV d'un agenda
    @PutMapping("/agenda/{agendaId}/statut")
    public ResponseEntity<List<RendezVousDTO>> modifierStatutTousParAgenda(
            @PathVariable String agendaId,
            @RequestParam("actif") boolean actif
    ) {
        List<RendezVousDTO> updatedList = rendezVousService.modifierStatutTousParAgenda(agendaId, actif);
        return ResponseEntity.ok(updatedList);
    }
}
