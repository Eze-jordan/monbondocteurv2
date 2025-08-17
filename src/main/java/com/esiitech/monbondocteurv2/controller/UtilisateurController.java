package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.ChangementMotDePasseDto;
import com.esiitech.monbondocteurv2.dto.LoginRequest;
import com.esiitech.monbondocteurv2.dto.UtilisateurDto;
import com.esiitech.monbondocteurv2.exception.ResourceNotFoundException;
import com.esiitech.monbondocteurv2.model.Utilisateur;
import com.esiitech.monbondocteurv2.repository.UtilisateurRepository;
import com.esiitech.monbondocteurv2.securite.CustomUserDetails;
import com.esiitech.monbondocteurv2.securite.JwtService;
import com.esiitech.monbondocteurv2.service.CustomUserDetailsService;
import com.esiitech.monbondocteurv2.service.UtilisateurService;
import com.esiitech.monbondocteurv2.service.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/V2/users")
@Tag(name = "Utilisateur", description = "Gestion des utilisateurs (inscription, connexion, activation, suppression, etc.)")
public class UtilisateurController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private JwtService jwtService;

    private final UtilisateurRepository utilisateurRepository;
    private final ValidationService validationService;
    private final CustomUserDetailsService customUserDetailsService;

    public UtilisateurController(UtilisateurRepository utilisateurRepository, ValidationService validationService, CustomUserDetailsService customUserDetailsService) {
        this.utilisateurRepository = utilisateurRepository;
        this.validationService = validationService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Operation(
            summary = "Créer un utilisateur",
            description = "Permet de créer un nouvel utilisateur avec une photo optionnelle (upload multipart/form-data)."
    )
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UtilisateurDto> createUtilisateur(
            @Parameter(description = "Photo de l'utilisateur (optionnelle)") @RequestParam(value = "photo", required = false) MultipartFile photo,
            @Parameter(description = "Données utilisateur au format JSON") @RequestParam("utilisateur") String utilisateurJson
    ) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        UtilisateurDto dto = objectMapper.readValue(utilisateurJson, UtilisateurDto.class);

        if (photo == null || photo.isEmpty()) {
            dto.setPhotoPath("/uploads/utilisateurs/default.jpg");
        }

        UtilisateurDto savedUtilisateur = utilisateurService.save(dto, photo);
        return new ResponseEntity<>(savedUtilisateur, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Renvoyer un OTP",
            description = "Renvoie un nouveau code OTP si l'ancien est expiré."
    )
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody UtilisateurDto dto) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        validationService.renvoyerCode(utilisateur);
        return ResponseEntity.ok("Nouveau code envoyé");
    }

    @Operation(
            summary = "Activer un compte",
            description = "Valide le code reçu par mail pour activer le compte."
    )
    @PostMapping("/activation")
    public ResponseEntity<String> activation(@RequestBody Map<String, String> activation) {
        try {
            this.utilisateurService.activation(activation);
            return ResponseEntity.ok("Compte activé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(
            summary = "Connexion",
            description = "Permet de se connecter avec email et mot de passe. Retourne un JWT en cas de succès."
    )
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
            summary = "Récupérer un utilisateur",
            description = "Récupère les informations d’un utilisateur par son ID."
    )
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UtilisateurDto> getUtilisateur(
            @Parameter(description = "ID de l'utilisateur") @PathVariable String id
    ) {
        UtilisateurDto utilisateurDto = utilisateurService.findById(id);

        if (utilisateurDto == null) {
            throw new ResourceNotFoundException("Utilisateur", "id", id, 404);
        }

        return new ResponseEntity<>(utilisateurDto, HttpStatus.OK);
    }

    @Operation(
            summary = "Mettre à jour un utilisateur",
            description = "Met à jour les informations d’un utilisateur."
    )
    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UtilisateurDto> updateUtilisateur(
            @Parameter(description = "ID de l'utilisateur") @PathVariable String id,
            @RequestBody UtilisateurDto dto
    ) {
        UtilisateurDto updatedUtilisateur = utilisateurService.update(id, dto);
        return new ResponseEntity<>(updatedUtilisateur, HttpStatus.OK);
    }

    @Operation(
            summary = "Supprimer un utilisateur",
            description = "Supprime un utilisateur par son email."
    )
    @DeleteMapping("/delete/{email}")
    public ResponseEntity<Void> deleteUtilisateur(
            @Parameter(description = "Email de l'utilisateur") @PathVariable String email
    ) {
        utilisateurService.deleteByEmail(email);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Lister tous les utilisateurs",
            description = "Retourne la liste de tous les utilisateurs enregistrés."
    )
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Iterable<UtilisateurDto>> getAllUtilisateurs() {
        Iterable<UtilisateurDto> utilisateurs = utilisateurService.getAllUsers();
        return new ResponseEntity<>(utilisateurs, HttpStatus.OK);
    }

    @Operation(
            summary = "Modification du mot de passe",
            description = "Modifie le mot de passe d’un utilisateur en vérifiant l’email et la confirmation du nouveau mot de passe."
    )
    @PostMapping(value = "/utilisateurs/motdepasse/reset", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> resetMotDePasse(@RequestBody ChangementMotDePasseDto dto) {
        utilisateurService.updatePasswordByEmail(dto);
        return ResponseEntity.ok("Mot de passe mis à jour avec succès.");
    }
}
