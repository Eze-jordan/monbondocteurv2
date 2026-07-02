package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.MedecinDto;
import com.esiitech.monbondocteurv2.dto.StructureSanitaireDto;
import com.esiitech.monbondocteurv2.service.CreneauService;
import com.esiitech.monbondocteurv2.service.MedecinService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/V2/public/medecins")
@RequiredArgsConstructor
public class MedecinPublicController {

    private final MedecinService medecinService;
    private final CreneauService creneauService;

    /**
     * GET /api/V2/public/medecins/search?nom=laurel&specialite=Chirurgie&limit=5
     */
    @GetMapping("/search")
    public ResponseEntity<List<MedecinDto>> searchMedecins(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String specialite,
            @RequestParam(defaultValue = "10") int limit) {

        List<MedecinDto> resultats;

        // ✅ Recherche par nom (priorité)
        if (nom != null && !nom.isBlank()) {
            resultats = medecinService.searchByNomOuPrenom(nom);
        }
        // Sinon recherche par spécialité
        else if (specialite != null && !specialite.isBlank()) {
            resultats = medecinService.searchBySpeciality(specialite);
        }
        // Sinon tous les médecins actifs
        else {
            resultats = medecinService.getActiveMedecins();
        }

        // Limiter le nombre de résultats
        if (resultats.size() > limit) {
            resultats = resultats.subList(0, limit);
        }

        return ResponseEntity.ok(resultats);
    }

    /**
     * GET /api/V2/public/medecins/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<MedecinDto> getMedecin(@PathVariable String id) {
        return ResponseEntity.ok(medecinService.findByIdDto(id));
    }

    /**
     * GET /api/V2/public/medecins/{id}/creneaux?date=2025-06-01
     */
    @GetMapping("/{id}/creneaux")
    public ResponseEntity<Map<String, Object>> getCreneaux(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(creneauService.getCreneauxJourMap(id, date));
    }

    /**
     * GET /api/V2/public/medecins/{id}/creneaux/semaine?date=2025-06-01
     */
    @GetMapping("/{id}/creneaux/semaine")
    public ResponseEntity<Map<String, Object>> getCreneauxSemaine(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(creneauService.getCreneauxSemaineMap(id, date));
    }

    @GetMapping("/{medecinId}/structures")
    public ResponseEntity<List<StructureSanitaireDto>> getStructuresByMedecin(
            @PathVariable String medecinId) {

        List<StructureSanitaireDto> structures = medecinService.getStructuresByMedecin(medecinId);
        return ResponseEntity.ok(structures);
    }
}