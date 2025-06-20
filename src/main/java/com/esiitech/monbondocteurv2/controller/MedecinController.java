package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.MedecinDto;
import com.esiitech.monbondocteurv2.model.RefGrade;
import com.esiitech.monbondocteurv2.model.RefSpecialite;
import com.esiitech.monbondocteurv2.service.MedecinService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/medecins")
public class MedecinController {

    @Autowired
    private MedecinService medecinService;

    /**
     * Enregistrer un médecin avec une photo envoyée en multipart et des données en JSON.
     */
    @PostMapping("/create")
    public ResponseEntity<MedecinDto> saveMedecin(@RequestParam("photo") MultipartFile photo,
                                                  @RequestParam("medecin") String medecinJson) throws IOException {

        // Convertir les données JSON du médecin en DTO
        ObjectMapper objectMapper = new ObjectMapper();
        MedecinDto dto = objectMapper.readValue(medecinJson, MedecinDto.class);

        // Enregistrer le médecin avec la photo
        MedecinDto savedMedecin = medecinService.save(dto, photo);

        return new ResponseEntity<>(savedMedecin, HttpStatus.CREATED);
    }

    /**
     * Mettre à jour un médecin avec des données JSON et une photo.
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<MedecinDto> updateMedecin(@PathVariable Long id,
                                                    @RequestPart("medecin") MedecinDto medecinDto,
                                                    @RequestPart(value = "photo", required = false) MultipartFile photo) throws IOException {
        // Appeler le service pour mettre à jour le médecin
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
}
