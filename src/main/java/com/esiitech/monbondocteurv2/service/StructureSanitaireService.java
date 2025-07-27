package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.ChangementMotDePasseDto;
import com.esiitech.monbondocteurv2.dto.StructureSanitaireDto;
import com.esiitech.monbondocteurv2.mapper.StructureSanitaireMapper;
import com.esiitech.monbondocteurv2.model.*;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    @Value("${app.upload.dir.structureSanitaire}")  // Le répertoire où les photos des structures sanitaires sont stockées
    private String uploadDir;

    private static final String DEFAULT_PHOTO_PATH = "/uploads/structuresanitaire/default.jpg"; // Photo par défaut

    /**
     * Enregistrer une structure sanitaire avec sa photo.
     */
    public StructureSanitaireDto save(StructureSanitaireDto dto, MultipartFile photo) throws IOException {
        // Valider les données du DTO
        validateStructureSanitaireDto(dto);

        // Créer l'entité StructureSanitaire
        StructureSanitaire structureSanitaire = mapper.toEntity(dto);

        // Sauvegarder la photo si elle existe, sinon mettre la photo par défaut
        if (photo != null && !photo.isEmpty()) {
            String photoPath = savePhoto(photo);  // Sauvegarder la photo et obtenir son chemin
            structureSanitaire.setPhotoPath(photoPath);  // Mettre à jour le chemin de la photo
        } else {
            // Si aucune photo n'est fournie, assigner une photo par défaut
            structureSanitaire.setPhotoPath(DEFAULT_PHOTO_PATH);
        }

        // Vérification du rôle et assignation du rôle par défaut (USER) si nécessaire
        if (structureSanitaire.getRole() == null) {
            structureSanitaire.setRole(Role.STRUCTURESANITAIRE);  // Assigner le rôle USER par défaut
        }
        // ✅ Encodage correct du mot de passe
        structureSanitaire.setMotDePasse(passwordEncoder.encode(structureSanitaire.getMotDePasse()));

        if (structureSanitaire.getId() == null) {
            structureSanitaire.setId(generateCustomId());
        }

        // Sauvegarder dans la base de données
        structureSanitaire = repository.save(structureSanitaire);
        this.validationService.enregisterStructure(structureSanitaire);



        // Retourner le DTO de la structure sanitaire sauvegardée avec l'URL de la photo
        return mapper.toDto(structureSanitaire);
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
        notificationService.envoyerBienvenueAuStructures(StructureActiver.getEmail(), StructureActiver.getNomStructureSanitaire());

    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.repository.findByEmail(username).orElseThrow (()
                -> new UsernameNotFoundException(
                "Aucun utilisateur ne conrespond à cet identifiant"
        ));
    }

    public String getSpecialitesStructure(String id) {
        StructureSanitaire structure = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));
        return structure.getRefSpecialites();
    }



    public Set<String> getToutesLesSpecialitesUtilisees() {
        return repository.findAll().stream()
                .map(StructureSanitaire::getRefSpecialites)        // récupère la chaîne "Cardiologie, Pédiatrie"
                .filter(Objects::nonNull)                          // ignore les valeurs null
                .flatMap(specialites -> Arrays.stream(specialites.split(","))) // découpe chaque chaîne
                .map(String::trim)                                 // supprime les espaces autour
                .filter(s -> !s.isEmpty())                         // ignore les vides
                .collect(Collectors.toSet());                      // regroupe sans doublons
    }

    public List<StructureSanitaireDto> findBySpecialite(String specialite) {
        List<StructureSanitaire> list = repository.findBySpecialiteContainingIgnoreCase(specialite);
        return list.stream().map(mapper::toDto).collect(Collectors.toList());
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
    private String savePhoto(MultipartFile photo) throws IOException {
        // Vérifier si le fichier est une image (par exemple, JPEG ou PNG)
        String contentType = photo.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Le fichier doit être une image JPEG ou PNG.");
        }

        // Créer un nom unique pour la photo
        String photoName = System.currentTimeMillis() + "_" + photo.getOriginalFilename();
        Path path = Paths.get(uploadDir, photoName);

        // Créer les répertoires si nécessaires
        Files.createDirectories(path.getParent());

        // Sauvegarder le fichier
        Files.write(path, photo.getBytes());

        // Retourner le chemin relatif pour afficher l'image
        return "/uploads/structuresanitaire/" + photoName;
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
}

