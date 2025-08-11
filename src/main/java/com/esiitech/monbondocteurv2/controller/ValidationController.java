package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.model.Utilisateur;
import com.esiitech.monbondocteurv2.model.Validation;
import com.esiitech.monbondocteurv2.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

}
