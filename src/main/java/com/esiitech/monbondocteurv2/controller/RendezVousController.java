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
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "Créer un nouveau rendez-vous")
    @PostMapping
    public ResponseEntity<RendezVousDTO> creerRendezVous(@RequestBody RendezVousDTO dto) {
        RendezVousDTO nouveauRdv = rendezVousService.creerRendezVous(dto);
        return ResponseEntity.ok(nouveauRdv);
    }

    @Operation(summary = "Lister tous les rendez-vous")
    @GetMapping
    public ResponseEntity<List<RendezVousDTO>> listerTousLesRendezVous() {
        return ResponseEntity.ok(rendezVousService.listerTous());
    }

    @Operation(summary = "Obtenir un rendez-vous par son ID")
    @GetMapping("/{id}")
    public ResponseEntity<RendezVousDTO> trouverRendezVousParId(@PathVariable String id) {
        return rendezVousService.trouverParId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Supprimer un rendez-vous par ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerRendezVous(@PathVariable String id) {
        rendezVousService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lister les rendez-vous d'une structure sanitaire par son ID")
    @GetMapping("/structure/{id}")
    public ResponseEntity<List<RendezVousDTO>> getByStructure(@PathVariable String id) {
        StructureSanitaire structure = new StructureSanitaire();
        structure.setId(id);
        return ResponseEntity.ok(rendezVousService.trouverParStructureSanitaire(structure));
    }

    @Operation(summary = "Lister les rendez-vous d'un médecin par son ID")
    @GetMapping("/medecin/{id}")
    public ResponseEntity<List<RendezVousDTO>> getByMedecin(@PathVariable String id) {
        Medecin medecin = new Medecin();
        medecin.setId(id);
        return ResponseEntity.ok(rendezVousService.trouverParMedecin(medecin));
    }

    @Operation(summary = "Rechercher les rendez-vous par nom de structure sanitaire")
    @GetMapping("/structure")
    public ResponseEntity<List<RendezVousDTO>> getByNomStructure(@RequestParam String nom) {
        return ResponseEntity.ok(rendezVousService.trouverParNomStructure(nom));
    }

    @Operation(summary = "Rechercher les rendez-vous par nom de médecin")
    @GetMapping("/medecin")
    public ResponseEntity<List<RendezVousDTO>> getByNomMedecin(@RequestParam String nom) {
        return ResponseEntity.ok(rendezVousService.trouverParNomMedecin(nom));
    }
}
