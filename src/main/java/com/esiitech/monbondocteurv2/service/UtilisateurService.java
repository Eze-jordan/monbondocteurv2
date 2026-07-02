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

    private static final String DEFAULT_PHOTO_PATH = "/uploads/utilisateurs/default.jpg";

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
    // CRÉATION UTILISATEUR AVEC MOT DE PASSE
    // Ancienne méthode conservée pour compatibilité.
    // =====================================================

    @Transactional
    public UtilisateurDto save(UtilisateurDto dto, MultipartFile photo) throws IOException {
        validateUtilisateurDto(dto);

        if (dto.getRole() == null) {
            dto.setRole(Role.USER);
        }

        Utilisateur utilisateur = mapper.toEntity(dto);

        if (utilisateur.getId() == null || utilisateur.getId().isBlank()) {
            utilisateur.setId(generateUserId());
        }


        utilisateur.setRole(dto.getRole());
        utilisateur.setStatutCompte(StatutCompte.INVITE);
        utilisateur.setActif(false);

        if (photo != null && !photo.isEmpty()) {
            utilisateur.setPhotoPath(savePhoto(photo));
        } else if (utilisateur.getPhotoPath() == null || utilisateur.getPhotoPath().isBlank()) {
            utilisateur.setPhotoPath(DEFAULT_PHOTO_PATH);
        }

        Utilisateur saved = repository.save(utilisateur);

        validationService.enregister(saved);

        return mapper.toDto(saved);
    }

    // =====================================================
    // CRÉATION UTILISATEUR PAR INVITATION
    // Nouvelle méthode conservée.
    // =====================================================

    @Transactional
    public UtilisateurDto createUser(UtilisateurDto dto, MultipartFile photo) throws IOException {
        validateUtilisateurDto(dto);

        Utilisateur utilisateur = mapper.toEntity(dto);

        utilisateur.setId(generateUserId());
        utilisateur.setMotDePasse(null);
        utilisateur.setStatutCompte(StatutCompte.INVITE);
        utilisateur.setActif(false);
        utilisateur.setRole(dto.getRole() == null ? Role.USER : dto.getRole());
        utilisateur.setPhotoPath(savePhotoOrDefault(photo));

        Utilisateur saved = repository.save(utilisateur);

        validationService.enregister(saved);

        return mapper.toDto(saved);
    }

    private String generateUserId() {
        return "user-" + UUID.randomUUID();
    }

    // =====================================================
    // ACTIVATION AVEC CODE SIMPLE
    // Ancienne méthode conservée.
    // =====================================================

    @Transactional
    public void activation(Map<String, String> activation) {
        String code = activation.get("code");

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code d'activation manquant.");
        }

        Validation validation = validationService.lireEnFonctionDuCode(code);

        if (validation == null) {
            throw new RuntimeException("Code invalide");
        }

        if (Instant.now().isAfter(validation.getExpiration())) {
            throw new RuntimeException("Votre code a expiré");
        }

        Utilisateur utilisateurActiver = repository.findById(validation.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur inconnu"));

        utilisateurActiver.setActif(true);
        utilisateurActiver.setStatutCompte(StatutCompte.ACTIF);

        repository.save(utilisateurActiver);
    }

    // =====================================================
    // ACTIVATION AVEC CODE + MOT DE PASSE
    // Nouvelle méthode conservée.
    // =====================================================

    @Transactional
    public void activation(ActivationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La demande d'activation est obligatoire.");
        }

        String motDePasse = request.getMotDePasse();
        String confirmationMotDePasse = lireConfirmationMotDePasse(request);

        if (motDePasse == null || motDePasse.isBlank()
                || confirmationMotDePasse == null || confirmationMotDePasse.isBlank()) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire.");
        }

        if (!motDePasse.equals(confirmationMotDePasse)) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
        }

        Validation validation = validationService.lireEnFonctionDuCode(request.getCode());

        if (validation == null) {
            throw new RuntimeException("Code invalide");
        }

        if (Instant.now().isAfter(validation.getExpiration())) {
            throw new IllegalArgumentException("Code expiré");
        }

        Utilisateur utilisateur = repository.findById(validation.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        utilisateur.setMotDePasse(passwordEncoder.encode(motDePasse));
        utilisateur.setStatutCompte(StatutCompte.ACTIF);
        utilisateur.setActif(true);

        repository.save(utilisateur);
    }

    // =====================================================
    // UPDATE PROFIL
    // =====================================================

    @Transactional
    public UtilisateurDto update(String id, UtilisateurDto dto) {
        Utilisateur utilisateur = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (dto.getNom() != null) {
            utilisateur.setNom(dto.getNom());
        }

        if (dto.getPrenom() != null) {
            utilisateur.setPrenom(dto.getPrenom());
        }

        if (dto.getEmail() != null) {
            utilisateur.setEmail(dto.getEmail());
        }

        if (dto.getRole() != null) {
            utilisateur.setRole(dto.getRole());
        }


        Utilisateur updated = repository.save(utilisateur);

        return mapper.toDto(updated);
    }

    // =====================================================
    // ADMIN STATUS CONTROL
    // =====================================================

    @Transactional
    public void suspendUser(String id) {
        Utilisateur utilisateur = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        utilisateur.setStatutCompte(StatutCompte.SUSPENDU);
        utilisateur.setActif(false);

        repository.save(utilisateur);
    }

    @Transactional
    public void activateUser(String id) {
        Utilisateur utilisateur = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        utilisateur.setStatutCompte(StatutCompte.ACTIF);
        utilisateur.setActif(true);

        repository.save(utilisateur);
    }

    // =====================================================
    // MOT DE PASSE
    // =====================================================

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

        Utilisateur utilisateur = repository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur avec cet email non trouvé"));

        utilisateur.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));

        repository.save(utilisateur);
    }

    // =====================================================
    // LECTURE
    // =====================================================

    public UtilisateurDto findById(String id) {
        Utilisateur utilisateur = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return mapper.toDto(utilisateur);
    }

    public List<UtilisateurDto> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public Iterable<UtilisateurDto> getAllUsers() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public Utilisateur findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    // =====================================================
    // SUPPRESSION
    // =====================================================

    @Transactional
    public void deleteByEmail(String email) {
        Utilisateur utilisateur = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur avec cet email non trouvé"));

        repository.delete(utilisateur);
    }

    // =====================================================
    // PHOTO
    // =====================================================

    private String savePhotoOrDefault(MultipartFile photo) throws IOException {
        if (photo == null || photo.isEmpty()) {
            return DEFAULT_PHOTO_PATH;
        }

        return savePhoto(photo);
    }

    private String savePhoto(MultipartFile photo) throws IOException {
        if (photo == null || photo.isEmpty()) {
            return DEFAULT_PHOTO_PATH;
        }

        String contentType = photo.getContentType();

        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Le fichier doit être une image JPEG ou PNG.");
        }

        String originalFilename = photo.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "utilisateur-photo";
        }

        String photoName = System.currentTimeMillis() + "_" + originalFilename;
        Path path = Paths.get(uploadDir, photoName);

        Files.createDirectories(path.getParent());
        Files.write(path, photo.getBytes());

        return "/uploads/utilisateurs/" + photoName;
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateUtilisateurDto(UtilisateurDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Les informations de l'utilisateur sont obligatoires.");
        }

        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("L'email de l'utilisateur ne peut pas être vide.");
        }

        if (dto.getNom() == null || dto.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom de l'utilisateur ne peut pas être vide.");
        }

        if (dto.getPrenom() == null || dto.getPrenom().isBlank()) {
            throw new IllegalArgumentException("Le prénom de l'utilisateur ne peut pas être vide.");
        }
    }

    /*
     * Compatibilité entre les deux versions des DTO :
     * - nouvelle version : getConfirmationMotDePasse()
     * - ancienne version : getConfirmerMotDePasse()
     */
    private String lireConfirmationMotDePasse(Object dto) {
        try {
            Method method = dto.getClass().getMethod("getConfirmationMotDePasse");
            Object value = method.invoke(dto);
            return value != null ? value.toString() : null;
        } catch (ReflectiveOperationException ignored) {
            // On tente l'ancien nom après.
        }

        try {
            Method method = dto.getClass().getMethod("getConfirmerMotDePasse");
            Object value = method.invoke(dto);
            return value != null ? value.toString() : null;
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Aucun utilisateur ne correspond à cet identifiant"
                ));
    }
}