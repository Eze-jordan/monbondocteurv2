// com.esiitech.monbondocteurv2.service.PasswordResetService.java
package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.ResetPasswordRequest;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.PasswordResetToken;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.repository.MedecinRepository;
import com.esiitech.monbondocteurv2.repository.PasswordResetTokenRepository;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {
    private final StructureSanitaireRepository structureRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    // Durée de validité du lien (ex: 60 min)
    private static final Duration TOKEN_TTL = Duration.ofMinutes(60);
    private final MedecinRepository medecinRepository;

    public PasswordResetService(StructureSanitaireRepository structureRepo,
                                PasswordResetTokenRepository tokenRepo,
                                PasswordEncoder passwordEncoder,
                                NotificationService notificationService, MedecinRepository medecinRepository) {
        this.structureRepo = structureRepo;
        this.tokenRepo = tokenRepo;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.medecinRepository = medecinRepository;
    }

    /** Étape 1 : Demande -> créer un token et envoyer un lien par email (pas d’OTP). */
    @Transactional
    public void demandeResetParEmail(String email, String frontendResetBaseUrl) {
        Optional<StructureSanitaire> opt = structureRepo.findByEmail(email);
        // Pour éviter l’énumération d’emails, on ne révèle pas si l’email existe ou non
        if (opt.isEmpty()) {
            return; // on renvoie quand même 200 côté controller
        }

        StructureSanitaire ss = opt.get();

        // (Optionnel) invalider les anciens tokens de ce compte
        tokenRepo.deleteByStructureSanitaire_Id(ss.getId());

        PasswordResetToken t = new PasswordResetToken();
        t.setToken(UUID.randomUUID().toString());
        t.setStructureSanitaire(ss);
        t.setExpiresAt(Instant.now().plus(TOKEN_TTL));
        t.setUsed(false);
        tokenRepo.save(t);

        String resetUrl = frontendResetBaseUrl + "?token=" + t.getToken();
        notificationService.envoyerLienReinitMdpStructure(ss.getEmail(), ss.getNomStructureSanitaire(), resetUrl);
    }

    /** Étape 2 : Consommation du lien -> poser le nouveau mot de passe. */
    @Transactional
    public void appliquerNouveauMotDePasse(String token, ResetPasswordRequest req) {
        PasswordResetToken t = tokenRepo.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        if (t.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Le lien de réinitialisation a expiré.");
        }

        if (req.getNouveauMotDePasse() == null
                || !req.getNouveauMotDePasse().equals(req.getConfirmerMotDePasse())) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        }

        StructureSanitaire ss = t.getStructureSanitaire();
        ss.setMotDePasse(passwordEncoder.encode(req.getNouveauMotDePasse()));

        t.setUsed(true); // token à usage unique
        // Transactional => flush auto
    }
}
