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

    @Operation(summary = "Créer un médecin", description = "Enregistre un médecin avec photo (multipart) et données JSON")
    @PostMapping("/create")
    public ResponseEntity<MedecinDto> saveMedecin(@RequestParam("photo") MultipartFile photo,
                                                  @RequestParam("medecin") String medecinJson) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        MedecinDto dto = objectMapper.readValue(medecinJson, MedecinDto.class);
        MedecinDto savedMedecin = medecinService.save(dto, photo);
        return new ResponseEntity<>(savedMedecin, HttpStatus.CREATED);
    }

    @Operation(summary = "Mettre à jour un médecin")
    @PutMapping("/update/{id}")
    public ResponseEntity<MedecinDto> updateMedecin(@PathVariable String id,
                                                    @RequestBody MedecinDto medecinDto,
                                                    @RequestPart(value = "photo", required = false) MultipartFile photo) throws IOException {
        MedecinDto updatedMedecin = medecinService.update(id, medecinDto, photo);
        return new ResponseEntity<>(updatedMedecin, HttpStatus.OK);
    }

    @Operation(summary = "Activer un médecin via un code OTP")
    @PostMapping("/activation")
    public ResponseEntity<String> activation(@RequestBody Map<String, String> activation) {
        try {
            this.medecinService.activation(activation);
            return ResponseEntity.ok("Compte activé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Renvoyer un OTP expiré")
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody UtilisateurDto dto) {
        Medecin medecin = medecinRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        validationService.renvoyerCodeMedecin(medecin);
        return ResponseEntity.ok("Nouveau code envoyé");
    }

    @Operation(summary = "Connexion d'un médecin", description = "Retourne un token JWT si les identifiants sont valides")
    @PostMapping("/connexion")
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
                    userDetails.getRole()
            );
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email ou mot de passe incorrect");
        }
    }

    @Operation(summary = "Lister tous les médecins")
    @GetMapping("/all")
    public ResponseEntity<List<MedecinDto>> getAllMedecins() {
        List<MedecinDto> medecins = medecinService.findAll();
        return new ResponseEntity<>(medecins, HttpStatus.OK);
    }

    @Operation(summary = "Supprimer un médecin par ID")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteMedecin(@PathVariable String id) {
        medecinService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Changer le statut actif d’un médecin")
    @PutMapping("/update-status/{id}")
    public ResponseEntity<MedecinDto> updateStatus(@PathVariable String id, @RequestParam boolean actif) {
        MedecinDto updatedMedecin = medecinService.updateStatus(id, actif);
        return new ResponseEntity<>(updatedMedecin, HttpStatus.OK);
    }

    @Operation(summary = "Compter le nombre total de médecins")
    @GetMapping("/count")
    public ResponseEntity<Long> countAllMedecins() {
        long count = medecinService.countAll();
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @Operation(summary = "Rechercher un médecin par email")
    @GetMapping("/email/{email}")
    public ResponseEntity<MedecinDto> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(medecinService.findByEmail(email));
    }

    @Operation(summary = "Supprimer un médecin par email")
    @DeleteMapping("/email/{email}")
    public ResponseEntity<Void> deleteByEmail(@PathVariable String email) {
        medecinService.deleteByEmail(email);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtenir la photo du médecin")
    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> getPhoto(@PathVariable String id) throws IOException {
        byte[] image = medecinService.getPhoto(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(image);
    }

    @Operation(summary = "Rechercher les médecins par spécialité")
    @GetMapping("/specialite/{specialite}")
    public ResponseEntity<List<MedecinDto>> getBySpecialite(@PathVariable String specialite) {
        return ResponseEntity.ok(medecinService.searchBySpeciality(specialite));
    }

    @Operation(summary = "Liste des médecins actifs")
    @GetMapping("/actifs")
    public ResponseEntity<List<MedecinDto>> getActiveMedecins() {
        return ResponseEntity.ok(medecinService.getActiveMedecins());
    }

    @PostMapping("/medecin/motdepasse/reset")
    @Operation(summary = "Modification du mot de passe")
    public ResponseEntity<String> resetMotDePasse(@RequestBody ChangementMotDePasseDto dto) {
        medecinService.updatePasswordByEmail(dto);
        return ResponseEntity.ok("Mot de passe mis à jour avec succès.");
    }
}
