package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.ChangementMotDePasseDto;
import com.esiitech.monbondocteurv2.dto.LoginRequest;
import com.esiitech.monbondocteurv2.dto.StructureSanitaireDto;
import com.esiitech.monbondocteurv2.model.RefSpecialite;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.model.Ville;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import com.esiitech.monbondocteurv2.securite.CustomUserDetails;
import com.esiitech.monbondocteurv2.securite.JwtService;
import com.esiitech.monbondocteurv2.service.StructureSanitaireService;
import com.esiitech.monbondocteurv2.service.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/V2/structuresanitaires")
@Tag(name = "Structure Sanitaire", description = "Endpoints pour la gestion des structures sanitaires")
public class StructureSanitaireController {

    @Autowired
    private StructureSanitaireService structureSanitaireService;
    @Autowired
    private StructureSanitaireRepository sanitaireRepository;
    @Autowired
    private ValidationService validationService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/create")
    @Operation(summary = "Créer une structure sanitaire", description = "Créer une structure avec ses informations et une photo en multipart")
    public ResponseEntity<StructureSanitaireDto> createStructureSanitaire(
            @Parameter(description = "Photo de la structure") @RequestParam(value = "photo", required = false) MultipartFile photo,
            @Parameter(description = "Données de la structure au format JSON") @RequestParam("structureSanitaire") String structureSanitaireJson) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        StructureSanitaireDto dto = objectMapper.readValue(structureSanitaireJson, StructureSanitaireDto.class);
        StructureSanitaireDto savedStructureSanitaire = structureSanitaireService.save(dto, photo);

        return new ResponseEntity<>(savedStructureSanitaire, HttpStatus.CREATED);
    }

    @PostMapping("/activation")
    @Operation(summary = "Activer un compte", description = "Permet d'activer le compte avec un code OTP")
    public ResponseEntity<String> activation(@RequestBody Map<String, String> activation) {
        try {
            this.structureSanitaireService.activation(activation);
            return ResponseEntity.ok("Compte activé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Renvoyer un code OTP", description = "Renvoie un nouveau code si l'ancien a expiré")
    public ResponseEntity<?> resendOtp(@RequestBody StructureSanitaireDto dto) {
        StructureSanitaire structureSanitaire = sanitaireRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        validationService.renvoyerCodeStructure(structureSanitaire);
        return ResponseEntity.ok("Nouveau code envoyé");
    }

    @PostMapping("/connexion")
    @Operation(summary = "Connexion", description = "Authentifie un utilisateur et retourne un JWT")
    public ResponseEntity<?> connexion(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails, userDetails.getNom(), userDetails.getUsername(), userDetails.getRole());

            return ResponseEntity.ok(Collections.singletonMap("token", token));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email ou mot de passe incorrect");
        }
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Mettre à jour une structure sanitaire")
    public ResponseEntity<StructureSanitaireDto> updateStructureSanitaire(
            @Parameter(description = "ID de la structure") @PathVariable String id,
            @Parameter(description = "Nouvelle photo (optionnelle)") @RequestParam(value = "photo", required = false) MultipartFile photo,
            @Parameter(description = "Nouvelles données de la structure au format JSON") @RequestParam("structureSanitaire") String structureSanitaireJson) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        StructureSanitaireDto dto = objectMapper.readValue(structureSanitaireJson, StructureSanitaireDto.class);
        StructureSanitaireDto updatedStructureSanitaire = structureSanitaireService.update(id, dto, photo);

        return new ResponseEntity<>(updatedStructureSanitaire, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une structure sanitaire par ID")
    public ResponseEntity<StructureSanitaireDto> getStructureSanitaire(@PathVariable String id) {
        StructureSanitaireDto structureSanitaireDto = structureSanitaireService.findById(id);
        return new ResponseEntity<>(structureSanitaireDto, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Supprimer une structure sanitaire par ID")
    public ResponseEntity<Void> deleteStructureSanitaire(@PathVariable String id) {
        structureSanitaireService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/all")
    @Operation(summary = "Lister toutes les structures sanitaires")
    public ResponseEntity<List<StructureSanitaireDto>> getAllStructureSanitaire() {
        List<StructureSanitaireDto> structureSanitaireDtos = structureSanitaireService.findAll();
        return new ResponseEntity<>(structureSanitaireDtos, HttpStatus.OK);
    }



    /**
     * Récupère toutes les structures d'une ville donnée.
     */
    @GetMapping("/ville/{ville}")
    public ResponseEntity<List<StructureSanitaireDto>> getByVille(@PathVariable String ville) {
        return ResponseEntity.ok(structureSanitaireService.findByVille(ville));
    }


    @GetMapping("/specialite/{specialite}")
    public ResponseEntity<List<StructureSanitaireDto>> getBySpecialite(@PathVariable String specialite) {
        return ResponseEntity.ok(structureSanitaireService.findBySpecialite(specialite));
    }




    @GetMapping("/{id}/specialites")
    public ResponseEntity<String> getSpecialites(@PathVariable String id) {
        String specialites = structureSanitaireService.getSpecialitesStructure(id);
        return ResponseEntity.ok(specialites);
    }



    /**
     * Endpoint pour récupérer toutes les spécialités utilisées dans les structures sanitaires.
     * @return Set de noms de spécialités (sans doublons).
     */
    @GetMapping("/specialites")
    public ResponseEntity<Set<String>> getToutesLesSpecialitesUtilisees() {
        Set<String> specialites = structureSanitaireService.getToutesLesSpecialitesUtilisees();
        return ResponseEntity.ok(specialites);
    }

    @PostMapping("/structureSanitaire/motdepasse/reset")
    @Operation(summary = "Modification du mot de passe")
    public ResponseEntity<String> resetMotDePasse(@RequestBody ChangementMotDePasseDto dto) {
        structureSanitaireService.updatePasswordByEmail(dto);
        return ResponseEntity.ok("Mot de passe mis à jour avec succès.");
    }

}
