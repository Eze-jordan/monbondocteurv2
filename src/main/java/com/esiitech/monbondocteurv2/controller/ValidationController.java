package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.model.Validation;
import com.esiitech.monbondocteurv2.service.ValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin
@RestController
@RequestMapping("/api/validations")
@Tag(name = "Validations", description = "Gestion des codes de validation des utilisateurs")
public class ValidationController {

    private final ValidationService validationService;

    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @GetMapping("/{code}")
    @Operation(
            summary = "Récupérer une validation par son code",
            description = "Permet de récupérer un objet Validation en utilisant un code unique"
    )
    @ApiResponse(responseCode = "200", description = "Validation trouvée")
    @ApiResponse(responseCode = "404", description = "Validation non trouvée")
    public Validation getValidationByCode(@PathVariable String code) {
        return validationService.lireEnFonctionDuCode(code);
    }

    @GetMapping
    @Operation(
            summary = "Lister toutes les validations",
            description = "Retourne la liste complète de toutes les validations"
    )
    @ApiResponse(responseCode = "200", description = "Liste des validations")
    public List<Validation> getAllValidations() {
        return validationService.getValidationRipository().findAll();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Supprimer une validation par son ID",
            description = "Supprime une validation de la base de données en fonction de son ID"
    )
    @ApiResponse(responseCode = "204", description = "Validation supprimée")
    @ApiResponse(responseCode = "404", description = "Validation non trouvée")
    public void deleteValidationById(@PathVariable String id) {
        validationService.getValidationRipository().deleteById(id);
    }

}
