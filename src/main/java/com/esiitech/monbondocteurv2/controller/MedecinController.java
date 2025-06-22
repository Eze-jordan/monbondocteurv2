package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.MedecinDto;
import com.esiitech.monbondocteurv2.service.MedecinService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/medecins")
@CrossOrigin(origins = "*")
public class MedecinController {

    private final MedecinService medecinService;

    public MedecinController(MedecinService medecinService) {
        this.medecinService = medecinService;
    }

    /**
     * Enregistrer un médecin avec une photo envoyée en multipart et des données en JSON.
     */
    @PostMapping("/create")
    public ResponseEntity<MedecinDto> saveMedecin(@RequestParam("photo") MultipartFile photo,
                                                  @RequestParam("medecin") String medecinJson) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        MedecinDto dto = objectMapper.readValue(medecinJson, MedecinDto.class);
        MedecinDto savedMedecin = medecinService.save(dto, photo);
        return new ResponseEntity<>(savedMedecin, HttpStatus.CREATED);
    }

    /**
     * Mettre à jour un médecin avec des données JSON et une photo.
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<MedecinDto> updateMedecin(@PathVariable Long id,
                                                    @RequestBody MedecinDto medecinDto,
                                                    @RequestPart(value = "photo", required = false) MultipartFile photo) throws IOException {
        MedecinDto updatedMedecin = medecinService.update(id, medecinDto, photo);
        return new ResponseEntity<>(updatedMedecin, HttpStatus.OK);
    }

    /**
     * Récupérer tous les médecins.
     */
    @GetMapping("/all")
    public ResponseEntity<List<MedecinDto>> getAllMedecins() {
        List<MedecinDto> medecins = medecinService.findAll();
        return new ResponseEntity<>(medecins, HttpStatus.OK);
    }

    /**
     * Supprimer un médecin par son ID.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteMedecin(@PathVariable Long id) {
        medecinService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Mettre à jour le statut actif/inactif d'un médecin.
     */
    @PutMapping("/update-status/{id}")
    public ResponseEntity<MedecinDto> updateStatus(@PathVariable Long id, @RequestParam boolean actif) {
        MedecinDto updatedMedecin = medecinService.updateStatus(id, actif);
        return new ResponseEntity<>(updatedMedecin, HttpStatus.OK);
    }

    /**
     * Compter le nombre total de médecins.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countAllMedecins() {
        long count = medecinService.countAll();
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    // ✅ 🔽 Ajout des nouveaux endpoints demandés 🔽

    // 1. 🔍 Rechercher un médecin par email
    @GetMapping("/email/{email}")
    public ResponseEntity<MedecinDto> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(medecinService.findByEmail(email));
    }

    // 2. 🗑️ Supprimer un médecin par email
    @DeleteMapping("/email/{email}")
    public ResponseEntity<Void> deleteByEmail(@PathVariable String email) {
        medecinService.deleteByEmail(email);
        return ResponseEntity.noContent().build();
    }

    // 3. 🖼️ Récupérer la photo du médecin
    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) throws IOException {
        byte[] image = medecinService.getPhoto(id);
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG) // ou IMAGE_PNG selon le cas
                .body(image);
    }

    // 4. 🩺 Rechercher les médecins par spécialité
    @GetMapping("/specialite/{specialite}")
    public ResponseEntity<List<MedecinDto>> getBySpecialite(@PathVariable String specialite) {
        return ResponseEntity.ok(medecinService.searchBySpeciality(specialite));
    }

    // 5. ✅ Obtenir la liste des médecins actifs
    @GetMapping("/actifs")
    public ResponseEntity<List<MedecinDto>> getActiveMedecins() {
        return ResponseEntity.ok(medecinService.getActiveMedecins());
    }
}
