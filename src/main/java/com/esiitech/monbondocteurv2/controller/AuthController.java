package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.ForgotPasswordRequest;
import com.esiitech.monbondocteurv2.dto.LoginRequest;
import com.esiitech.monbondocteurv2.dto.ResetPasswordRequest;
import com.esiitech.monbondocteurv2.securite.CustomUserDetails;
import com.esiitech.monbondocteurv2.securite.JwtResponse;
import com.esiitech.monbondocteurv2.securite.JwtService;
import com.esiitech.monbondocteurv2.service.CustomUserDetailsService;
import com.esiitech.monbondocteurv2.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/api/V2/auth")
@Tag(name = "Authentification", description = "Endpoints pour la connexion et la déconnexion")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          CustomUserDetailsService userDetailsService, PasswordResetService passwordResetService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordResetService = passwordResetService;
    }

    @Operation(summary = "Connexion de l'utilisateur", description = "Permet à un utilisateur de se connecter et de recevoir un token JWT dans un cookie sécurisé")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
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

            // ✅ Cookie sécurisé
            ResponseCookie jwtCookie = ResponseCookie.from("jwt", token)
                    .httpOnly(true)
                    .secure(false) // 🔒 true en production avec HTTPS
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .sameSite("Strict")
                    .build();

            response.setHeader("Set-Cookie", jwtCookie.toString());

            // ✅ Ajout du token dans la réponse JSON (Swagger-friendly)
            JwtResponse jwtResponse = new JwtResponse("Connexion réussie", token);

            return ResponseEntity.ok(jwtResponse);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Échec de la connexion");
        }
    }

    @Operation(summary = "Déconnexion de l'utilisateur", description = "Supprime le cookie JWT et met fin à la session")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0) // 🔥 Expire immédiatement
                .sameSite("Strict")
                .build();

        response.setHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.ok("Déconnexion réussie");
    }

    // OUVERT (pas d’auth) — demande de reset
    @PostMapping("/password/forgot")
    @Operation(summary = "Mot de passe oublié (sans OTP)",
            description = "Envoie un email avec un lien de réinitialisation. Réponse 200 même si l'email n'existe pas.")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        // URL de ta page front qui recevra ?token=...
        String frontendResetBaseUrl = "https://monbondocteur.com/reset";
        passwordResetService.demandeResetParEmail(req.getEmail(), frontendResetBaseUrl);
        return ResponseEntity.ok("Si un compte existe pour cet email, un message a été envoyé.");
    }

    // OUVERT (pas d’auth) — consommer le lien
    @Operation(summary = "nouveau mot de pass",
            description = "Envoie un email avec un lien de réinitialisation et saisie du nouveau mot de passe")

    @PostMapping("/reset")
    public ResponseEntity<Void> resetPassword(
            @RequestParam("token") String token,
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        passwordResetService.appliquerNouveauMotDePasse(token, request);
        return ResponseEntity.noContent().build();
    }

}
