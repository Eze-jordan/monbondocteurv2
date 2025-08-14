package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.ChangementMotDePasseDto;
import com.esiitech.monbondocteurv2.dto.StructureSanitaireDto;
import com.esiitech.monbondocteurv2.mapper.StructureSanitaireMapper;
import com.esiitech.monbondocteurv2.model.*;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import java.security.SecureRandom;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
@Service
public class StructureSanitaireService implements UserDetailsService {

    @Autowired
    private StructureSanitaireRepository repository;
    @Autowired
    private ValidationService validationService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private StructureSanitaireMapper mapper;
    // ✅ deux propriétés distinctes
    @Value("${app.upload.dir.structureSanitaire}")
    private String uploadDirStructure;            // ex: /var/app/uploads/structuresanitaire

    @Value("${app.upload.dir.documentStructure}")
    private String uploadDirDocs;                 // ex: /var/app/uploads/documentStructure

    private static final String DEFAULT_PHOTO_PATH = "/uploads/structuresanitaire/default.jpg"; // Photo par défaut

    /**
     * Enregistrer une structure sanitaire avec sa photo.
     */
    // ------- CREATE -------
    @Transactional
    public StructureSanitaireDto create(StructureSanitaireDto dto,
                                        MultipartFile photo,
                                        MultipartFile document) throws IOException {

        validateStructureSanitaireDto(dto);

        String email = dto.getEmail().trim().toLowerCase();
        String phone = dto.getNumeroTelephone().trim();

        if (repository.existsByEmail(email))  throw new IllegalArgumentException("Cet email est déjà utilisé.");
        if (repository.existsByNumeroTelephone(phone)) throw new IllegalArgumentException("Ce numéro de téléphone est déjà utilisé.");

        StructureSanitaire ss = mapper.toEntity(dto);

        if (ss.getId() == null || ss.getId().isBlank()) ss.setId(generateCustomId());
        ss.setEmail(email);
        ss.setNumeroTelephone(phone);
        ss.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));

        // PHOTO (optionnelle)
        if (photo != null && !photo.isEmpty()) {
            ss.setPhotoPath(savePhoto(photo));
        } else if (ss.getPhotoPath() == null || ss.getPhotoPath().isBlank()) {
            ss.setPhotoPath(DEFAULT_PHOTO_PATH);
        }

        // DOCUMENT (optionnel) — soit upload, soit URL déjà fournie dans le DTO
        if (document != null && !document.isEmpty()) {
            ss.setUrldocument(saveDocument(document)); // on stocke et renvoie une URL relative
        } else if (dto.getUrldocument() != null && !dto.getUrldocument().isBlank()) {
            ss.setUrldocument(dto.getUrldocument().trim());
        }

        ss.setRefSpecialites(normalizeSpecialites(dto.getRefSpecialites()));
        ss.setRole(Role.STRUCTURESANITAIRE);
        ss.setStatut(Statut.SUSPENDU);
        ss.setActif(false);

        StructureSanitaire saved = repository.save(ss);

        // Envoi OTP
        validationService.enregisterStructure(saved);
        return mapper.toDto(saved);
    }

    /** Sauvegarde le document (PDF/JPG/PNG) et renvoie une URL relative à exposer côté front. */

    private String saveDocument(MultipartFile file) throws IOException {
        String ct = file.getContentType();
        if (ct == null || !(ct.equals("application/pdf") || ct.equals("image/jpeg") || ct.equals("image/png"))) {
            throw new IllegalArgumentException("Le document doit être un PDF, JPEG ou PNG.");
        }

        String name = System.currentTimeMillis() + "_" + Objects.requireNonNull(file.getOriginalFilename());
        Path dir = Paths.get(uploadDirDocs);          // ex: /var/.../uploads/documentStructure
        Files.createDirectories(dir);
        Files.write(dir.resolve(name), file.getBytes());

        // ⚠️ ajout du slash manquant
        return "/uploads/documentStructure/" + name;
    }

    private static long lastId = 500000;  // Commence à 500000

    private synchronized String generateCustomId() {
        lastId++;
        return String.format("%06d", lastId);
    }

    public void activation(Map<String, String> activation) {
        Validation validation = validationService.lireEnFonctionDuCode(activation.get("code"));
        if (Instant.now().isAfter(validation.getExpiration())) {
            throw new RuntimeException("Votre code a expiré");
        }

        StructureSanitaire StructureActiver = repository.findById(validation.getStructureSanitaire().getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur inconnu"));

        StructureActiver.setActif(true);
        repository.save(StructureActiver);
        notificationService.envoyerAccuseEnregistrementStructure(StructureActiver.getEmail(), StructureActiver.getNomStructureSanitaire());

    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.repository.findByEmail(username).orElseThrow (()
                -> new UsernameNotFoundException(
                "Aucun utilisateur ne conrespond à cet identifiant"
        ));
    }

    public Set<String> getSpecialitesStructure(String id) {
        StructureSanitaire structure = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));
        return structure.getRefSpecialites();
    }


    public Set<String> getToutesLesSpecialitesUtilisees() {
        return repository.findAll().stream()
                .map(StructureSanitaire::getRefSpecialites)
                .filter(Objects::nonNull)
                .flatMap(Set::stream)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public List<StructureSanitaireDto> findBySpecialite(String specialite) {
        return repository.findBySpecialite(specialite).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public List<StructureSanitaireDto> findByVille(String ville) {
        List<StructureSanitaire> structures = repository.findByVilleIgnoreCase(ville);
        return structures.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }



    /**
     * Valider les données du DTO de la structure sanitaire avant d'effectuer l'enregistrement
     */
    private void validateStructureSanitaireDto(StructureSanitaireDto dto) {
        if (dto.getNomStructureSanitaire() == null || dto.getNomStructureSanitaire().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la structure sanitaire ne peut pas être vide.");
        }

        if (dto.getAdresse() == null || dto.getAdresse().isEmpty()) {
            throw new IllegalArgumentException("L'adresse de la structure sanitaire ne peut pas être vide.");
        }
    }

    /**
     * Sauvegarder la photo de la structure sanitaire et retourner son chemin.
     * Cette méthode vérifie également que le fichier est un type d'image valide.
     */
    private String savePhoto(MultipartFile file) throws IOException {
        String ct = file.getContentType();
        if (ct == null || !(ct.equals("image/jpeg") || ct.equals("image/png"))) {
            throw new IllegalArgumentException("Le fichier photo doit être JPEG ou PNG.");
        }

        String name = System.currentTimeMillis() + "_" + Objects.requireNonNull(file.getOriginalFilename());
        Path dir = Paths.get(uploadDirStructure);     // ex: /var/.../uploads/structuresanitaire
        Files.createDirectories(dir);
        Files.write(dir.resolve(name), file.getBytes());

        // URL publique pour le front
        return "/uploads/structuresanitaire/" + name;
    }


    /**
     * Récupérer une structure sanitaire par son ID.
     */
    public StructureSanitaireDto findById(String id) {
        StructureSanitaire structureSanitaire = repository.findById(id).orElseThrow(() -> new RuntimeException("Structure sanitaire non trouvée"));
        return mapper.toDto(structureSanitaire);
    }

    /**
     * Mettre à jour les informations d'une structure sanitaire.
     */
    public StructureSanitaireDto update(String id, StructureSanitaireDto dto, MultipartFile photo) throws IOException {
        StructureSanitaire structureSanitaire = repository.findById(id).orElseThrow(() -> new RuntimeException("Structure sanitaire non trouvée"));

        // Mettre à jour les informations de la structure sanitaire
        structureSanitaire.setNomStructureSanitaire(dto.getNomStructureSanitaire());
        structureSanitaire.setAdresse(dto.getAdresse());
        structureSanitaire.setEmail(dto.getEmail());
        structureSanitaire.setNumeroTelephone(dto.getNumeroTelephone());
        structureSanitaire.setVille(dto.getVille());

        // Si une nouvelle photo est fournie, la sauvegarder
        if (photo != null && !photo.isEmpty()) {
            String photoPath = savePhoto(photo);  // Sauvegarder la nouvelle photo et obtenir son chemin
            structureSanitaire.setPhotoPath(photoPath);  // Mettre à jour le chemin de la photo
        }

        // Sauvegarder les modifications
        structureSanitaire = repository.save(structureSanitaire);

        return mapper.toDto(structureSanitaire);
    }

    /**
     * Supprimer une structure sanitaire par son ID.
     */
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    /**
     * Récupérer toutes les structures sanitaires.
     */
    public List<StructureSanitaireDto> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public void updatePasswordByEmail(ChangementMotDePasseDto dto) {
        if (!dto.getNouveauMotDePasse().equals(dto.getConfirmerMotDePasse())) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        }

        StructureSanitaire structureSanitaire = repository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur avec cet email non trouvé"));

        structureSanitaire.setMotDePasse(passwordEncoder.encode(dto.getNouveauMotDePasse()));
        repository.save(structureSanitaire);
    }

    /** Récupère le profil de la structure actuellement connectée (via JWT/SecurityContext). */
    public StructureSanitaireDto getMyProfile() {
        String emailConnecte = SecurityContextHolder.getContext().getAuthentication().getName();
        StructureSanitaire me = repository.findByEmail(emailConnecte)
                .orElseThrow(() -> new RuntimeException("Structure introuvable pour l'email connecté"));
        return mapper.toDto(me);
    }

    /** Met à jour le profil de la structure connectée (nom, email, téléphone, adresse, ville, photo…).
     * Remarque : si tu permets de changer l’email, le JWT en cours contient encore l’ancien email.
     * Après update, fais re-login côté front pour régénérer un token propre. */
    public StructureSanitaireDto updateMyProfile(StructureSanitaireDto dto, MultipartFile photo) throws IOException {
        String emailConnecte = SecurityContextHolder.getContext().getAuthentication().getName();
        StructureSanitaire me = repository.findByEmail(emailConnecte)
                .orElseThrow(() -> new RuntimeException("Structure introuvable pour l'email connecté"));

        // (Optionnel) contrôles d’unicité si changement d’email/téléphone
        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(me.getEmail())) {
            if (repository.existsByEmailAndIdNot(dto.getEmail(), me.getId())) {
                throw new IllegalArgumentException("Cet email est déjà utilisé.");
            }
            me.setEmail(dto.getEmail());
        }
        if (dto.getNumeroTelephone() != null && !dto.getNumeroTelephone().equalsIgnoreCase(me.getNumeroTelephone())) {
            if (repository.existsByNumeroTelephoneAndIdNot(dto.getNumeroTelephone(), me.getId())) {
                throw new IllegalArgumentException("Ce numéro de téléphone est déjà utilisé.");
            }
            me.setNumeroTelephone(dto.getNumeroTelephone());
        }

        // Champs “profil” autorisés
        if (dto.getNomStructureSanitaire() != null) me.setNomStructureSanitaire(dto.getNomStructureSanitaire());
        if (dto.getAdresse() != null)               me.setAdresse(dto.getAdresse());
        if (dto.getVille() != null)                 me.setVille(dto.getVille());
        if (dto.getRefType() != null)               me.setRefType(dto.getRefType());
        if (dto.getGpsLatitude() != null)           me.setGpsLatitude(dto.getGpsLatitude());
        if (dto.getGpsLongitude() != null)          me.setGpsLongitude(dto.getGpsLongitude());
        if (dto.getRefSpecialites() != null)        me.setRefSpecialites(dto.getRefSpecialites()); // Set<String>

        // Photo (optionnelle)
        if (photo != null && !photo.isEmpty()) {
            String photoPath = savePhoto(photo);
            me.setPhotoPath(photoPath);
        }

        // ⚠️ Ne PAS toucher à: id, role, motDePasse, actif ici
        StructureSanitaire saved = repository.save(me);
        return mapper.toDto(saved);
    }


    // Ajout (merge) d’une liste de spécialités
    public Set<String> addSpecialites(String structureId, Set<String> toAdd) {
        StructureSanitaire ss = repository.findById(structureId)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        if (ss.getRefSpecialites() == null) ss.setRefSpecialites(new HashSet<>());

        // normaliser (trim, éviter doublons, ignorer vides)
        Set<String> normalized = toAdd.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        ss.getRefSpecialites().addAll(normalized);
        repository.save(ss);
        return ss.getRefSpecialites();
    }

    // Nettoie la liste des spécialités (trim, retire null/vides, supprime doublons)
    private Set<String> normalizeSpecialites(Set<String> in) {
        if (in == null) return new LinkedHashSet<>();
        return in.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
    // StructureSanitaireService.java
    @Transactional
    public String adminActiverEtReinitialiserMdp(String id) {
        StructureSanitaire ss = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        // 1) Générer un nouveau mot de passe fort (en clair, pour l’email/retour)
        String plain = generateStrongPassword(14); // ex: 14 caractères

        // 2) Remplacer le mot de passe en base par le HASH du nouveau mot de passe
        ss.setMotDePasse(passwordEncoder.encode(plain));

        // 3) Activer le compte et le statut
        ss.setActif(true);
        ss.setStatut(Statut.ACTIF);

        // 4) Persister
        repository.save(ss);

        // 5) Prévenir la structure (email)
        notificationService.envoyerIdentifiantsStructure(
                ss.getEmail(),
                ss.getNomStructureSanitaire(),
                ss.getId(),
                plain // ⚠️ seulement pour communication à l’utilisateur
        );

        // 6) Optionnel: renvoyer le plain au controller si returnPassword=true (tests)
        return plain;
    }

    private String generateStrongPassword(int len) {
        String U = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String L = "abcdefghijklmnopqrstuvwxyz";
        String D = "0123456789";
        String S = "@$!%*?&";
        String ALL = U + L + D + S;

        java.security.SecureRandom r = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder(len);

        // garantir au moins 1 de chaque catégorie
        sb.append(U.charAt(r.nextInt(U.length())));
        sb.append(L.charAt(r.nextInt(L.length())));
        sb.append(D.charAt(r.nextInt(D.length())));
        sb.append(S.charAt(r.nextInt(S.length())));

        for (int i = 4; i < len; i++) {
            sb.append(ALL.charAt(r.nextInt(ALL.length())));
        }

        // petit shuffle
        return sb.chars()
                .mapToObj(c -> (char) c)
                .sorted((a,b) -> r.nextInt(3) - 1)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }


    @Transactional
    public StructureSanitaireDto updateGpsById(String id, Float lat, Float lon) {
        StructureSanitaire ss = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        applyAndValidateGps(ss, lat, lon);
        repository.save(ss);
        return mapper.toDto(ss);
    }

    @Transactional
    public StructureSanitaireDto updateMyGps(Float lat, Float lon) {
        String emailConnecte = SecurityContextHolder.getContext().getAuthentication().getName();
        StructureSanitaire me = repository.findByEmail(emailConnecte)
                .orElseThrow(() -> new RuntimeException("Structure introuvable pour l'email connecté"));

        applyAndValidateGps(me, lat, lon);
        repository.save(me);
        return mapper.toDto(me);
    }


    // --- helpers ---
    private void applyAndValidateGps(StructureSanitaire ss, Float lat, Float lon) {
        if (lat == null && lon == null) {
            throw new IllegalArgumentException("gpsLatitude ou gpsLongitude doit être fourni.");
        }
        if (lat != null) {
            validateLatitude(lat);
            ss.setGpsLatitude(lat);
        }
        if (lon != null) {
            validateLongitude(lon);
            ss.setGpsLongitude(lon);
        }
    }

    private void validateLatitude(float lat) {
        if (lat < -90f || lat > 90f) {
            throw new IllegalArgumentException("gpsLatitude doit être dans [-90, 90].");
        }
    }

    private void validateLongitude(float lon) {
        if (lon < -180f || lon > 180f) {
            throw new IllegalArgumentException("gpsLongitude doit être dans [-180, 180].");
        }
    }


}

