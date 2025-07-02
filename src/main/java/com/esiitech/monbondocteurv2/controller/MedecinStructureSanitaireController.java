package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.MedecinStructureSanitaireDto;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.service.MedecinService;
import com.esiitech.monbondocteurv2.service.MedecinStructureSanitaireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/liaisons")
@Tag(name = "Liaisons Médecin-Structure", description = "Endpoints pour gérer les associations entre les médecins et les structures sanitaires")
public class MedecinStructureSanitaireController {

    @Autowired
    private MedecinStructureSanitaireService medecinStructureSanitaireService;

    @Autowired
    private MedecinService medecinService;

    @Operation(summary = "Lister toutes les liaisons")
    @GetMapping
    public ResponseEntity<List<MedecinStructureSanitaireDto>> getAll() {
        return ResponseEntity.ok(medecinStructureSanitaireService.findAll());
    }

    @Operation(summary = "Obtenir une liaison par ID")
    @GetMapping("/{id}")
    public ResponseEntity<MedecinStructureSanitaireDto> getById(@PathVariable Long id) {
        return medecinStructureSanitaireService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Créer une nouvelle liaison médecin-structure")
    @PostMapping
    public ResponseEntity<MedecinStructureSanitaireDto> create(@RequestBody MedecinStructureSanitaireDto dto) {
        return ResponseEntity.ok(medecinStructureSanitaireService.save(dto));
    }

    @Operation(summary = "Mettre à jour une liaison existante")
    @PutMapping("/{id}")
    public ResponseEntity<MedecinStructureSanitaireDto> update(@PathVariable Long id, @RequestBody MedecinStructureSanitaireDto dto) {
        dto.setId(id);
        return ResponseEntity.ok(medecinStructureSanitaireService.save(dto));
    }

    @Operation(summary = "Supprimer une liaison par ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        medecinStructureSanitaireService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtenir la structure active d’un médecin")
    @GetMapping("/{medecinId}/structure-active")
    public StructureSanitaire getStructureActifByMedecin(@PathVariable Long medecinId) {
        Medecin medecin = medecinService.getById(medecinId);
        return medecinStructureSanitaireService.getStructureSanitaireActifByMedecin(medecin);
    }
}
