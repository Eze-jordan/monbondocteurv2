package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.MedecinDto;
import com.esiitech.monbondocteurv2.dto.MedecinStructureSanitaireDto;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.service.MedecinService;
import com.esiitech.monbondocteurv2.service.MedecinStructureSanitaireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/V2/liaisons")
@Tag(name = "Liaisons Médecin-Structure", description = "Endpoints pour gérer les associations entre les médecins et les structures sanitaires")
public class MedecinStructureSanitaireController {

    @Autowired
    private MedecinStructureSanitaireService medecinStructureSanitaireService;

    @Autowired
    private MedecinService medecinService;

    @Operation(summary = "Lister toutes les liaisons")
    @GetMapping("/all")
    public ResponseEntity<List<MedecinStructureSanitaireDto>> getAll() {
        return ResponseEntity.ok(medecinStructureSanitaireService.findAll());
    }

    @Operation(summary = "Obtenir une liaison par ID")
    @GetMapping("/{id}")
    public ResponseEntity<MedecinStructureSanitaireDto> getById(@PathVariable String id) {
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
    public ResponseEntity<MedecinStructureSanitaireDto> update(@PathVariable String id, @RequestBody MedecinStructureSanitaireDto dto) {
        dto.setId(id);
        return ResponseEntity.ok(medecinStructureSanitaireService.save(dto));
    }

    @Operation(summary = "Supprimer une liaison par ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        medecinStructureSanitaireService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtenir les structures actives d’un médecin")
    @GetMapping("/{medecinId}/structures-actives")
    public List<StructureSanitaire> getStructuresActivesByMedecin(
            @PathVariable String medecinId
    ) {
        Medecin medecin = medecinService.getById(medecinId);
        return medecinStructureSanitaireService
                .getStructuresSanitairesActivesByMedecin(medecin);
    }


    @GetMapping(value = "/{structureId}/medecins", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(tags = "Structures", summary = "Liste des médecins d'une structure", description = "Renvoie tous les médecins rattachés à la structure (sans pagination).")
    public ResponseEntity<List<MedecinDto>> getMedecinsByStructure(@PathVariable String structureId,
                                                                   @RequestParam(required = false, defaultValue = "false") boolean onlyActive) {
        List<MedecinDto> list = onlyActive ?
                medecinStructureSanitaireService.getActiveMedecinsByStructure(structureId) :
                medecinStructureSanitaireService.getAllMedecinsByStructure(structureId);
        return ResponseEntity.ok(list);
    }


}
