package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.ActivationRequest;
import com.esiitech.monbondocteurv2.dto.ChangementMotDePasseDto;
import com.esiitech.monbondocteurv2.dto.LoginRequest;
import com.esiitech.monbondocteurv2.dto.UtilisateurDto;
import com.esiitech.monbondocteurv2.model.Utilisateur;
import com.esiitech.monbondocteurv2.repository.UtilisateurRepository;
import com.esiitech.monbondocteurv2.securite.CustomUserDetails;
import com.esiitech.monbondocteurv2.securite.JwtService;
import com.esiitech.monbondocteurv2.service.UtilisateurService;
import com.esiitech.monbondocteurv2.service.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/V2/users")
public class UtilisateurController {

    private final AuthenticationManager authenticationManager;
    private final UtilisateurService utilisateurService;
    private final JwtService jwtService;
    private final UtilisateurRepository utilisateurRepository;
    private final ValidationService validationService;

    public UtilisateurController(
            AuthenticationManager authenticationManager,
            UtilisateurService utilisateurService,
            JwtService jwtService,
            UtilisateurRepository utilisateurRepository,
            ValidationService validationService
    ) {
        this.authenticationManager = authenticationManager;
        this.utilisateurService = utilisateurService;
        this.jwtService = jwtService;
        this.utilisateurRepository = utilisateurRepository;
        this.validationService = validationService;
    }

    // =====================================================
    // 1. CRÉATION UTILISATEUR
    // =====================================================

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UtilisateurDto> create(
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam("utilisateur") String utilisateurJson
    ) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        UtilisateurDto dto = objectMapper.readValue(utilisateurJson, UtilisateurDto.class);

        if (photo == null || photo.isEmpty()) {
            dto.setPhotoPath("/uploads/utilisateurs/default.jpg");
        }

        /*
         * createUser = nouvelle logique INVITE + activation avec mot de passe.
         * Si ton front utilise encore un mot de passe direct à l'inscription,
         * remplace createUser(...) par save(...).
         */
        UtilisateurDto savedUtilisateur = utilisateurService.createUser(dto, photo);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedUtilisateur);
    }

    // =====================================================
    // 2. RENVOYER OTP
    // =====================================================

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestBody UtilisateurDto dto) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        validationService.renvoyerCode(utilisateur);

        return ResponseEntity.ok("Nouveau code envoyé");
    }

    // =====================================================
    // 3. ACTIVATION COMPTE AVEC CODE + MOT DE PASSE
    // =====================================================

    @PostMapping("/activation")
    public ResponseEntity<String> activation(@RequestBody ActivationRequest request) {
        utilisateurService.activation(request);
        return ResponseEntity.ok("Compte activé avec succès");
    }

    // =====================================================
    // 4. ACTIVATION SIMPLE ANCIEN FORMAT { "code": "..." }
    // =====================================================

    @PostMapping("/activation-code")
    public ResponseEntity<String> activationCode(@RequestBody Map<String, String> activation) {
        utilisateurService.activation(activation);
        return ResponseEntity.ok("Compte activé avec succès.");
    }

    // =====================================================
    // 5. CONNEXION
    // =====================================================

    @PostMapping("/connexion")
    public ResponseEntity<?> connexion(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getMotDePasse()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            String token = jwtService.generateToken(userDetails);

            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email ou mot de passe incorrect");
        }
    }

    // Alias conservé si du code appelle encore login(...)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return connexion(request);
    }

    // =====================================================
    // 6. RÉCUPÉRER UN UTILISATEUR
    // =====================================================

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UtilisateurDto> getUtilisateur(@PathVariable String id) {
        return ResponseEntity.ok(utilisateurService.findById(id));
    }

    // =====================================================
    // 7. METTRE À JOUR UN UTILISATEUR
    // =====================================================

    @PutMapping(
            value = "/update/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UtilisateurDto> updateUtilisateur(
            @PathVariable String id,
            @RequestBody UtilisateurDto dto
    ) {
        return ResponseEntity.ok(utilisateurService.update(id, dto));
    }

    // =====================================================
    // 8. SUPPRIMER UN UTILISATEUR
    // =====================================================

    @DeleteMapping("/delete/{email}")
    public ResponseEntity<Void> deleteUtilisateur(@PathVariable String email) {
        utilisateurService.deleteByEmail(email);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // 9. LISTER TOUS LES UTILISATEURS
    // =====================================================

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UtilisateurDto>> getAllUtilisateurs() {
        return ResponseEntity.ok(utilisateurService.findAll());
    }

    // =====================================================
    // 10. RESET PASSWORD - NOUVELLE ROUTE
    // =====================================================

    @PostMapping(value = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> resetPassword(@RequestBody ChangementMotDePasseDto dto) {
        utilisateurService.updatePasswordByEmail(dto);
        return ResponseEntity.ok("Mot de passe mis à jour");
    }

    // =====================================================
    // 11. RESET PASSWORD - ANCIENNE ROUTE CONSERVÉE
    // =====================================================

    @PostMapping(value = "/utilisateurs/motdepasse/reset", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> resetMotDePasse(@RequestBody ChangementMotDePasseDto dto) {
        utilisateurService.updatePasswordByEmail(dto);
        return ResponseEntity.ok("Mot de passe mis à jour avec succès.");
    }

    // =====================================================
    // 12. SUSPENDRE UTILISATEUR
    // =====================================================

    @PostMapping("/suspend/{id}")
    public ResponseEntity<String> suspend(@PathVariable String id) {
        utilisateurService.suspendUser(id);
        return ResponseEntity.ok("Utilisateur suspendu");
    }

    // =====================================================
    // 13. ACTIVER UTILISATEUR
    // =====================================================

    @PostMapping("/activate/{id}")
    public ResponseEntity<String> activate(@PathVariable String id) {
        utilisateurService.activateUser(id);
        return ResponseEntity.ok("Utilisateur activé");
    }
}