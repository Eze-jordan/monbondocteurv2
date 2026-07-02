package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.ChangementMotDePasseDto;
import com.esiitech.monbondocteurv2.dto.MedecinDto;
import com.esiitech.monbondocteurv2.dto.StructureSanitaireDto;
import com.esiitech.monbondocteurv2.enums.Role;
import com.esiitech.monbondocteurv2.exception.MedecinNonTrouveException;
import com.esiitech.monbondocteurv2.mapper.MedecinMapper;
import com.esiitech.monbondocteurv2.mapper.StructureSanitaireMapper;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.MedecinStructureSanitaire;
import com.esiitech.monbondocteurv2.model.Validation;
import com.esiitech.monbondocteurv2.repository.MedecinRepository;
import com.esiitech.monbondocteurv2.repository.MedecinStructureSanitaireRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class MedecinService implements UserDetailsService {

    @Autowired
    private MedecinRepository repository;

    @Autowired
    private MedecinMapper medecinMapper;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MedecinStructureSanitaireRepository medecinStructureSanitaireRepository;

    @Autowired
    private StructureSanitaireMapper structureSanitaireMapper;

    @Value("${app.upload.dir.medecins}")
    private String uploadDir;

    private static final long START_AT = 100000L;
    private static final AtomicLong LAST_MEDECIN_ID = new AtomicLong(START_AT);

    // ==================== CRÉATION ====================

    @Transactional
    public MedecinDto save(MedecinDto dto, MultipartFile photo) throws IOException {
        validateMedecinDto(dto);

        if (dto.getRole() == null) {
            dto.setRole(Role.MEDECIN);
        }

        Medecin entity = medecinMapper.toEntity(dto);

        if (photo != null && !photo.isEmpty()) {
            String photoPath = savePhoto(photo);
            entity.setPhotoPath(photoPath);
        }

        entity.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));

        if (entity.getId() == null) {
            entity.setId(generateMedecinId());
        }

        Medecin savedMedecin = repository.save(entity);
        validationService.enregisterMedecin(savedMedecin);

        return medecinMapper.toDto(savedMedecin);
    }

    // ==================== ID GÉNÉRATEUR ====================

    @PostConstruct
    public void initMedecinLastId() {
        long max = START_AT;

        for (Medecin medecin : repository.findAll()) {
            try {
                String idStr = medecin.getId();

                if (idStr != null && idStr.matches("\\d{6}")) {
                    long value = Long.parseLong(idStr);

                    if (value > max) {
                        max = value;
                    }
                }
            } catch (NumberFormatException ignored) {
                // Ignore les IDs qui ne sont pas numériques.
            }
        }

        LAST_MEDECIN_ID.set(max);
    }

    private String generateMedecinId() {
        String id;

        do {
            long next = LAST_MEDECIN_ID.incrementAndGet();
            id = String.format("%06d", next);
        } while (repository.existsById(id));

        return id;
    }

    // ==================== ACTIVATION ====================

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
            throw new RuntimeException("Votre code a expiré.");
        }

        String medecinIdStr = validation.getMedecin().getId();

        Medecin medecinActiver = repository.findById(medecinIdStr)
                .orElseThrow(() -> new RuntimeException("Utilisateur inconnu."));

        medecinActiver.setActif(true);
        repository.save(medecinActiver);

        Long medecinIdLong;

        try {
            medecinIdLong = Long.parseLong(medecinIdStr);
        } catch (NumberFormatException ex) {
            throw new RuntimeException(
                    "L'ID du médecin n'est pas au format numérique attendu : " + medecinIdStr,
                    ex
            );
        }

        notificationService.envoyerBienvenueAuMedecin(
                medecinActiver.getEmail(),
                medecinActiver.getNomMedecin(),
                medecinIdLong
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

    // ==================== PHOTO ====================

    private String savePhoto(MultipartFile photo) throws IOException {
        String originalFilename = photo.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "medecin-photo";
        }

        String photoName = System.currentTimeMillis() + "_" + originalFilename;
        Path path = Paths.get(uploadDir, photoName);

        Files.createDirectories(path.getParent());
        Files.write(path, photo.getBytes());

        return "/uploads/medecins/" + photoName;
    }

    public byte[] getPhoto(String id) throws IOException {
        Medecin entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));

        String photoPath = entity.getPhotoPath();

        if (photoPath != null && !photoPath.isEmpty()) {
            Path path = Paths.get(uploadDir, photoPath.substring(photoPath.lastIndexOf("/") + 1));
            return Files.readAllBytes(path);
        }

        throw new RuntimeException("Aucune photo trouvée pour ce médecin.");
    }

    // ==================== VALIDATION ====================

    private void validateMedecinDto(MedecinDto dto) {
        if (dto.getNomMedecin() == null || dto.getNomMedecin().isEmpty()) {
            throw new IllegalArgumentException("Le nom du médecin ne peut pas être vide.");
        }

        if (dto.getPrenomMedecin() == null || dto.getPrenomMedecin().isEmpty()) {
            throw new IllegalArgumentException("Le prénom du médecin ne peut pas être vide.");
        }

        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            throw new IllegalArgumentException("L'email du médecin ne peut pas être vide.");
        }
    }

    // ==================== CRUD ====================

    @Transactional
    public MedecinDto update(String id, MedecinDto dto, MultipartFile photo) throws IOException {
        Medecin entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));

        entity.setNomMedecin(dto.getNomMedecin());
        entity.setPrenomMedecin(dto.getPrenomMedecin());
        entity.setEmail(dto.getEmail());
        entity.setRefGrade(dto.getRefGrade());
        entity.setRefSpecialite(dto.getRefSpecialite());
        entity.setActif(dto.isActif());

        if (photo != null && !photo.isEmpty()) {
            String photoPath = savePhoto(photo);
            entity.setPhotoPath(photoPath);
        }

        Medecin updatedMedecin = repository.save(entity);
        return medecinMapper.toDto(updatedMedecin);
    }

    @Transactional
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Transactional
    public void deleteByEmail(String email) {
        Medecin entity = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Médecin avec cet email non trouvé"));

        repository.delete(entity);
    }

    @Transactional
    public MedecinDto updateStatus(String id, boolean actif) {
        Medecin entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));

        entity.setActif(actif);

        Medecin updatedMedecin = repository.save(entity);
        return medecinMapper.toDto(updatedMedecin);
    }

    // ==================== LECTURE ====================

    public long countAll() {
        return repository.count();
    }

    public List<MedecinDto> findAll() {
        List<Medecin> medecins = repository.findAll();

        return medecins.stream()
                .map(medecinMapper::toDto)
                .collect(Collectors.toList());
    }

    public MedecinDto findByIdDto(String id) {
        Medecin medecin = repository.findById(id)
                .orElseThrow(() -> new MedecinNonTrouveException(
                        "Médecin introuvable avec l'id " + id
                ));

        return medecinMapper.toDto(medecin);
    }

    public Medecin findEntityById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Médecin introuvable avec l'id " + id
                ));
    }

    public MedecinDto findByEmail(String email) {
        Medecin entity = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Médecin avec cet email non trouvé"));

        return medecinMapper.toDto(entity);
    }

    public Medecin getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable avec l'id " + id));
    }

    // ==================== RECHERCHE ====================

    public List<MedecinDto> searchBySpeciality(String speciality) {
        List<Medecin> medecins = repository.findByRefSpecialite(speciality);

        return medecins.stream()
                .map(medecinMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<MedecinDto> getActiveMedecins() {
        List<Medecin> medecins = repository.findByActif(true);

        return medecins.stream()
                .map(medecinMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<MedecinDto> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return findAll();
        }

        String query = name.trim();

        List<Medecin> medecins = repository
                .findByNomMedecinIgnoreCaseContainingOrPrenomMedecinIgnoreCaseContaining(
                        query,
                        query
                );

        return medecins.stream()
                .map(medecinMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<MedecinDto> searchByNomOuPrenom(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return Collections.emptyList();
        }

        Pageable limit = PageRequest.of(0, 10);

        List<Medecin> medecins = repository.searchByNomOuPrenom(
                searchTerm.trim(),
                limit
        );

        return medecins.stream()
                .map(medecinMapper::toDto)
                .collect(Collectors.toList());
    }

    // ==================== STRUCTURES DU MÉDECIN ====================

    public List<StructureSanitaireDto> getStructuresByMedecin(String medecinId) {
        repository.findById(medecinId)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));

        List<MedecinStructureSanitaire> liaisons = medecinStructureSanitaireRepository
                .findByMedecinIdAndActifTrue(medecinId);

        return liaisons.stream()
                .map(MedecinStructureSanitaire::getStructureSanitaire)
                .filter(Objects::nonNull)
                .map(structureSanitaireMapper::toDto)
                .collect(Collectors.toList());
    }

    // ==================== MOT DE PASSE ====================

    @Transactional
    public void updatePasswordByEmail(ChangementMotDePasseDto dto) {
        String nouveauMotDePasse = dto.getNouveauMotDePasse();
        String confirmationMotDePasse = lireConfirmationMotDePasse(dto);

        if (nouveauMotDePasse == null || nouveauMotDePasse.isBlank()) {
            throw new IllegalArgumentException("Le nouveau mot de passe est obligatoire.");
        }

        if (!nouveauMotDePasse.equals(confirmationMotDePasse)) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        }

        Medecin medecin = repository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur avec cet email non trouvé"));

        medecin.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        repository.save(medecin);
    }

    /*
     * Compatibilité entre les deux versions du DTO :
     * - ancienne version : getConfirmerMotDePasse()
     * - nouvelle version : getConfirmationMotDePasse()
     */
    private String lireConfirmationMotDePasse(ChangementMotDePasseDto dto) {
        try {
            Method method = dto.getClass().getMethod("getConfirmationMotDePasse");
            Object value = method.invoke(dto);
            return value != null ? value.toString() : null;
        } catch (ReflectiveOperationException ignored) {
            // On tente l'ancien nom juste après.
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
}