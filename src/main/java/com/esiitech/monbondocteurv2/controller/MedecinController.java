package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.*;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.repository.MedecinRepository;
import com.esiitech.monbondocteurv2.securite.CustomUserDetails;
import com.esiitech.monbondocteurv2.securite.JwtService;
import com.esiitech.monbondocteurv2.service.CustomUserDetailsService;
import com.esiitech.monbondocteurv2.service.MedecinService;
import com.esiitech.monbondocteurv2.service.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/V2/medecins")
@Tag(name = "Médecins", description = "Endpoints liés à la gestion des médecins")
public class MedecinController {

    private final MedecinService medecinService;
    private final ValidationService validationService;
    private final MedecinRepository medecinRepository;
    private final CustomUserDetailsService customUserDetailsService;

    @Autowired private JwtService jwtService;
    @Autowired private AuthenticationManager authenticationManager;

    public MedecinController(MedecinService medecinService, ValidationService validationService, MedecinRepository medecinRepository, CustomUserDetailsService customUserDetailsService) {
        this.medecinService = medecinService;
        this.validationService = validationService;
        this.medecinRepository = medecinRepository;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Operation(
            tags = "Médecins",
            summary = "Créer un médecin",
            description = "Enregistre un médecin avec photo (multipart) et données JSON."
    )
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MedecinDto> saveMedecin(
            @Parameter(description = "Photo du médecin") @RequestParam("photo") MultipartFile photo,
            @Parameter(description = "Données du médecin au format JSON") @RequestParam("medecin") String medecinJson
    ) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        MedecinDto dto = objectMapper.readValue(medecinJson, MedecinDto.class);
        MedecinDto savedMedecin = medecinService.save(dto, photo);
        return new ResponseEntity<>(savedMedecin, HttpStatus.CREATED);
    }

    @Operation(
            tags = "Médecins",
            summary = "Mettre à jour un médecin",
            description = "Met à jour les informations d’un médecin (JSON) et éventuellement sa photo."
    )
    @PutMapping(value = "/update/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MedecinDto> updateMedecin(
            @Parameter(description = "ID du médecin") @PathVariable String id,
            @RequestBody MedecinDto medecinDto,
            @Parameter(description = "Nouvelle photo (optionnelle)") @RequestPart(value = "photo", required = false) MultipartFile photo
    ) throws IOException {
        MedecinDto updatedMedecin = medecinService.update(id, medecinDto, photo);
        return new ResponseEntity<>(updatedMedecin, HttpStatus.OK);
    }

    @Operation(
            tags = "Médecins",
            summary = "Activer un médecin via un code OTP",
            description = "Active le compte d’un médecin grâce à un code OTP."
    )
    @PostMapping("/activation")
    public ResponseEntity<String> activation(@RequestBody Map<String, String> activation) {
        try {
            this.medecinService.activation(activation);
            return ResponseEntity.ok("Compte activé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(
            tags = "Médecins",
            summary = "Renvoyer un OTP expiré",
            description = "Renvoie un nouveau code OTP au médecin si l’ancien a expiré."
    )
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody UtilisateurDto dto) {
        Medecin medecin = medecinRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        validationService.renvoyerCodeMedecin(medecin);
        return ResponseEntity.ok("Nouveau code envoyé");
    }

    @Operation(
            tags = "Médecins",
            summary = "Connexion d'un médecin",
            description = "Authentifie un médecin et retourne un token JWT si les identifiants sont valides."
    )
    @PostMapping(value = "/connexion", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> connexion(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse())
            );
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(
                    userDetails,
                    userDetails.getNom(),
                    userDetails.getUsername(),
                    userDetails.getRole(),
                    false // medecin ⇒ pas d’abonnement

            );
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email ou mot de passe incorrect");
        }
    }

    @Operation(
            tags = "Médecins",
            summary = "Lister tous les médecins",
            description = "Retourne la liste de tous les médecins."
    )
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MedecinDto>> getAllMedecins() {
        List<MedecinDto> medecins = medecinService.findAll();
        return new ResponseEntity<>(medecins, HttpStatus.OK);
    }

    @Operation(
            tags = "Médecins",
            summary = "Supprimer un médecin par ID",
            description = "Supprime un médecin par son identifiant."
    )
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteMedecin(@Parameter(description = "ID du médecin") @PathVariable String id) {
        medecinService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            tags = "Médecins",
            summary = "Changer le statut actif d’un médecin",
            description = "Active/Désactive un médecin (champ actif)."
    )
    @PutMapping(value = "/update-status/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MedecinDto> updateStatus(
            @Parameter(description = "ID du médecin") @PathVariable String id,
            @Parameter(description = "Nouveau statut actif") @RequestParam boolean actif
    ) {
        MedecinDto updatedMedecin = medecinService.updateStatus(id, actif);
        return new ResponseEntity<>(updatedMedecin, HttpStatus.OK);
    }

    @Operation(
            tags = "Médecins",
            summary = "Compter le nombre total de médecins",
            description = "Retourne le nombre total d’enregistrements de médecins."
    )
    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> countAllMedecins() {
        long count = medecinService.countAll();
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @Operation(
            tags = "Médecins",
            summary = "Rechercher un médecin par email",
            description = "Retourne les informations d’un médecin à partir de son email."
    )
    @GetMapping(value = "/email/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MedecinDto> getByEmail(@Parameter(description = "Email du médecin") @PathVariable String email) {
        return ResponseEntity.ok(medecinService.findByEmail(email));
    }

    @Operation(
            tags = "Médecins",
            summary = "Supprimer un médecin par email",
            description = "Supprime un médecin à partir de son email."
    )
    @DeleteMapping("/email/{email}")
    public ResponseEntity<Void> deleteByEmail(@Parameter(description = "Email du médecin") @PathVariable String email) {
        medecinService.deleteByEmail(email);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            tags = "Médecins",
            summary = "Obtenir la photo du médecin",
            description = "Retourne l’image de profil du médecin."
    )
    @GetMapping(value = "/{id}/photo", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getPhoto(@Parameter(description = "ID du médecin") @PathVariable String id) throws IOException {
        byte[] image = medecinService.getPhoto(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(image);
    }

    @Operation(
            tags = "Médecins",
            summary = "Rechercher les médecins par spécialité",
            description = "Recherche insensible à la casse; peut être partielle selon l’implémentation."
    )
    @GetMapping(value = "/specialite/{specialite}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MedecinDto>> getBySpecialite(@Parameter(description = "Nom de la spécialité") @PathVariable String specialite) {
        return ResponseEntity.ok(medecinService.searchBySpeciality(specialite));
    }

    @Operation(
            tags = "Médecins",
            summary = "Liste des médecins actifs",
            description = "Retourne uniquement les médecins dont le compte est actif."
    )
    @GetMapping(value = "/actifs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MedecinDto>> getActiveMedecins() {
        return ResponseEntity.ok(medecinService.getActiveMedecins());
    }

    @Operation(
            tags = "Médecins",
            summary = "Modification du mot de passe",
            description = "Modifie le mot de passe d’un médecin après vérification des informations."
    )
    @PostMapping(value = "/medecin/motdepasse/reset", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> resetMotDePasse(@RequestBody ChangementMotDePasseDto dto) {
        medecinService.updatePasswordByEmail(dto);
        return ResponseEntity.ok("Mot de passe mis à jour avec succès.");
    }
}
