package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.ForgotPasswordRequest;
import com.esiitech.monbondocteurv2.dto.LoginRequest;
import com.esiitech.monbondocteurv2.dto.ResetPasswordRequest;
import com.esiitech.monbondocteurv2.dto.UtilisateurDto;
import com.esiitech.monbondocteurv2.model.Utilisateur;
import com.esiitech.monbondocteurv2.securite.CustomUserDetails;
import com.esiitech.monbondocteurv2.securite.JwtResponse;
import com.esiitech.monbondocteurv2.securite.JwtService;
import com.esiitech.monbondocteurv2.service.PasswordResetService;
import com.esiitech.monbondocteurv2.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/V2/auth")
@Tag(name = "Authentification", description = "Endpoints pour la connexion et la déconnexion")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordResetService passwordResetService;
    private final UtilisateurService utilisateurService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            PasswordResetService passwordResetService,
            UtilisateurService utilisateurService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordResetService = passwordResetService;
        this.utilisateurService = utilisateurService;
    }

    // =========================
    // LOGIN
    // =========================

    @Operation(
            summary = "Connexion de l'utilisateur",
            description = "Permet à un utilisateur de se connecter et de recevoir un token JWT dans un cookie."
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getMotDePasse()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            ResponseCookie jwtCookie = ResponseCookie.from("jwt", token)
                    .httpOnly(true)
                    .secure(false) // true en production HTTPS
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .sameSite("Strict")
                    .build();

            response.setHeader("Set-Cookie", jwtCookie.toString());

            JwtResponse jwtResponse = new JwtResponse("Connexion réussie", token);

            return ResponseEntity.ok(jwtResponse);

        } catch (AuthenticationException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Échec de la connexion"));
        }
    }

    // =========================
    // PROFIL UTILISATEUR CONNECTÉ
    // =========================

    @Operation(
            summary = "Profil utilisateur connecté",
            description = "Retourne les informations de l'utilisateur grâce au token JWT."
    )
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            String token = extractJwtFromCookieOrHeader(request);

            String email = jwtService.extractUsername(token);

            Utilisateur utilisateur = utilisateurService.findByEmail(email);

            UtilisateurDto dto = new UtilisateurDto();
            dto.setId(utilisateur.getId());
            dto.setNom(utilisateur.getNom());
            dto.setPrenom(utilisateur.getPrenom());
            dto.setEmail(utilisateur.getEmail());
            dto.setSexe(utilisateur.getSexe());
            dto.setPhotoPath(utilisateur.getPhotoPath());
            dto.setRole(utilisateur.getRole());
            dto.setNumeroTelephone(utilisateur.getNumeroTelephone());
            dto.setStatut(utilisateur.getStatutCompte());

            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Token invalide ou expiré"));
        }
    }

    // =========================
    // LOGOUT
    // =========================

    @Operation(
            summary = "Déconnexion de l'utilisateur",
            description = "Supprime le cookie JWT et met fin à la session."
    )
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.setHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(
                Map.of("message", "Déconnexion réussie")
        );
    }

    // =========================
    // MOT DE PASSE OUBLIÉ
    // =========================

    @PostMapping("/password/forgot")
    @Operation(
            summary = "Mot de passe oublié",
            description = "Envoie un email avec un lien de réinitialisation. Réponse 200 même si l'email n'existe pas."
    )
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        String frontendResetBaseUrl = "http://localhost:4200/api/V2/auth/reset";

        passwordResetService.demandeResetParEmail(
                req.getEmail(),
                frontendResetBaseUrl
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Si un compte existe pour cet email, un message a été envoyé."
                )
        );
    }

    // =========================
    // RESET PASSWORD
    // =========================

    @PostMapping(
            path = "/reset",
            consumes = "application/json",
            produces = "application/json"
    )
    @Operation(
            summary = "Nouveau mot de passe",
            description = "Réinitialise le mot de passe à partir du token reçu par email."
    )
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestParam("token") String token,
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        passwordResetService.appliquerNouveauMotDePasse(
                token,
                request
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Mot de passe modifié avec succès."
                )
        );
    }

    // =========================
    // UTILITAIRE JWT
    // =========================

    private String extractJwtFromCookieOrHeader(HttpServletRequest request) {
        if (request.getCookies() != null) {
            String tokenFromCookie = Arrays.stream(request.getCookies())
                    .filter(cookie -> "jwt".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);

            if (tokenFromCookie != null && !tokenFromCookie.isBlank()) {
                return tokenFromCookie;
            }
        }

        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        throw new RuntimeException("Token introuvable");
    }
}