package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.ChangementMotDePasseDto;
import com.esiitech.monbondocteurv2.dto.LoginRequest;
import com.esiitech.monbondocteurv2.dto.StructureSanitaireDto;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import com.esiitech.monbondocteurv2.securite.CustomUserDetails;
import com.esiitech.monbondocteurv2.securite.JwtService;
import com.esiitech.monbondocteurv2.service.StructureSanitaireService;
import com.esiitech.monbondocteurv2.service.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private final ObjectMapper objectMapper; // <-- AJOUT

    public StructureSanitaireController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Créer une structure sanitaire",
            description = "Envoie un JSON (champ structureSanitaire) + fichiers optionnels (photo, document) en multipart/form-data."
    )
    public ResponseEntity<StructureSanitaireDto> create(
            @Parameter(description = "Photo (PNG/JPEG) - optionnelle")
            @RequestParam(value = "photo", required = false) MultipartFile photo,

            @Parameter(description = "Document justificatif (PDF/JPG/PNG) - optionnel")
            @RequestParam(value = "document", required = false) MultipartFile document,

            @Parameter(description = "Données de la structure au format JSON (clé: structureSanitaire)")
            @RequestParam("structureSanitaire") String structureSanitaireJson
    ) throws IOException {

        // Parse le JSON string -> DTO
        StructureSanitaireDto dto = objectMapper.readValue(structureSanitaireJson, StructureSanitaireDto.class);

        // Appelle le service qui gère tout (unicité, upload fichier, encodage mdp, OTP…)
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
            this.structureSanitaireService.activation(activation);
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
    public ResponseEntity<?> resendOtp(@RequestBody StructureSanitaireDto dto) {
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
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            return ResponseEntity.ok(Collections.singletonMap("token", token));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email ou mot de passe incorrect");
        }
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Mettre à jour une structure sanitaire",
            description = "Met à jour les informations d'une structure sanitaire et, éventuellement, sa photo."
    )
    public ResponseEntity<StructureSanitaireDto> updateStructureSanitaire(
            @Parameter(description = "ID de la structure") @PathVariable String id,
            @Parameter(description = "Nouvelle photo (optionnelle)") @RequestParam(value = "photo", required = false) MultipartFile photo,
            @Parameter(description = "Nouvelles données de la structure au format JSON") @RequestParam("structureSanitaire") String structureSanitaireJson
    ) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        StructureSanitaireDto dto = objectMapper.readValue(structureSanitaireJson, StructureSanitaireDto.class);
        StructureSanitaireDto updatedStructureSanitaire = structureSanitaireService.update(id, dto, photo);
        return new ResponseEntity<>(updatedStructureSanitaire, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Récupérer une structure sanitaire par ID",
            description = "Retourne les informations d'une structure sanitaire à partir de son ID."
    )
    public ResponseEntity<StructureSanitaireDto> getStructureSanitaire(
            @Parameter(description = "ID de la structure") @PathVariable String id
    ) {
        StructureSanitaireDto structureSanitaireDto = structureSanitaireService.findById(id);
        return new ResponseEntity<>(structureSanitaireDto, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(
            summary = "Supprimer une structure sanitaire",
            description = "Supprime une structure sanitaire à partir de son ID."
    )
    public ResponseEntity<Void> deleteStructureSanitaire(
            @Parameter(description = "ID de la structure") @PathVariable String id
    ) {
        structureSanitaireService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/all")
    @Operation(
            summary = "Lister toutes les structures sanitaires",
            description = "Retourne la liste de toutes les structures sanitaires."
    )
    public ResponseEntity<List<StructureSanitaireDto>> getAllStructureSanitaire() {
        List<StructureSanitaireDto> structureSanitaireDtos = structureSanitaireService.findAll();
        return new ResponseEntity<>(structureSanitaireDtos, HttpStatus.OK);
    }

    /**
     * Récupère toutes les structures d'une ville donnée.
     */
    @GetMapping("/ville/{ville}")
    @Operation(
            summary = "Lister par ville",
            description = "Retourne les structures sanitaires localisées dans une ville donnée."
    )
    public ResponseEntity<List<StructureSanitaireDto>> getByVille(
            @Parameter(description = "Nom de la ville (ex: Libreville)") @PathVariable String ville
    ) {
        return ResponseEntity.ok(structureSanitaireService.findByVille(ville));
    }

    @GetMapping("/specialite/{specialite}")
    @Operation(
            summary = "Lister par spécialité",
            description = "Retourne les structures sanitaires correspondant à une spécialité (recherche insensible à la casse, partielle possible selon l'implémentation)."
    )
    public ResponseEntity<List<StructureSanitaireDto>> getBySpecialite(
            @Parameter(description = "Nom de la spécialité (ex: Cardiologie)") @PathVariable String specialite
    ) {
        return ResponseEntity.ok(structureSanitaireService.findBySpecialite(specialite));
    }

    @GetMapping(value = "/{id}/specialites", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Spécialités d'une structure",
            description = "Retourne la liste (sans doublons) des spécialités d'une structure sanitaire."
    )
    public ResponseEntity<Set<String>> getSpecialites(
            @Parameter(description = "ID de la structure") @PathVariable String id
    ) {
        return ResponseEntity.ok(structureSanitaireService.getSpecialitesStructure(id));
    }

    /**
     * Endpoint pour récupérer toutes les spécialités utilisées dans les structures sanitaires.
     */
    @GetMapping("/specialites")
    @Operation(
            summary = "Toutes les spécialités utilisées",
            description = "Retourne l'ensemble des spécialités présentes au moins une fois dans les structures enregistrées (sans doublons)."
    )
    public ResponseEntity<Set<String>> getToutesLesSpecialitesUtilisees() {
        Set<String> specialites = structureSanitaireService.getToutesLesSpecialitesUtilisees();
        return ResponseEntity.ok(specialites);
    }

    @PostMapping("/structureSanitaire/motdepasse/reset")
    @Operation(
            summary = "Modification du mot de passe",
            description = "Modifie le mot de passe d'une structure sanitaire, après validation (email existant et confirmation de mot de passe)."
    )
    public ResponseEntity<String> resetMotDePasse(@RequestBody ChangementMotDePasseDto dto) {
        structureSanitaireService.updatePasswordByEmail(dto);
        return ResponseEntity.ok("Mot de passe mis à jour avec succès.");
    }

    @GetMapping("/me")
    @Operation(summary = "Mon profil (structure connectée)", description = "Retourne le profil de la structure actuellement connectée.")
    public ResponseEntity<StructureSanitaireDto> getMyProfile() {
        return ResponseEntity.ok(structureSanitaireService.getMyProfile());
    }
    @GetMapping("/me/id")
    public ResponseEntity<String> getMyId(HttpServletRequest request) {
        String jwt = Arrays.stream(request.getCookies())
                .filter(c -> "jwt".equals(c.getName()))
                .findFirst()
                .map(jakarta.servlet.http.Cookie::getValue) // ✅ bon import
                .orElseThrow(() -> new RuntimeException("Token introuvable"));

        return ResponseEntity.ok(jwtService.extractId(jwt));
    }



    // Version multipart (permet de changer la photo)
    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Mettre à jour mon profil (multipart)", description = "Met à jour les informations + photo de la structure connectée.")
    public ResponseEntity<StructureSanitaireDto> updateMyProfileMultipart(
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam("structureSanitaire") String structureSanitaireJson
    ) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        StructureSanitaireDto dto = objectMapper.readValue(structureSanitaireJson, StructureSanitaireDto.class);
        return ResponseEntity.ok(structureSanitaireService.updateMyProfile(dto, photo));
    }

    // Version JSON pur (pas de photo)
    @PutMapping(value = "/me/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Mettre à jour mon profil (JSON)", description = "Met à jour les informations de la structure connectée (sans photo).")
    public ResponseEntity<StructureSanitaireDto> updateMyProfileJson(@RequestBody StructureSanitaireDto dto)
            throws IOException {
        return ResponseEntity.ok(structureSanitaireService.updateMyProfile(dto, null));
    }

    @Operation(tags = "Structure Sanitaire", summary = "Ajouter des spécialités (merge)")
    @PostMapping("/{id}/specialites")
    public ResponseEntity<Set<String>> addSpecialites(
            @PathVariable String id,
            @RequestBody Set<String> specialites) {
        return ResponseEntity.ok(structureSanitaireService.addSpecialites(id, specialites));
    }


    @Operation(
            summary = "Activer une structure et régénérer son mot de passe (ADMIN)",
            description = "Passe le statut à ACTIF, active le compte et envoie un nouveau mot de passe par email."
    )
    @PostMapping("/{id}/activer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activerStructure(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, Boolean> body) {

        boolean returnPassword = body != null && Boolean.TRUE.equals(body.get("returnPassword"));
        String plain = structureSanitaireService.adminActiverEtReinitialiserMdp(id);

        return returnPassword
                ? ResponseEntity.ok(Map.of("message", "Structure activée, mot de passe envoyé par email."))
                : ResponseEntity.ok("Structure activée, identifiants envoyés par email.");
    }


    // Payload pour PATCH
    static class GpsUpdateRequest {
        private Float gpsLatitude;
        private Float gpsLongitude;
        public Float getGpsLatitude() { return gpsLatitude; }
        public void setGpsLatitude(Float gpsLatitude) { this.gpsLatitude = gpsLatitude; }
        public Float getGpsLongitude() { return gpsLongitude; }
        public void setGpsLongitude(Float gpsLongitude) { this.gpsLongitude = gpsLongitude; }
    }

    @PatchMapping("/{id}/gps")
    @PreAuthorize("hasRole('ADMIN')") // admin peut modifier n’importe quelle structure
    @Operation(summary = "Mettre à jour la position GPS d’une structure (ADMIN)")
    public ResponseEntity<StructureSanitaireDto> updateGpsById(
            @PathVariable String id,
            @RequestBody GpsUpdateRequest body
    ) {
        return ResponseEntity.ok(
                structureSanitaireService.updateGpsById(id, body.getGpsLatitude(), body.getGpsLongitude())
        );
    }

    @PatchMapping("/me/gps")
    @PreAuthorize("hasRole('STRUCTURESANITAIRE')") // la structure met à jour sa propre position
    @Operation(summary = "Mettre à jour MA position GPS (structure connectée)")
    public ResponseEntity<StructureSanitaireDto> updateMyGps(@RequestBody GpsUpdateRequest body) {
        return ResponseEntity.ok(
                structureSanitaireService.updateMyGps(body.getGpsLatitude(), body.getGpsLongitude())
        );

    }
    /**
     * Archive des spécialités (déplace de refSpecialites -> archivedSpecialites).
     * Body attendu : ["Cardiologie","Dermato", ...]
     */
    @PostMapping("/{id}/specialites/archive")
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

    /**
     * Restaure des spécialités (déplace archivedSpecialites -> refSpecialites).
     * Body attendu : ["Cardiologie","Dermato", ...]
     */
    @PostMapping("/{id}/specialites/restore")
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

    /**
     * Récupère les spécialités archivées de la structure.
     */
    @GetMapping("/{id}/specialites/archived")
    public ResponseEntity<Set<String>> getArchivedSpecialites(@PathVariable("id") String structureId) {
        if (structureId == null || structureId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Set<String> archived = structureSanitaireService.getArchivedSpecialites(structureId);
        return ResponseEntity.ok(archived == null ? Collections.emptySet() : archived);
    }

}
