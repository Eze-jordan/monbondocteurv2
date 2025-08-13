package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.model.Utilisateur;
import com.esiitech.monbondocteurv2.model.Validation;
import com.esiitech.monbondocteurv2.service.ValidationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/validations")
public class ValidationController {

    private final ValidationService validationService;

    @Autowired
    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    // ✅ Supprimer une validation par ID
    @DeleteMapping("/{id}")
    public void supprimerParId(@PathVariable String id) {
        validationService.supprimerParId(id);
    }

    // ✅ Supprimer une validation par utilisateur
    @DeleteMapping("/utilisateur")
    public void supprimerParUtilisateur(@RequestBody Utilisateur utilisateur) {
        validationService.supprimerParUtilisateur(utilisateur);
    }

    // ✅ Supprimer une validation par médecin
    @DeleteMapping("/medecin")
    public void supprimerParMedecin(@RequestBody Medecin medecin) {
        validationService.supprimerParMedecin(medecin);
    }

    // ✅ Supprimer une validation par structure sanitaire
    @DeleteMapping("/structure")
    public void supprimerParStructure(@RequestBody StructureSanitaire structure) {
        validationService.supprimerParStructure(structure);
    }
    // ✅ Récupérer toutes les validations
    @GetMapping
    public List<Validation> getAllValidations() {
        return validationService.getAllValidations();
    }

    @Operation(tags = "Validations", summary = "Supprimer une validation par ID utilisateur")
    @DeleteMapping("/utilisateur/{id}")
    public ResponseEntity<Void> supprimerParUtilisateur(@PathVariable String id) {
        validationService.supprimerParUtilisateurId(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(tags = "Validations", summary = "Supprimer une validation par ID médecin")
    @DeleteMapping("/medecin/{id}")
    public ResponseEntity<Void> supprimerParMedecin(@PathVariable String id) {
        validationService.supprimerParMedecinId(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(tags = "Validations", summary = "Supprimer une validation par ID structure")
    @DeleteMapping("/structure/{id}")
    public ResponseEntity<Void> supprimerParStructure(@PathVariable String id) {
        validationService.supprimerParStructureId(id);
        return ResponseEntity.noContent().build();
    }
    @Operation(tags = "Validations",
            summary = "Lister les validations",
            description = "Sans paramètre : toutes les validations. Avec ?type=utilisateur|medecin|structure : filtre par type.")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Validation>> getAllValidations(
            @RequestParam(name = "type", required = false) String type) {
        List<Validation> result = validationService.getAllValidationsByType(type);
        return ResponseEntity.ok(result);
    }

    @Operation(tags = "Validations",
            summary = "Lister les validations groupées par type",
            description = "Retourne un objet avec trois listes: { utilisateurs:[], medecins:[], structures:[] }")
    @GetMapping(value = "/grouped", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<Validation>>> getAllValidationsGrouped() {
        return ResponseEntity.ok(validationService.getAllValidationsGrouped());
    }

}
