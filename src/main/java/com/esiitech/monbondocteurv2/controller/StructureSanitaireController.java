package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.StructureSanitaireDto;
import com.esiitech.monbondocteurv2.service.StructureSanitaireService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
@RestController
@RequestMapping("/api/structuresanitaires")
public class StructureSanitaireController {

    @Autowired
    private StructureSanitaireService structureSanitaireService;

    @PostMapping("/create")
    public ResponseEntity<StructureSanitaireDto> createStructureSanitaire(
            @RequestParam(value = "photo", required = false) MultipartFile photo,  // Photo envoyée en tant que fichier
            @RequestParam("structureSanitaire") String structureSanitaireJson) throws IOException {

        // Convertir le JSON en DTO StructureSanitaire
        ObjectMapper objectMapper = new ObjectMapper();
        StructureSanitaireDto dto = objectMapper.readValue(structureSanitaireJson, StructureSanitaireDto.class);

        // Appeler le service pour enregistrer la structure sanitaire avec la photo (ou la photo par défaut)
        StructureSanitaireDto savedStructureSanitaire = structureSanitaireService.save(dto, photo);

        return new ResponseEntity<>(savedStructureSanitaire, HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<StructureSanitaireDto> updateStructureSanitaire(
            @PathVariable Long id,
            @RequestParam(value = "photo", required = false) MultipartFile photo, // Photo envoyée en tant que fichier
            @RequestParam("structureSanitaire") String structureSanitaireJson) throws IOException {

        // Convertir le JSON en DTO StructureSanitaire
        ObjectMapper objectMapper = new ObjectMapper();
        StructureSanitaireDto dto = objectMapper.readValue(structureSanitaireJson, StructureSanitaireDto.class);

        // Appeler le service pour mettre à jour la structure sanitaire avec la photo (ou la photo par défaut)
        StructureSanitaireDto updatedStructureSanitaire = structureSanitaireService.update(id, dto, photo);

        return new ResponseEntity<>(updatedStructureSanitaire, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StructureSanitaireDto> getStructureSanitaire(@PathVariable Long id) {
        StructureSanitaireDto structureSanitaireDto = structureSanitaireService.findById(id);
        return new ResponseEntity<>(structureSanitaireDto, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteStructureSanitaire(@PathVariable Long id) {
        structureSanitaireService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/all")
    public ResponseEntity<List<StructureSanitaireDto>> getAllStructureSanitaire() {
        List<StructureSanitaireDto> structureSanitaireDtos = structureSanitaireService.findAll();
        return new ResponseEntity<>(structureSanitaireDtos, HttpStatus.OK);
    }
}
