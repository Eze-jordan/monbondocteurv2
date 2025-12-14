package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.RendezVousDTO;
import com.esiitech.monbondocteurv2.mapper.RendezVousMapper;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.repository.MedecinRepository;
import com.esiitech.monbondocteurv2.repository.RendezVousRepository;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import com.esiitech.monbondocteurv2.service.RendezVousService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/V2/rendezvous")
@Tag(name = "Rendez-vous", description = "Gestion des rendez-vous médicaux")
public class RendezVousController {

    private final RendezVousService rendezVousService;

    public RendezVousController(
            RendezVousService rendezVousService,
            MedecinRepository medecinRepository,
            StructureSanitaireRepository structureSanitaireRepository,
            RendezVousRepository rendezVousRepository,
            RendezVousMapper rendezVousMapper
    ) {
        this.rendezVousService = rendezVousService;
    }

    @Operation(
            tags = "Rendez-vous",
            summary = "Créer un nouveau rendez-vous",
            description = "Crée un rendez-vous à partir des informations fournies."
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RendezVousDTO> creerRendezVous(@RequestBody RendezVousDTO dto) {
        RendezVousDTO nouveauRdv = rendezVousService.creerRendezVous(dto);
        return ResponseEntity.ok(nouveauRdv);
    }

    @Operation(
            tags = "Rendez-vous",
            summary = "Lister tous les rendez-vous",
            description = "Retourne la liste complète des rendez-vous."
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RendezVousDTO>> listerTousLesRendezVous() {
        return ResponseEntity.ok(rendezVousService.listerTous());
    }

    @Operation(
            tags = "Rendez-vous",
            summary = "Obtenir un rendez-vous par son ID",
            description = "Retourne un rendez-vous correspondant à l'identifiant fourni."
    )
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RendezVousDTO> trouverRendezVousParId(
            @Parameter(description = "ID du rendez-vous") @PathVariable String id) {
        return rendezVousService.trouverParId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            tags = "Rendez-vous",
            summary = "Supprimer un rendez-vous par ID",
            description = "Supprime un rendez-vous à partir de son identifiant."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerRendezVous(
            @Parameter(description = "ID du rendez-vous") @PathVariable String id) {
        rendezVousService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            tags = "Rendez-vous",
            summary = "Lister les rendez-vous d'une structure sanitaire par son ID",
            description = "Retourne les rendez-vous rattachés à une structure donnée."
    )
    @GetMapping(value = "/structure/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RendezVousDTO>> getByStructure(
            @Parameter(description = "ID de la structure sanitaire") @PathVariable String id) {
        StructureSanitaire structure = new StructureSanitaire();
        structure.setId(id);
        return ResponseEntity.ok(rendezVousService.trouverParStructureSanitaire(structure));
    }

    @Operation(
            tags = "Rendez-vous",
            summary = "Lister les rendez-vous d'un médecin par son ID",
            description = "Retourne les rendez-vous rattachés à un médecin donné."
    )
    @GetMapping(value = "/medecin/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RendezVousDTO>> getByMedecin(
            @Parameter(description = "ID du médecin") @PathVariable String id) {
        Medecin medecin = new Medecin();
        medecin.setId(id);
        return ResponseEntity.ok(rendezVousService.trouverParMedecin(medecin));
    }

    @Operation(
            tags = "Rendez-vous",
            summary = "Rechercher les rendez-vous par nom de structure sanitaire",
            description = "Recherche par nom (insensible à la casse selon l'implémentation)."
    )
    @GetMapping(value = "/structure", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RendezVousDTO>> getByNomStructure(
            @Parameter(description = "Nom de la structure sanitaire") @RequestParam String nom) {
        return ResponseEntity.ok(rendezVousService.trouverParNomStructure(nom));
    }

    @Operation(
            tags = "Rendez-vous",
            summary = "Rechercher les rendez-vous par nom de médecin",
            description = "Recherche par nom (insensible à la casse selon l'implémentation)."
    )
    @GetMapping(value = "/medecin", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RendezVousDTO>> getByNomMedecin(
            @Parameter(description = "Nom du médecin") @RequestParam String nom) {
        return ResponseEntity.ok(rendezVousService.trouverParNomMedecin(nom));
    }
    @GetMapping("/date/{date}")
    @Operation(
            tags = "Rendez-vous",
            summary = "Rechercher les rendez-vous par date",
            description = "Recherche par date (insensible à la casse selon l'implémentation)."
    )
    public List<RendezVousDTO> getRendezVousParDate(@PathVariable LocalDate date) {
        return rendezVousService.trouverParDate(date);
    }
    @GetMapping("/medecin/{medecinId}")
    @Operation(
            tags = "Rendez-vous",
            summary = "Rechercher les rendez-vous par Id du médecin ",
            description = "Recherche par  Id du médecin (insensible à la casse selon l'implémentation)."
    )
    public List<RendezVousDTO> getRendezVousParMedecin(@PathVariable String medecinId) {
        return rendezVousService.trouverParMedecinId(medecinId);
    }

    @GetMapping("/agenda/{agendaId}")
    @Operation(
            tags = "Rendez-vous",
            summary = "Rechercher les rendez-vous par Id de l'agenda  ",
            description = "Recherche par Id de l'agenda (insensible à la casse selon l'implémentation)."
    )
    public List<RendezVousDTO> getRendezVousParAgenda(@PathVariable String agendaId) {
        return rendezVousService.trouverParAgendaId(agendaId);
    }


}

