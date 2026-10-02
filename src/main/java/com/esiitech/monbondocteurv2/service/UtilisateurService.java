package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.ActivationRequest;
import com.esiitech.monbondocteurv2.dto.ChangementMotDePasseDto;
import com.esiitech.monbondocteurv2.dto.UtilisateurDto;
import com.esiitech.monbondocteurv2.enums.Role;
import com.esiitech.monbondocteurv2.enums.StatutCompte;
import com.esiitech.monbondocteurv2.mapper.UtilisateurMapper;
import com.esiitech.monbondocteurv2.model.Utilisateur;
import com.esiitech.monbondocteurv2.model.Validation;
import com.esiitech.monbondocteurv2.repository.UtilisateurRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UtilisateurService implements UserDetailsService {

    private final UtilisateurRepository repository;
    private final UtilisateurMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final ValidationService validationService;
    private final NotificationService notificationService;

    @Value("${app.upload.dir.utilisateurs}")
    private String uploadDir;

    private static final String DEFAULT_PHOTO_PATH =
            "/uploads/utilisateurs/default.jpg";

    private static final String PASSWORD_CHARACTERS =
            "ABCDEFGHJKLMNPQRSTUVWXYZ" +
                    "abcdefghijkmnopqrstuvwxyz" +
                    "23456789" ;
    private static final int PASSWORD_LENGTH = 12;

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    // =====================================================
    // CONSTRUCTEUR
    // =====================================================

    public UtilisateurService(
            UtilisateurRepository repository,
            UtilisateurMapper mapper,
            PasswordEncoder passwordEncoder,
            ValidationService validationService,
            NotificationService notificationService
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.validationService = validationService;
        this.notificationService = notificationService;
    }

    // =====================================================
    // CRÉATION UTILISATEUR
    // MOT DE PASSE GÉNÉRÉ AUTOMATIQUEMENT
    // COMPTE DIRECTEMENT ACTIF
    // =====================================================

    @Transactional
    public UtilisateurDto createUser(
            UtilisateurDto dto,
            MultipartFile photo
    ) throws IOException {

        validateUtilisateurDto(dto);

        // Vérifier que l'adresse email n'est pas déjà utilisée
        if (repository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException(
                    "Un utilisateur avec cet email existe déjà."
            );
        }

        // Conversion DTO -> Entity
        Utilisateur utilisateur = mapper.toEntity(dto);

        // Génération de l'identifiant utilisateur
        utilisateur.setId(generateUserId());

        // Génération du mot de passe temporaire
        String motDePassePlain = generatePassword();

        // IMPORTANT :
        // seul le hash est enregistré dans la base de données
        utilisateur.setMotDePasse(
                passwordEncoder.encode(motDePassePlain)
        );

        // Le compte est directement actif
        utilisateur.setStatutCompte(StatutCompte.ACTIF);
        utilisateur.setActif(true);

        // Rôle choisi par l'administrateur
        // USER par défaut si aucun rôle n'est fourni
        utilisateur.setRole(
                dto.getRole() != null
                        ? dto.getRole()
                        : Role.USER
        );

        // Photo utilisateur
        utilisateur.setPhotoPath(
                savePhotoOrDefault(photo)
        );

        // Sauvegarde en base
        Utilisateur saved = repository.save(utilisateur);

        // Envoi des identifiants par email
        notificationService.envoyerIdentifiantsUtilisateur(
                saved.getEmail(),
                saved.getPrenom() + " " + saved.getNom(),
                saved.getEmail(),
                motDePassePlain
        );

        return mapper.toDto(saved);
    }

    // =====================================================
    // ANCIENNE CRÉATION PAR INVITATION
    // CONSERVÉE SI TU EN AS ENCORE BESOIN
    // =====================================================

    @Transactional
    public UtilisateurDto createUserByInvitation(
            UtilisateurDto dto,
            MultipartFile photo
    ) throws IOException {

        validateUtilisateurDto(dto);

        if (repository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException(
                    "Un utilisateur avec cet email existe déjà."
            );
        }

        Utilisateur utilisateur = mapper.toEntity(dto);

        utilisateur.setId(generateUserId());

        utilisateur.setMotDePasse(null);

        utilisateur.setStatutCompte(
                StatutCompte.INVITE
        );

        utilisateur.setActif(false);

        utilisateur.setRole(
                dto.getRole() != null
                        ? dto.getRole()
                        : Role.USER
        );

        utilisateur.setPhotoPath(
                savePhotoOrDefault(photo)
        );

        Utilisateur saved =
                repository.save(utilisateur);

        // Génération et envoi OTP
        validationService.enregister(saved);

        return mapper.toDto(saved);
    }

    // =====================================================
    // ANCIENNE MÉTHODE SAVE
    // CONSERVÉE POUR COMPATIBILITÉ
    // =====================================================

    @Transactional
    public UtilisateurDto save(
            UtilisateurDto dto,
            MultipartFile photo
    ) throws IOException {

        return createUser(dto, photo);
    }

    // =====================================================
    // GÉNÉRATION ID UTILISATEUR
    // =====================================================

    private String generateUserId() {

        return "user-" + UUID.randomUUID();
    }

    // =====================================================
    // GÉNÉRATION MOT DE PASSE
    // =====================================================

    private String generatePassword() {

        StringBuilder password =
                new StringBuilder(PASSWORD_LENGTH);

        for (int i = 0; i < PASSWORD_LENGTH; i++) {

            int index = SECURE_RANDOM.nextInt(
                    PASSWORD_CHARACTERS.length()
            );

            password.append(
                    PASSWORD_CHARACTERS.charAt(index)
            );
        }

        return password.toString();
    }

    // =====================================================
    // ACTIVATION AVEC CODE SIMPLE
    // ANCIEN SYSTÈME
    // =====================================================

    @Transactional
    public void activation(
            Map<String, String> activation
    ) {

        String code = activation.get("code");

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Code d'activation manquant."
            );
        }

        Validation validation =
                validationService.lireEnFonctionDuCode(code);

        if (validation == null) {
            throw new RuntimeException(
                    "Code invalide"
            );
        }

        if (Instant.now().isAfter(
                validation.getExpiration()
        )) {
            throw new RuntimeException(
                    "Votre code a expiré"
            );
        }

        Utilisateur utilisateur =
                repository
                        .findById(
                                validation
                                        .getUtilisateur()
                                        .getId()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Utilisateur inconnu"
                                )
                        );

        utilisateur.setActif(true);

        utilisateur.setStatutCompte(
                StatutCompte.ACTIF
        );

        repository.save(utilisateur);
    }

    // =====================================================
    // ACTIVATION AVEC CODE + MOT DE PASSE
    // ANCIEN SYSTÈME INVITATION
    // =====================================================

    @Transactional
    public void activation(
            ActivationRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "La demande d'activation est obligatoire."
            );
        }

        String motDePasse =
                request.getMotDePasse();

        String confirmationMotDePasse =
                lireConfirmationMotDePasse(request);

        if (motDePasse == null
                || motDePasse.isBlank()
                || confirmationMotDePasse == null
                || confirmationMotDePasse.isBlank()) {

            throw new IllegalArgumentException(
                    "Le mot de passe est obligatoire."
            );
        }

        if (!motDePasse.equals(
                confirmationMotDePasse
        )) {

            throw new IllegalArgumentException(
                    "Les mots de passe ne correspondent pas"
            );
        }

        Validation validation =
                validationService
                        .lireEnFonctionDuCode(
                                request.getCode()
                        );

        if (validation == null) {
            throw new RuntimeException(
                    "Code invalide"
            );
        }

        if (Instant.now().isAfter(
                validation.getExpiration()
        )) {

            throw new IllegalArgumentException(
                    "Code expiré"
            );
        }

        Utilisateur utilisateur =
                repository
                        .findById(
                                validation
                                        .getUtilisateur()
                                        .getId()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Utilisateur introuvable"
                                )
                        );

        utilisateur.setMotDePasse(
                passwordEncoder.encode(
                        motDePasse
                )
        );

        utilisateur.setStatutCompte(
                StatutCompte.ACTIF
        );

        utilisateur.setActif(true);

        repository.save(utilisateur);
    }

    // =====================================================
    // UPDATE UTILISATEUR
    // =====================================================

    @Transactional
    public UtilisateurDto update(
            String id,
            UtilisateurDto dto
    ) {

        Utilisateur utilisateur =
                repository
                        .findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Utilisateur non trouvé"
                                )
                        );

        if (dto.getNom() != null) {
            utilisateur.setNom(
                    dto.getNom()
            );
        }

        if (dto.getPrenom() != null) {
            utilisateur.setPrenom(
                    dto.getPrenom()
            );
        }

        if (dto.getEmail() != null) {

            // Vérifier uniquement si l'adresse change
            if (!dto.getEmail().equals(
                    utilisateur.getEmail()
            )) {

                repository
                        .findByEmail(dto.getEmail())
                        .ifPresent(existing -> {

                            if (!existing
                                    .getId()
                                    .equals(id)) {

                                throw new IllegalArgumentException(
                                        "Un utilisateur avec cet email existe déjà."
                                );
                            }
                        });
            }

            utilisateur.setEmail(
                    dto.getEmail()
            );
        }

        if (dto.getRole() != null) {
            utilisateur.setRole(
                    dto.getRole()
            );
        }

        if (dto.getNumeroTelephone() != null) {
            utilisateur.setNumeroTelephone(
                    dto.getNumeroTelephone()
            );
        }

        if (dto.getSexe() != null) {
            utilisateur.setSexe(
                    dto.getSexe()
            );
        }

        Utilisateur updated =
                repository.save(utilisateur);

        return mapper.toDto(updated);
    }

    // =====================================================
    // SUSPENDRE UTILISATEUR
    // =====================================================

    @Transactional
    public void suspendUser(String id) {

        Utilisateur utilisateur =
                repository
                        .findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Utilisateur introuvable"
                                )
                        );

        utilisateur.setStatutCompte(
                StatutCompte.SUSPENDU
        );

        utilisateur.setActif(false);

        repository.save(utilisateur);
    }

    // =====================================================
    // ACTIVER UTILISATEUR
    // =====================================================

    @Transactional
    public void activateUser(String id) {

        Utilisateur utilisateur =
                repository
                        .findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Utilisateur introuvable"
                                )
                        );

        utilisateur.setStatutCompte(
                StatutCompte.ACTIF
        );

        utilisateur.setActif(true);

        repository.save(utilisateur);
    }

    // =====================================================
    // MODIFICATION MOT DE PASSE
    // =====================================================

    @Transactional
    public void updatePasswordByEmail(
            ChangementMotDePasseDto dto
    ) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Les informations sont obligatoires."
            );
        }

        String nouveauMotDePasse =
                dto.getNouveauMotDePasse();

        String confirmationMotDePasse =
                lireConfirmationMotDePasse(dto);

        if (nouveauMotDePasse == null
                || nouveauMotDePasse.isBlank()
                || confirmationMotDePasse == null
                || confirmationMotDePasse.isBlank()) {

            throw new IllegalArgumentException(
                    "Le mot de passe est obligatoire."
            );
        }

        if (!nouveauMotDePasse.equals(
                confirmationMotDePasse
        )) {

            throw new IllegalArgumentException(
                    "Les mots de passe ne correspondent pas."
            );
        }

        Utilisateur utilisateur =
                repository
                        .findByEmail(dto.getEmail())
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Utilisateur avec cet email non trouvé"
                                )
                        );

        utilisateur.setMotDePasse(
                passwordEncoder.encode(
                        nouveauMotDePasse
                )
        );

        repository.save(utilisateur);
    }

    // =====================================================
    // RÉCUPÉRER UTILISATEUR PAR ID
    // =====================================================

    public UtilisateurDto findById(String id) {

        Utilisateur utilisateur =
                repository
                        .findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Utilisateur non trouvé"
                                )
                        );

        return mapper.toDto(utilisateur);
    }

    // =====================================================
    // RÉCUPÉRER TOUS LES UTILISATEURS
    // =====================================================

    public List<UtilisateurDto> findAll() {

        return repository
                .findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public Iterable<UtilisateurDto> getAllUsers() {

        return repository
                .findAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    // =====================================================
    // RÉCUPÉRER PAR EMAIL
    // =====================================================

    public Utilisateur findByEmail(
            String email
    ) {

        return repository
                .findByEmail(email)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Utilisateur introuvable"
                        )
                );
    }

    // =====================================================
    // SUPPRESSION
    // =====================================================

    @Transactional
    public void deleteByEmail(
            String email
    ) {

        Utilisateur utilisateur =
                repository
                        .findByEmail(email)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Utilisateur avec cet email non trouvé"
                                )
                        );

        repository.delete(utilisateur);
    }

    // =====================================================
    // PHOTO
    // =====================================================

    private String savePhotoOrDefault(
            MultipartFile photo
    ) throws IOException {

        if (photo == null || photo.isEmpty()) {
            return DEFAULT_PHOTO_PATH;
        }

        return savePhoto(photo);
    }

    private String savePhoto(
            MultipartFile photo
    ) throws IOException {

        if (photo == null || photo.isEmpty()) {
            return DEFAULT_PHOTO_PATH;
        }

        String contentType =
                photo.getContentType();

        if (contentType == null
                || (!contentType.equals("image/jpeg")
                && !contentType.equals("image/png"))) {

            throw new IllegalArgumentException(
                    "Le fichier doit être une image JPEG ou PNG."
            );
        }

        String originalFilename =
                photo.getOriginalFilename();

        if (originalFilename == null
                || originalFilename.isBlank()) {

            originalFilename =
                    "utilisateur-photo";
        }

        String photoName =
                System.currentTimeMillis()
                        + "_"
                        + originalFilename;

        Path path =
                Paths.get(
                        uploadDir,
                        photoName
                );

        Files.createDirectories(
                path.getParent()
        );

        Files.write(
                path,
                photo.getBytes()
        );

        return "/uploads/utilisateurs/"
                + photoName;
    }

    // =====================================================
    // VALIDATION DTO
    // =====================================================

    private void validateUtilisateurDto(
            UtilisateurDto dto
    ) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Les informations de l'utilisateur sont obligatoires."
            );
        }

        if (dto.getEmail() == null
                || dto.getEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "L'email de l'utilisateur ne peut pas être vide."
            );
        }

        if (dto.getNom() == null
                || dto.getNom().isBlank()) {

            throw new IllegalArgumentException(
                    "Le nom de l'utilisateur ne peut pas être vide."
            );
        }

        if (dto.getPrenom() == null
                || dto.getPrenom().isBlank()) {

            throw new IllegalArgumentException(
                    "Le prénom de l'utilisateur ne peut pas être vide."
            );
        }
    }

    // =====================================================
    // COMPATIBILITÉ CONFIRMATION MOT DE PASSE
    // =====================================================

    private String lireConfirmationMotDePasse(
            Object dto
    ) {

        try {

            Method method =
                    dto.getClass()
                            .getMethod(
                                    "getConfirmationMotDePasse"
                            );

            Object value =
                    method.invoke(dto);

            return value != null
                    ? value.toString()
                    : null;

        } catch (ReflectiveOperationException ignored) {
            // Tentative avec ancien nom
        }

        try {

            Method method =
                    dto.getClass()
                            .getMethod(
                                    "getConfirmerMotDePasse"
                            );

            Object value =
                    method.invoke(dto);

            return value != null
                    ? value.toString()
                    : null;

        } catch (ReflectiveOperationException ex) {

            throw new IllegalStateException(
                    "Aucune méthode de confirmation de mot de passe trouvée dans le DTO.",
                    ex
            );
        }
    }

    // =====================================================
    // SPRING SECURITY
    // =====================================================

    @Override
    public UserDetails loadUserByUsername(
            String username
    ) throws UsernameNotFoundException {

        return repository
                .findByEmail(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException(
                                "Aucun utilisateur ne correspond à cet identifiant"
                        )
                );
    }
}