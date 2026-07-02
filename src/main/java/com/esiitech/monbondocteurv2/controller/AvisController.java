package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.AvisDto;
import com.esiitech.monbondocteurv2.dto.StatistiquesAvisDto;
import com.esiitech.monbondocteurv2.dto.TestimonialDto;
import com.esiitech.monbondocteurv2.service.AvisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/V2/avis")
@RequiredArgsConstructor
public class AvisController {

    private final AvisService avisService;

    /**
     * Créer un avis après un rendez-vous
     */
    @PostMapping
    public ResponseEntity<AvisDto> creerAvis(
            @RequestBody AvisDto dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(avisService.creerAvis(dto, authentication.getName()));
    }

    /**
     * Modifier un avis
     */
    @PutMapping("/{id}")
    public ResponseEntity<AvisDto> modifierAvis(
            @PathVariable String id,
            @RequestBody AvisDto dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(avisService.modifierAvis(id, dto, authentication.getName()));
    }

    /**
     * Supprimer un avis
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerAvis(
            @PathVariable String id,
            Authentication authentication
    ) {
        avisService.supprimerAvis(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupérer les avis et stats d'un médecin (toutes structures)
     */
    @GetMapping("/medecin/{medecinId}")
    public ResponseEntity<Map<String, Object>> getAvisMedecin(@PathVariable String medecinId) {
        return ResponseEntity.ok(avisService.getAvisEtStatsMedecin(medecinId));
    }

    /**
     * ✅ NOUVEAU : Récupérer les avis et stats d'un médecin pour une structure spécifique
     */
    @GetMapping("/medecin/{medecinId}/structure/{structureId}")
    public ResponseEntity<Map<String, Object>> getAvisMedecinParStructure(
            @PathVariable String medecinId,
            @PathVariable String structureId
    ) {
        return ResponseEntity.ok(avisService.getAvisEtStatsMedecinParStructure(medecinId, structureId));
    }

    /**
     * Récupérer les statistiques de notes pour une structure
     */
    @GetMapping("/structure/{structureId}/stats")
    public ResponseEntity<StatistiquesAvisDto> getStatsStructure(@PathVariable String structureId) {
        return ResponseEntity.ok(avisService.getStatistiquesStructure(structureId));
    }

    /**
     * Vérifier si un utilisateur peut noter un rendez-vous
     */
    @GetMapping("/peut-noter/{rendezVousId}")
    public ResponseEntity<Boolean> peutNoter(
            @PathVariable String rendezVousId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(avisService.peutNoter(rendezVousId, authentication.getName()));
    }

    /**
     * Note moyenne d'une spécialité dans une structure
     */
    @GetMapping("/structure/{structureId}/specialite/{specialite}/note")
    public ResponseEntity<Double> getNoteSpecialite(
            @PathVariable String structureId,
            @PathVariable String specialite
    ) {
        return ResponseEntity.ok(avisService.getNoteMoyenneSpecialite(structureId, specialite));
    }


    /**
     * Récupérer les derniers témoignages pour la page d'accueil
     * GET /api/V2/avis/testimonials?limit=3
     */
    @GetMapping("/testimonials")
    public ResponseEntity<List<TestimonialDto>> getTestimonials(
            @RequestParam(defaultValue = "3") int limit
    ) {
        return ResponseEntity.ok(avisService.getRecentTestimonials(limit));
    }
}