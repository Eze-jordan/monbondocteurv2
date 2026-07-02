package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.ChangementMotDePasseDto;
import com.esiitech.monbondocteurv2.dto.LoginRequest;
import com.esiitech.monbondocteurv2.dto.SpecialiteDetailDto;
import com.esiitech.monbondocteurv2.dto.StructureSanitaireDto;
import com.esiitech.monbondocteurv2.dto.StructureSpecialitesStatsDto;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import com.esiitech.monbondocteurv2.securite.CustomUserDetails;
import com.esiitech.monbondocteurv2.securite.JwtService;
import com.esiitech.monbondocteurv2.service.AvisService;
import com.esiitech.monbondocteurv2.service.MedecinStructureSanitaireService;
import com.esiitech.monbondocteurv2.service.StructureSanitaireService;
import com.esiitech.monbondocteurv2.service.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/V2/structuresanitaires")
@Tag(name = "Structure Sanitaire", description = "Endpoints pour la gestion des structures sanitaires")
public class StructureSanitaireController {

    private final StructureSanitaireService structureSanitaireService;
    private final StructureSanitaireRepository sanitaireRepository;
    private final ValidationService validationService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MedecinStructureSanitaireService medecinStructureSanitaireService;
    private final AvisService avisService;
    private final ObjectMapper objectMapper;

    public StructureSanitaireController(
            StructureSanitaireService structureSanitaireService,
            StructureSanitaireRepository sanitaireRepository,
            ValidationService validationService,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            MedecinStructureSanitaireService medecinStructureSanitaireService,
            AvisService avisService,
            ObjectMapper objectMapper
    ) {
        this.structureSanitaireService = structureSanitaireService;
        this.sanitaireRepository = sanitaireRepository;
        this.validationService = validationService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.medecinStructureSanitaireService = medecinStructureSanitaireService;
        this.avisService = avisService;
        this.objectMapper = objectMapper;
    }

    // ============================================================
    // CRÉATION / ACTIVATION / CONNEXION
    // ============================================================

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Créer une structure sanitaire",
            description = "Envoie un JSON (champ structureSanitaire) + fichiers optionnels (photo, document) en multipart/form-data."
    )
    public ResponseEntity<StructureSanitaireDto> create(
            @Parameter(description = "Photo PNG/JPEG optionnelle")
            @RequestParam(value = "photo", required = false) MultipartFile photo,

            @Parameter(description = "Document justificatif PDF/JPG/PNG optionnel")
            @RequestParam(value = "document", required = false) MultipartFile document,

            @Parameter(description = "Données de la structure au format JSON")
            @RequestParam("structureSanitaire") String structureSanitaireJson
    ) throws IOException {
        StructureSanitaireDto dto = objectMapper.readValue(structureSanitaireJson, StructureSanitaireDto.class);
        StructureSanitaireDto saved = structureSanitaireService.create(dto, photo, document);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/activation")
    @Operation(
            summary = "Activer un compte",
            description = "Active le compte d'une structure sanitaire via un code OTP."
    )
    public ResponseEntity<String> activation(@RequestBody Map<String, String> activation) {
        try {
            structureSanitaireService.activation(activation);
            return ResponseEntity.ok("Compte activé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/resend-otp")
    @Operation(
            summary = "Renvoyer un code OTP",
            description = "Renvoie un nouveau code OTP si l'ancien a expiré."
    )
    public ResponseEntity<String> resendOtp(@RequestBody StructureSanitaireDto dto) {
        StructureSanitaire structureSanitaire = sanitaireRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        validationService.renvoyerCodeStructure(structureSanitaire);

        return ResponseEntity.ok("Nouveau code envoyé");
    }

    @PostMapping("/connexion")
    @Operation(
            summary = "Connexion",
            description = "Authentifie une structure sanitaire et retourne un JWT."
    )
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

    // ============================================================
    // CRUD DE BASE
    // ============================================================

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Mettre à jour une structure sanitaire",
            description = "Met à jour les informations d'une structure sanitaire et éventuellement sa photo."
    )
    public ResponseEntity<StructureSanitaireDto> updateStructureSanitaire(
            @Parameter(description = "ID de la structure")
            @PathVariable String id,

            @Parameter(description = "Nouvelle photo optionnelle")
            @RequestParam(value = "photo", required = false) MultipartFile photo,

            @Parameter(description = "Nouvelles données de la structure au format JSON")
            @RequestParam("structureSanitaire") String structureSanitaireJson
    ) throws IOException {
        StructureSanitaireDto dto = objectMapper.readValue(structureSanitaireJson, StructureSanitaireDto.class);
        StructureSanitaireDto updated = structureSanitaireService.update(id, dto, photo);

        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Récupérer une structure sanitaire par ID",
            description = "Retourne les informations d'une structure sanitaire à partir de son ID."
    )
    public ResponseEntity<StructureSanitaireDto> getStructureSanitaire(
            @Parameter(description = "ID de la structure")
            @PathVariable String id
    ) {
        return ResponseEntity.ok(structureSanitaireService.findById(id));
    }

    @DeleteMapping("/delete/{id}")
    @Operation(
            summary = "Supprimer une structure sanitaire",
            description = "Supprime une structure sanitaire à partir de son ID."
    )
    public ResponseEntity<Void> deleteStructureSanitaire(
            @Parameter(description = "ID de la structure")
            @PathVariable String id
    ) {
        structureSanitaireService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    @Operation(
            summary = "Lister toutes les structures sanitaires",
            description = "Retourne la liste de toutes les structures sanitaires."
    )
    public ResponseEntity<List<StructureSanitaireDto>> getAllStructureSanitaire() {
        return ResponseEntity.ok(structureSanitaireService.findAll());
    }

    @GetMapping("/ville/{ville}")
    @Operation(
            summary = "Lister par ville",
            description = "Retourne les structures sanitaires localisées dans une ville donnée."
    )
    public ResponseEntity<List<StructureSanitaireDto>> getByVille(
            @Parameter(description = "Nom de la ville")
            @PathVariable String ville
    ) {
        return ResponseEntity.ok(structureSanitaireService.findByVille(ville));
    }

    @GetMapping("/specialite/{specialite}")
    @Operation(
            summary = "Lister par spécialité",
            description = "Retourne les structures sanitaires correspondant à une spécialité."
    )
    public ResponseEntity<List<StructureSanitaireDto>> getBySpecialite(
            @Parameter(description = "Nom de la spécialité")
            @PathVariable String specialite
    ) {
        return ResponseEntity.ok(structureSanitaireService.findBySpecialite(specialite));
    }

    @GetMapping("/ville/{ville}/specialite/{specialite}")
    @Operation(
            summary = "Lister par ville et spécialité",
            description = "Retourne les structures sanitaires correspondant à une ville et une spécialité."
    )
    public ResponseEntity<List<StructureSanitaireDto>> getByVilleAndSpecialite(
            @PathVariable String ville,
            @PathVariable String specialite
    ) {
        return ResponseEntity.ok(structureSanitaireService.findByVilleAndSpecialite(ville, specialite));
    }

    @GetMapping("/search")
    @Operation(
            summary = "Rechercher une structure par nom",
            description = "Retourne des suggestions de structures sanitaires par nom."
    )
    public ResponseEntity<List<StructureSanitaireDto>> searchByNom(@RequestParam("nom") String nom) {
        return ResponseEntity.ok(structureSanitaireService.searchByNom(nom));
    }

    // ============================================================
    // GESTION DES SPÉCIALITÉS
    // ============================================================

    @GetMapping(value = "/{id}/specialites", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Spécialités d'une structure",
            description = "Retourne la liste des spécialités d'une structure sanitaire."
    )
    public ResponseEntity<Set<String>> getSpecialites(
            @Parameter(description = "ID de la structure")
            @PathVariable String id
    ) {
        return ResponseEntity.ok(structureSanitaireService.getSpecialitesStructure(id));
    }

    @GetMapping(value = "/me/specialites", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Mes spécialités",
            description = "Retourne les spécialités de la structure actuellement connectée."
    )
    public ResponseEntity<Set<String>> getMySpecialites() {
        return ResponseEntity.ok(structureSanitaireService.getMySpecialites());
    }

    @GetMapping("/specialites")
    @Operation(
            summary = "Toutes les spécialités utilisées",
            description = "Retourne toutes les spécialités présentes dans les structures enregistrées."
    )
    public ResponseEntity<Set<String>> getToutesLesSpecialitesUtilisees() {
        return ResponseEntity.ok(structureSanitaireService.getToutesLesSpecialitesUtilisees());
    }

    @PostMapping("/{id}/specialites")
    @Operation(summary = "Ajouter des spécialités")
    public ResponseEntity<Set<String>> addSpecialites(
            @PathVariable String id,
            @RequestBody Set<String> specialites
    ) {
        return ResponseEntity.ok(structureSanitaireService.addSpecialites(id, specialites));
    }

    @PostMapping("/{id}/specialites/archive")
    @Operation(summary = "Archiver des spécialités")
    public ResponseEntity<Set<String>> archiveSpecialites(
            @PathVariable("id") String structureId,
            @RequestBody(required = false) Set<String> specialitesToArchive
    ) {
        if (structureId == null || structureId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        if (specialitesToArchive == null || specialitesToArchive.isEmpty()) {
            return ResponseEntity.ok(Collections.emptySet());
        }

        Set<String> archived = structureSanitaireService.archiveSpecialites(structureId, specialitesToArchive);
        return ResponseEntity.ok(archived);
    }

    @PostMapping("/{id}/specialites/restore")
    @Operation(summary = "Restaurer des spécialités archivées")
    public ResponseEntity<Set<String>> restoreSpecialites(
            @PathVariable("id") String structureId,
            @RequestBody(required = false) Set<String> specialitesToRestore
    ) {
        if (structureId == null || structureId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        if (specialitesToRestore == null || specialitesToRestore.isEmpty()) {
            return ResponseEntity.ok(Collections.emptySet());
        }

        Set<String> active = structureSanitaireService.restoreSpecialites(structureId, specialitesToRestore);
        return ResponseEntity.ok(active);
    }

    @GetMapping("/{id}/specialites/archived")
    @Operation(summary = "Récupérer les spécialités archivées")
    public ResponseEntity<Set<String>> getArchivedSpecialites(@PathVariable("id") String structureId) {
        if (structureId == null || structureId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Set<String> archived = structureSanitaireService.getArchivedSpecialites(structureId);
        return ResponseEntity.ok(archived == null ? Collections.emptySet() : archived);
    }

    // ============================================================
    // STATISTIQUES DES SPÉCIALITÉS
    // ============================================================

    @GetMapping("/{id}/specialites/stats")
    @Operation(
            summary = "Statistiques des spécialités",
            description = "Retourne les statistiques complètes de toutes les spécialités d'une structure."
    )
    public ResponseEntity<StructureSpecialitesStatsDto> getSpecialitesStats(
            @Parameter(description = "ID de la structure")
            @PathVariable String id
    ) {
        return ResponseEntity.ok(medecinStructureSanitaireService.getStructureSpecialitesStats(id));
    }

    @GetMapping("/{id}/specialites/{specialite}/detail")
    @Operation(
            summary = "Détail d'une spécialité",
            description = "Retourne le détail complet d'une spécialité."
    )
    public ResponseEntity<SpecialiteDetailDto> getSpecialiteDetail(
            @Parameter(description = "ID de la structure")
            @PathVariable String id,

            @Parameter(description = "Nom de la spécialité")
            @PathVariable String specialite
    ) {
        return ResponseEntity.ok(medecinStructureSanitaireService.getSpecialiteDetail(id, specialite));
    }

    @GetMapping("/{id}/specialites/{specialite}/medecins-disponibles")
    @Operation(
            summary = "Médecins disponibles pour une spécialité",
            description = "Retourne la liste des médecins disponibles aujourd'hui pour une spécialité donnée."
    )
    public ResponseEntity<List<SpecialiteDetailDto.MedecinSpecialiteDetailDto>> getMedecinsDisponibles(
            @Parameter(description = "ID de la structure")
            @PathVariable String id,

            @Parameter(description = "Nom de la spécialité")
            @PathVariable String specialite
    ) {
        return ResponseEntity.ok(
                medecinStructureSanitaireService.getMedecinsDisponiblesAujourdhui(id, specialite)
        );
    }

    @GetMapping("/{id}/specialites/{specialite}/note")
    @Operation(
            summary = "Note moyenne d'une spécialité",
            description = "Retourne la note moyenne d'une spécialité dans une structure."
    )
    public ResponseEntity<Double> getNoteSpecialite(
            @Parameter(description = "ID de la structure")
            @PathVariable String id,

            @Parameter(description = "Nom de la spécialité")
            @PathVariable String specialite
    ) {
        return ResponseEntity.ok(avisService.getNoteMoyenneSpecialite(id, specialite));
    }

    // ============================================================
    // PROFIL CONNECTÉ
    // ============================================================

    @GetMapping("/me")
    @Operation(
            summary = "Mon profil",
            description = "Retourne le profil de la structure actuellement connectée."
    )
    public ResponseEntity<StructureSanitaireDto> getMyProfile() {
        return ResponseEntity.ok(structureSanitaireService.getMyProfile());
    }

    @GetMapping("/me/id")
    @Operation(
            summary = "Mon ID",
            description = "Retourne l'ID de la structure connectée depuis le token JWT."
    )
    public ResponseEntity<String> getMyId(HttpServletRequest request) {
        String jwt = extractJwtFromCookieOrHeader(request);
        return ResponseEntity.ok(jwtService.extractId(jwt));
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Mettre à jour mon profil multipart",
            description = "Met à jour les informations et la photo de la structure connectée."
    )
    public ResponseEntity<StructureSanitaireDto> updateMyProfileMultipart(
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam("structureSanitaire") String structureSanitaireJson
    ) throws IOException {
        StructureSanitaireDto dto = objectMapper.readValue(structureSanitaireJson, StructureSanitaireDto.class);
        return ResponseEntity.ok(structureSanitaireService.updateMyProfile(dto, photo));
    }

    @PutMapping(value = "/me/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Mettre à jour mon profil JSON",
            description = "Met à jour les informations de la structure connectée sans photo."
    )
    public ResponseEntity<StructureSanitaireDto> updateMyProfileJson(@RequestBody StructureSanitaireDto dto)
            throws IOException {
        return ResponseEntity.ok(structureSanitaireService.updateMyProfile(dto, null));
    }

    // ============================================================
    // ADMIN / MOT DE PASSE
    // ============================================================

    @PostMapping("/structureSanitaire/motdepasse/reset")
    @Operation(
            summary = "Modification du mot de passe",
            description = "Modifie le mot de passe d'une structure sanitaire."
    )
    public ResponseEntity<String> resetMotDePasse(@RequestBody ChangementMotDePasseDto dto) {
        structureSanitaireService.updatePasswordByEmail(dto);
        return ResponseEntity.ok("Mot de passe mis à jour avec succès.");
    }

    @PostMapping("/{id}/activer")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Activer une structure et régénérer son mot de passe",
            description = "Passe le statut à ACTIF, active le compte et envoie un nouveau mot de passe par email."
    )
    public ResponseEntity<?> activerStructure(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, Boolean> body
    ) {
        structureSanitaireService.adminActiverEtReinitialiserMdp(id);
        return ResponseEntity.ok("Structure activée, identifiants envoyés par email.");
    }

    // ============================================================
    // GPS
    // ============================================================

    static class GpsUpdateRequest {
        private Float gpsLatitude;
        private Float gpsLongitude;

        public Float getGpsLatitude() {
            return gpsLatitude;
        }

        public void setGpsLatitude(Float gpsLatitude) {
            this.gpsLatitude = gpsLatitude;
        }

        public Float getGpsLongitude() {
            return gpsLongitude;
        }

        public void setGpsLongitude(Float gpsLongitude) {
            this.gpsLongitude = gpsLongitude;
        }
    }

    @PatchMapping("/{id}/gps")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour la position GPS d'une structure")
    public ResponseEntity<StructureSanitaireDto> updateGpsById(
            @PathVariable String id,
            @RequestBody GpsUpdateRequest body
    ) {
        return ResponseEntity.ok(
                structureSanitaireService.updateGpsById(
                        id,
                        body.getGpsLatitude(),
                        body.getGpsLongitude()
                )
        );
    }

    @PatchMapping("/me/gps")
    @PreAuthorize("hasRole('STRUCTURESANITAIRE')")
    @Operation(summary = "Mettre à jour ma position GPS")
    public ResponseEntity<StructureSanitaireDto> updateMyGps(@RequestBody GpsUpdateRequest body) {
        return ResponseEntity.ok(
                structureSanitaireService.updateMyGps(
                        body.getGpsLatitude(),
                        body.getGpsLongitude()
                )
        );
    }

    // ============================================================
    // UTILITAIRE JWT
    // ============================================================

    private String extractJwtFromCookieOrHeader(HttpServletRequest request) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> "jwt".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElseGet(() -> extractJwtFromAuthorizationHeader(request));
        }

        return extractJwtFromAuthorizationHeader(request);
    }

    private String extractJwtFromAuthorizationHeader(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        throw new RuntimeException("Token introuvable");
    }
}