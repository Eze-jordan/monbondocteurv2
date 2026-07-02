package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.MedecinDto;
import com.esiitech.monbondocteurv2.dto.StructureSanitaireDto;
import com.esiitech.monbondocteurv2.service.MedecinStructureSanitaireService;
import com.esiitech.monbondocteurv2.service.StructureSanitaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/V2/public/structures")
@RequiredArgsConstructor
public class StructurePublicController {

    private final StructureSanitaireService structureSanitaireService;
    private final MedecinStructureSanitaireService liaisonService;

    /**
     * GET /api/V2/public/structures?ville=Paris&specialite=Chirurgie
     */
    @GetMapping
    public ResponseEntity<List<StructureSanitaireDto>> searchStructures(
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) String specialite) {

        List<StructureSanitaireDto> resultats;

        if (ville != null && !ville.isBlank() && specialite != null && !specialite.isBlank()) {
            resultats = structureSanitaireService.findByVilleAndSpecialite(ville, specialite);
        } else if (ville != null && !ville.isBlank()) {
            resultats = structureSanitaireService.findByVille(ville);
        } else if (specialite != null && !specialite.isBlank()) {
            resultats = structureSanitaireService.findBySpecialite(specialite);
        } else {
            resultats = structureSanitaireService.findAll();
        }

        return ResponseEntity.ok(resultats);
    }

    /**
     * GET /api/V2/public/structures/search?nom=amissa&limit=5
     * Recherche d'établissements par nom pour les suggestions en live
     */
    @GetMapping("/search")
    public ResponseEntity<List<StructureSanitaireDto>> searchStructuresByNom(
            @RequestParam(required = false) String nom,
            @RequestParam(defaultValue = "10") int limit) {

        List<StructureSanitaireDto> resultats;

        if (nom != null && !nom.isBlank()) {
            resultats = structureSanitaireService.searchByNom(nom);
        } else {
            resultats = structureSanitaireService.findAll();
        }

        if (resultats.size() > limit) {
            resultats = resultats.subList(0, limit);
        }

        return ResponseEntity.ok(resultats);
    }

    /**
     * GET /api/V2/public/structures/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<StructureSanitaireDto> getStructure(@PathVariable String id) {
        return ResponseEntity.ok(structureSanitaireService.findById(id));
    }

    /**
     * GET /api/V2/public/structures/{structureId}/medecins?specialite=Chirurgie
     */
    @GetMapping("/{structureId}/medecins")
    public ResponseEntity<List<MedecinDto>> getMedecins(
            @PathVariable String structureId,
            @RequestParam(required = false) String specialite) {
        if (specialite != null && !specialite.isBlank()) {
            return ResponseEntity.ok(liaisonService.getMedecinsByStructureAndSpecialite(structureId, specialite));
        }
        return ResponseEntity.ok(liaisonService.getAllMedecinsByStructure(structureId));
    }

    /**
     * GET /api/V2/public/structures/{id}/specialites
     */
    @GetMapping("/{id}/specialites")
    public ResponseEntity<Set<String>> getSpecialites(@PathVariable String id) {
        return ResponseEntity.ok(structureSanitaireService.getSpecialitesStructure(id));
    }
}