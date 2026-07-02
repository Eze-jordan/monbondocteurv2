package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.ChangementMotDePasseDto;
import com.esiitech.monbondocteurv2.dto.StructureSanitaireDto;
import com.esiitech.monbondocteurv2.enums.Role;
import com.esiitech.monbondocteurv2.enums.Statut;
import com.esiitech.monbondocteurv2.mapper.StructureSanitaireMapper;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.model.Validation;
import com.esiitech.monbondocteurv2.repository.MedecinRepository;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import com.esiitech.monbondocteurv2.repository.UtilisateurRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
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

    @Autowired
    private AbonnementStructureService abonnementStructureService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private MedecinRepository medecinRepository;

    @Value("${app.upload.dir.structureSanitaire}")
    private String uploadDirStructure;

    @Value("${app.upload.dir.documentStructure}")
    private String uploadDirDocs;

    private static final String DEFAULT_PHOTO_PATH = "/uploads/structuresanitaire/default.jpg";

    private static final long START_AT = 500000L;
    private static final AtomicLong LAST_STRUCTURE_ID = new AtomicLong(START_AT);

    // ==================== CRÉATION ====================

    @Transactional
    public StructureSanitaireDto create(
            StructureSanitaireDto dto,
            MultipartFile photo,
            MultipartFile document
    ) throws IOException {
        validateStructureSanitaireDto(dto);

        String email = dto.getEmail().trim().toLowerCase();
        String phone = dto.getNumeroTelephone().trim();

        if (utilisateurRepository.findByEmail(email).isPresent()
                || medecinRepository.findByEmail(email).isPresent()
                || repository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }

        if (repository.existsByNumeroTelephone(phone)) {
            throw new IllegalArgumentException("Ce numéro de téléphone est déjà utilisé.");
        }

        StructureSanitaire structureSanitaire = mapper.toEntity(dto);

        if (structureSanitaire.getId() == null || structureSanitaire.getId().isBlank()) {
            structureSanitaire.setId(generateStructureId());
        }

        structureSanitaire.setEmail(email);
        structureSanitaire.setNumeroTelephone(phone);

        String tempPassword = "TEMP_" + UUID.randomUUID();
        structureSanitaire.setMotDePasse(passwordEncoder.encode(tempPassword));

        if (photo != null && !photo.isEmpty()) {
            structureSanitaire.setPhotoPath(savePhoto(photo));
        } else if (structureSanitaire.getPhotoPath() == null || structureSanitaire.getPhotoPath().isBlank()) {
            structureSanitaire.setPhotoPath(DEFAULT_PHOTO_PATH);
        }

        if (document != null && !document.isEmpty()) {
            structureSanitaire.setUrldocument(saveDocument(document));
        } else if (dto.getUrldocument() != null && !dto.getUrldocument().isBlank()) {
            structureSanitaire.setUrldocument(dto.getUrldocument().trim());
        }

        structureSanitaire.setRefSpecialites(normalizeSpecialites(dto.getRefSpecialites()));
        structureSanitaire.setRole(Role.STRUCTURESANITAIRE);
        structureSanitaire.setStatut(Statut.SUSPENDU);
        structureSanitaire.setActif(false);

        StructureSanitaire saved = repository.save(structureSanitaire);

        validationService.enregisterStructure(saved);

        return mapper.toDto(saved);
    }

    // ==================== ID GÉNÉRATEUR ====================

    @PostConstruct
    public void initStructureLastId() {
        long max = START_AT;

        for (StructureSanitaire structure : repository.findAll()) {
            try {
                String idStr = structure.getId();

                if (idStr != null && idStr.matches("\\d{6}")) {
                    long value = Long.parseLong(idStr);

                    if (value > max) {
                        max = value;
                    }
                }
            } catch (NumberFormatException ignored) {
                // Ignore les IDs non numériques.
            }
        }

        LAST_STRUCTURE_ID.set(max);
    }

    private String generateStructureId() {
        String id;

        do {
            long next = LAST_STRUCTURE_ID.incrementAndGet();
            id = String.format("%06d", next);
        } while (repository.existsById(id));

        return id;
    }

    // ==================== ACTIVATION EMAIL ====================

    @Transactional
    public void activation(Map<String, String> activation) {
        String code = activation.get("code");

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code d'activation manquant.");
        }

        Validation validation = validationService.lireEnFonctionDuCode(code);

        if (validation == null) {
            throw new RuntimeException("Code d'activation invalide.");
        }

        if (Instant.now().isAfter(validation.getExpiration())) {
            throw new RuntimeException("Votre code a expiré");
        }

        StructureSanitaire structureActiver = repository.findById(validation.getStructureSanitaire().getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur inconnu"));

        structureActiver.setActif(true);
        repository.save(structureActiver);

        notificationService.envoyerAccuseEnregistrementStructure(
                structureActiver.getEmail(),
                structureActiver.getNomStructureSanitaire()
        );
    }

    // ==================== SPRING SECURITY ====================

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Aucun utilisateur ne correspond à cet identifiant"
                ));
    }

    // ==================== LECTURE ====================

    public StructureSanitaireDto findById(String id) {
        StructureSanitaire structureSanitaire = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Structure sanitaire non trouvée"));

        return mapper.toDto(structureSanitaire);
    }

    public StructureSanitaireDto getProfileById(String id) {
        StructureSanitaire structure = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Structure sanitaire non trouvée"));

        return mapper.toDto(structure);
    }

    public StructureSanitaireDto getMyProfile() {
        String emailConnecte = SecurityContextHolder.getContext().getAuthentication().getName();

        StructureSanitaire structure = repository.findByEmail(emailConnecte)
                .orElseThrow(() -> new RuntimeException("Structure introuvable pour l'email connecté"));

        return mapper.toDto(structure);
    }

    public List<StructureSanitaireDto> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public List<StructureSanitaireDto> findBySpecialite(String specialite) {
        return repository.findBySpecialite(specialite)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public List<StructureSanitaireDto> findByVille(String ville) {
        return repository.findByVilleIgnoreCase(ville)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public List<StructureSanitaireDto> findByVilleAndSpecialite(String ville, String specialite) {
        return repository.findByVilleAndSpecialite(ville, specialite)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public List<StructureSanitaireDto> searchByNom(String nom) {
        if (nom == null || nom.isBlank()) {
            return Collections.emptyList();
        }

        String searchTerm = nom.trim().toLowerCase();

        List<StructureSanitaire> structures = repository.findAll()
                .stream()
                .filter(StructureSanitaire::isActif)
                .filter(structure -> structure.getNomStructureSanitaire() != null
                        && structure.getNomStructureSanitaire().toLowerCase().contains(searchTerm))
                .limit(10)
                .collect(Collectors.toList());

        return structures.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public Set<String> getSpecialitesStructure(String id) {
        StructureSanitaire structure = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        return structure.getRefSpecialites() != null
                ? structure.getRefSpecialites()
                : Collections.emptySet();
    }

    public Set<String> getMySpecialites() {
        String emailConnecte = SecurityContextHolder.getContext().getAuthentication().getName();

        StructureSanitaire structure = repository.findByEmail(emailConnecte)
                .orElseThrow(() -> new RuntimeException("Structure introuvable pour l'email connecté"));

        return structure.getRefSpecialites() != null
                ? structure.getRefSpecialites()
                : Collections.emptySet();
    }

    public Set<String> getToutesLesSpecialitesUtilisees() {
        return repository.findAll()
                .stream()
                .map(StructureSanitaire::getRefSpecialites)
                .filter(Objects::nonNull)
                .flatMap(Set::stream)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(specialite -> !specialite.isEmpty())
                .collect(Collectors.toSet());
    }

    // ==================== MISE À JOUR ====================

    @Transactional
    public StructureSanitaireDto update(
            String id,
            StructureSanitaireDto dto,
            MultipartFile photo
    ) throws IOException {
        StructureSanitaire structureSanitaire = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Structure sanitaire non trouvée"));

        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(structureSanitaire.getEmail())) {
            String email = dto.getEmail().trim().toLowerCase();

            if (repository.existsByEmailAndIdNot(email, structureSanitaire.getId())
                    || utilisateurRepository.findByEmail(email).isPresent()
                    || medecinRepository.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("Cet email est déjà utilisé.");
            }

            structureSanitaire.setEmail(email);
        }

        if (dto.getNumeroTelephone() != null
                && !dto.getNumeroTelephone().equalsIgnoreCase(structureSanitaire.getNumeroTelephone())) {
            String phone = dto.getNumeroTelephone().trim();

            if (repository.existsByNumeroTelephoneAndIdNot(phone, structureSanitaire.getId())) {
                throw new IllegalArgumentException("Ce numéro de téléphone est déjà utilisé.");
            }

            structureSanitaire.setNumeroTelephone(phone);
        }

        if (dto.getNomStructureSanitaire() != null) {
            structureSanitaire.setNomStructureSanitaire(dto.getNomStructureSanitaire());
        }

        if (dto.getAdresse() != null) {
            structureSanitaire.setAdresse(dto.getAdresse());
        }

        if (dto.getVille() != null) {
            structureSanitaire.setVille(dto.getVille());
        }

        if (dto.getRefType() != null) {
            structureSanitaire.setRefType(dto.getRefType());
        }

        if (dto.getGpsLatitude() != null) {
            structureSanitaire.setGpsLatitude(dto.getGpsLatitude());
        }

        if (dto.getGpsLongitude() != null) {
            structureSanitaire.setGpsLongitude(dto.getGpsLongitude());
        }

        if (dto.getRefSpecialites() != null) {
            structureSanitaire.setRefSpecialites(normalizeSpecialites(dto.getRefSpecialites()));
        }

        if (photo != null && !photo.isEmpty()) {
            structureSanitaire.setPhotoPath(savePhoto(photo));
        }

        StructureSanitaire saved = repository.save(structureSanitaire);

        return mapper.toDto(saved);
    }

    @Transactional
    public StructureSanitaireDto updateMyProfile(
            StructureSanitaireDto dto,
            MultipartFile photo
    ) throws IOException {
        String emailConnecte = SecurityContextHolder.getContext().getAuthentication().getName();

        StructureSanitaire me = repository.findByEmail(emailConnecte)
                .orElseThrow(() -> new RuntimeException("Structure introuvable pour l'email connecté"));

        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(me.getEmail())) {
            String email = dto.getEmail().trim().toLowerCase();

            if (repository.existsByEmailAndIdNot(email, me.getId())
                    || utilisateurRepository.findByEmail(email).isPresent()
                    || medecinRepository.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("Cet email est déjà utilisé.");
            }

            me.setEmail(email);
        }

        if (dto.getNumeroTelephone() != null && !dto.getNumeroTelephone().equalsIgnoreCase(me.getNumeroTelephone())) {
            String phone = dto.getNumeroTelephone().trim();

            if (repository.existsByNumeroTelephoneAndIdNot(phone, me.getId())) {
                throw new IllegalArgumentException("Ce numéro de téléphone est déjà utilisé.");
            }

            me.setNumeroTelephone(phone);
        }

        if (dto.getNomStructureSanitaire() != null) {
            me.setNomStructureSanitaire(dto.getNomStructureSanitaire());
        }

        if (dto.getAdresse() != null) {
            me.setAdresse(dto.getAdresse());
        }

        if (dto.getVille() != null) {
            me.setVille(dto.getVille());
        }

        if (dto.getRefType() != null) {
            me.setRefType(dto.getRefType());
        }

        if (dto.getGpsLatitude() != null) {
            me.setGpsLatitude(dto.getGpsLatitude());
        }

        if (dto.getGpsLongitude() != null) {
            me.setGpsLongitude(dto.getGpsLongitude());
        }

        if (dto.getRefSpecialites() != null) {
            me.setRefSpecialites(normalizeSpecialites(dto.getRefSpecialites()));
        }

        if (photo != null && !photo.isEmpty()) {
            me.setPhotoPath(savePhoto(photo));
        }

        StructureSanitaire saved = repository.save(me);

        return mapper.toDto(saved);
    }

    @Transactional
    public StructureSanitaireDto updateGpsById(String id, Float lat, Float lon) {
        StructureSanitaire structureSanitaire = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        applyAndValidateGps(structureSanitaire, lat, lon);
        repository.save(structureSanitaire);

        return mapper.toDto(structureSanitaire);
    }

    @Transactional
    public StructureSanitaireDto updateMyGps(Float lat, Float lon) {
        String emailConnecte = SecurityContextHolder.getContext().getAuthentication().getName();

        StructureSanitaire me = repository.findByEmail(emailConnecte)
                .orElseThrow(() -> new RuntimeException("Structure introuvable pour l'email connecté"));

        verifierAccesStructure(me.getId());

        applyAndValidateGps(me, lat, lon);
        repository.save(me);

        return mapper.toDto(me);
    }

    // ==================== SUPPRESSION ====================

    @Transactional
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    // ==================== MOT DE PASSE ====================

    @Transactional
    public void updatePasswordByEmail(ChangementMotDePasseDto dto) {
        String nouveauMotDePasse = dto.getNouveauMotDePasse();
        String confirmationMotDePasse = lireConfirmationMotDePasse(dto);

        if (nouveauMotDePasse == null || nouveauMotDePasse.isBlank()
                || confirmationMotDePasse == null || confirmationMotDePasse.isBlank()) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire.");
        }

        if (!nouveauMotDePasse.equals(confirmationMotDePasse)) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        }

        StructureSanitaire structureSanitaire = repository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur avec cet email non trouvé"));

        structureSanitaire.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));

        repository.save(structureSanitaire);
    }

    private String lireConfirmationMotDePasse(ChangementMotDePasseDto dto) {
        try {
            Method method = dto.getClass().getMethod("getConfirmationMotDePasse");
            Object value = method.invoke(dto);
            return value != null ? value.toString() : null;
        } catch (ReflectiveOperationException ignored) {
            // Compatibilité avec l'ancien nom.
        }

        try {
            Method method = dto.getClass().getMethod("getConfirmerMotDePasse");
            Object value = method.invoke(dto);
            return value != null ? value.toString() : null;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(
                    "Aucune méthode de confirmation de mot de passe trouvée dans ChangementMotDePasseDto.",
                    ex
            );
        }
    }

    // ==================== ACTIVATION ADMIN ====================

    @Transactional
    public String adminActiverEtReinitialiserMdp(String id) {
        StructureSanitaire structureSanitaire = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        if (Statut.ACTIF == structureSanitaire.getStatut()) {
            throw new IllegalStateException("Cette structure est déjà active");
        }

        if (!structureSanitaire.isActif()) {
            throw new IllegalStateException("La structure n'a pas encore confirmé son email");
        }

        String plainPassword = generateStrongPassword(14);

        structureSanitaire.setMotDePasse(passwordEncoder.encode(plainPassword));
        structureSanitaire.setActif(true);
        structureSanitaire.setStatut(Statut.ACTIF);

        repository.save(structureSanitaire);

        notificationService.envoyerIdentifiantsStructure(
                structureSanitaire.getEmail(),
                structureSanitaire.getNomStructureSanitaire(),
                structureSanitaire.getId(),
                plainPassword
        );

        return plainPassword;
    }

    // ==================== SPÉCIALITÉS ====================

    @Transactional
    public Set<String> addSpecialites(String structureId, Set<String> toAdd) {
        StructureSanitaire structureSanitaire = repository.findById(structureId)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        verifierAccesStructure(structureSanitaire.getId());

        if (structureSanitaire.getRefSpecialites() == null) {
            structureSanitaire.setRefSpecialites(new HashSet<>());
        }

        Set<String> normalized = normalizeSpecialites(toAdd);

        structureSanitaire.getRefSpecialites().addAll(normalized);
        repository.save(structureSanitaire);

        return structureSanitaire.getRefSpecialites();
    }

    @Transactional
    public Set<String> archiveSpecialites(String structureId, Set<String> toArchive) {
        if (structureId == null || structureId.isBlank()) {
            throw new IllegalArgumentException("structureId requis");
        }

        if (toArchive == null || toArchive.isEmpty()) {
            return Collections.emptySet();
        }

        StructureSanitaire structureSanitaire = repository.findById(structureId)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        verifierAccesStructure(structureSanitaire.getId());

        if (structureSanitaire.getRefSpecialites() == null) {
            structureSanitaire.setRefSpecialites(new HashSet<>());
        }

        if (structureSanitaire.getArchivedSpecialites() == null) {
            structureSanitaire.setArchivedSpecialites(new HashSet<>());
        }

        Set<String> normalized = normalizeSpecialites(toArchive);

        boolean modified = false;

        for (String requested : normalized) {
            Optional<String> present = structureSanitaire.getRefSpecialites()
                    .stream()
                    .filter(specialite -> specialite != null && specialite.trim().equalsIgnoreCase(requested))
                    .findFirst();

            if (present.isPresent()) {
                String original = present.get();
                structureSanitaire.getRefSpecialites().remove(original);
                structureSanitaire.getArchivedSpecialites().add(original);
            } else {
                structureSanitaire.getArchivedSpecialites().add(requested);
            }

            modified = true;
        }

        if (modified) {
            repository.save(structureSanitaire);
        }

        return structureSanitaire.getArchivedSpecialites();
    }

    @Transactional
    public Set<String> restoreSpecialites(String structureId, Set<String> toRestore) {
        if (structureId == null || structureId.isBlank()) {
            throw new IllegalArgumentException("structureId requis");
        }

        if (toRestore == null || toRestore.isEmpty()) {
            return Collections.emptySet();
        }

        StructureSanitaire structureSanitaire = repository.findById(structureId)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        verifierAccesStructure(structureSanitaire.getId());

        if (structureSanitaire.getRefSpecialites() == null) {
            structureSanitaire.setRefSpecialites(new HashSet<>());
        }

        if (structureSanitaire.getArchivedSpecialites() == null) {
            structureSanitaire.setArchivedSpecialites(new HashSet<>());
        }

        Set<String> normalized = normalizeSpecialites(toRestore);

        boolean modified = false;

        for (String requested : normalized) {
            Optional<String> present = structureSanitaire.getArchivedSpecialites()
                    .stream()
                    .filter(specialite -> specialite != null && specialite.trim().equalsIgnoreCase(requested))
                    .findFirst();

            if (present.isPresent()) {
                String original = present.get();
                structureSanitaire.getArchivedSpecialites().remove(original);
                structureSanitaire.getRefSpecialites().add(original);
                modified = true;
            }
        }

        if (modified) {
            repository.save(structureSanitaire);
        }

        return structureSanitaire.getRefSpecialites();
    }

    @Transactional(readOnly = true)
    public Set<String> getArchivedSpecialites(String structureId) {
        StructureSanitaire structureSanitaire = repository.findById(structureId)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        return structureSanitaire.getArchivedSpecialites() == null
                ? Collections.emptySet()
                : structureSanitaire.getArchivedSpecialites();
    }

    // ==================== FICHIERS ====================

    private String savePhoto(MultipartFile file) throws IOException {
        String contentType = file.getContentType();

        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Le fichier photo doit être JPEG ou PNG.");
        }

        String name = System.currentTimeMillis() + "_" + Objects.requireNonNull(file.getOriginalFilename());
        Path dir = Paths.get(uploadDirStructure);

        Files.createDirectories(dir);
        Files.write(dir.resolve(name), file.getBytes());

        return "/uploads/structuresanitaire/" + name;
    }

    private String saveDocument(MultipartFile file) throws IOException {
        String contentType = file.getContentType();

        if (contentType == null
                || !(contentType.equals("application/pdf")
                || contentType.equals("image/jpeg")
                || contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Le document doit être un PDF, JPEG ou PNG.");
        }

        String name = System.currentTimeMillis() + "_" + Objects.requireNonNull(file.getOriginalFilename());
        Path dir = Paths.get(uploadDirDocs);

        Files.createDirectories(dir);
        Files.write(dir.resolve(name), file.getBytes());

        return "/uploads/documentStructure/" + name;
    }

    // ==================== VALIDATION ====================

    private void validateStructureSanitaireDto(StructureSanitaireDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Les informations de la structure sont obligatoires.");
        }

        if (dto.getNomStructureSanitaire() == null || dto.getNomStructureSanitaire().isBlank()) {
            throw new IllegalArgumentException("Le nom de la structure sanitaire ne peut pas être vide.");
        }

        if (dto.getAdresse() == null || dto.getAdresse().isBlank()) {
            throw new IllegalArgumentException("L'adresse de la structure sanitaire ne peut pas être vide.");
        }

        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("L'email de la structure sanitaire ne peut pas être vide.");
        }

        if (dto.getNumeroTelephone() == null || dto.getNumeroTelephone().isBlank()) {
            throw new IllegalArgumentException("Le numéro de téléphone de la structure sanitaire ne peut pas être vide.");
        }
    }

    private Set<String> normalizeSpecialites(Set<String> input) {
        if (input == null) {
            return new LinkedHashSet<>();
        }

        return input.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(specialite -> !specialite.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // ==================== GPS ====================

    private void applyAndValidateGps(StructureSanitaire structureSanitaire, Float lat, Float lon) {
        if (lat == null && lon == null) {
            throw new IllegalArgumentException("gpsLatitude ou gpsLongitude doit être fourni.");
        }

        if (lat != null) {
            validateLatitude(lat);
            structureSanitaire.setGpsLatitude(lat);
        }

        if (lon != null) {
            validateLongitude(lon);
            structureSanitaire.setGpsLongitude(lon);
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

    // ==================== MOT DE PASSE FORT ====================

    private String generateStrongPassword(int length) {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "@$!%*?&";
        String all = upper + lower + digits + special;

        SecureRandom random = new SecureRandom();
        char[] password = new char[length];

        password[0] = upper.charAt(random.nextInt(upper.length()));
        password[1] = lower.charAt(random.nextInt(lower.length()));
        password[2] = digits.charAt(random.nextInt(digits.length()));
        password[3] = special.charAt(random.nextInt(special.length()));

        for (int i = 4; i < length; i++) {
            password[i] = all.charAt(random.nextInt(all.length()));
        }

        for (int i = length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = password[i];
            password[i] = password[j];
            password[j] = temp;
        }

        return new String(password);
    }

    // ==================== ABONNEMENT ====================

    private void verifierAccesStructure(String structureId) {
        if (structureId == null || structureId.isBlank()) {
            throw new IllegalArgumentException("structureId est obligatoire pour vérifier l'abonnement.");
        }

        abonnementStructureService.verifierAccesAbonnement(structureId);
    }
}