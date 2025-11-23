package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.ChangementMotDePasseDto;
import com.esiitech.monbondocteurv2.dto.UtilisateurDto;
import com.esiitech.monbondocteurv2.mapper.UtilisateurMapper;
import com.esiitech.monbondocteurv2.model.Utilisateur;
import com.esiitech.monbondocteurv2.model.Validation;
import com.esiitech.monbondocteurv2.repository.UtilisateurRepository;
import com.esiitech.monbondocteurv2.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UtilisateurService  implements UserDetailsService {

    @Autowired
    private UtilisateurRepository repository;
    @Autowired
    private UtilisateurMapper mapper;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private  ValidationService validationService;
    @Autowired
    private UtilisateurRepository utilisateurRepository;



    @Value("${app.upload.dir.utilisateurs}")  // Le répertoire où les photos des utilisateurs sont stockées
    private String uploadDir;

    private static final String DEFAULT_PHOTO_PATH = "/uploads/utilisateurs/default.jpg"; // Photo par défaut

    /**
     * Enregistrer un utilisateur avec sa photo.
     */
    public UtilisateurDto save(UtilisateurDto dto, MultipartFile photo) throws IOException {
        // Valider les données du DTO
        validateUtilisateurDto(dto);

        // Vérification du rôle et assignation du rôle par défaut (USER) si nécessaire
        if (dto.getRole() == null) {
            dto.setRole(Role.USER);  // Assigner le rôle USER par défaut
        }

        Utilisateur utilisateur = mapper.toEntity(dto);

        // Hacher le mot de passe avant de le sauvegarder
        utilisateur.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));

        // Sauvegarder la photo si elle existe, sinon mettre la photo par défaut
        if (photo != null && !photo.isEmpty()) {
            String photoPath = savePhoto(photo);  // Sauvegarder la photo et obtenir son chemin
            utilisateur.setPhotoPath(photoPath);  // Mettre à jour le chemin de la photo
        } else {
            // Si aucune photo n'est fournie, assigner une photo par défaut
            utilisateur.setPhotoPath(DEFAULT_PHOTO_PATH);
        }

        if (utilisateur.getId() == null) {
            utilisateur.setId(generateUserId());
        }

        // Sauvegarder dans la base de données
        utilisateur = this.repository.save(utilisateur);
        this.validationService.enregister(utilisateur);



        // Retourner le DTO de l'utilisateur sauvegardé avec l'URL de la photo
        return mapper.toDto(utilisateur);


    }

    private String generateUserId() {
        return "user-" + java.util.UUID.randomUUID();
    }

    /**
     * Valider les données du DTO de l'utilisateur avant d'effectuer l'enregistrement
     */
    private void validateUtilisateurDto(UtilisateurDto dto) {
        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            throw new IllegalArgumentException("L'email de l'utilisateur ne peut pas être vide.");
        }

        if (dto.getNom() == null || dto.getNom().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'utilisateur ne peut pas être vide.");
        }

        if (dto.getPrenom() == null || dto.getPrenom().isEmpty()) {
            throw new IllegalArgumentException("Le prénom de l'utilisateur ne peut pas être vide.");
        }
    }

    /**
     * Sauvegarder la photo de l'utilisateur et retourner son chemin.
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
        return "/uploads/utilisateurs/" + photoName;
    }
    /**
     * Récupérer un utilisateur par son ID.
     */
    public UtilisateurDto findById(String id) {
        Utilisateur utilisateur = repository.findById(id).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return mapper.toDto(utilisateur);
    }

    /**
     * Mettre à jour le profil d'un utilisateur.
     */
    public UtilisateurDto update(String id, UtilisateurDto dto) {
        Utilisateur utilisateur = repository.findById(id).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Mettre à jour les informations de l'utilisateur
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        utilisateur.setRole(dto.getRole());


        // Sauvegarder les modifications
        Utilisateur updatedUtilisateur = repository.save(utilisateur);

        return mapper.toDto(updatedUtilisateur);
    }

    /**
     * Supprimer un utilisateur par son email.
     */
    public void deleteByEmail(String email) {
        Utilisateur utilisateur = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur avec cet email non trouvé"));
        repository.delete(utilisateur);
    }

    /**
     * Récupérer tous les utilisateurs.
     */
    public Iterable<UtilisateurDto> getAllUsers() {
        // Convertir l'Iterable en une List
        List<Utilisateur> utilisateursList = (List<Utilisateur>) repository.findAll();
        return utilisateursList.stream()
                .map(mapper::toDto)  // Mapper chaque entité Utilisateur en DTO
                .collect(Collectors.toList());  // Collecter en une liste
    }

    public void activation(Map<String, String> activation) {
        Validation validation = validationService.lireEnFonctionDuCode(activation.get("code"));
        if (Instant.now().isAfter(validation.getExpiration())) {
            throw new RuntimeException("Votre code a expiré");
        }

        Utilisateur utilisateurActiver = utilisateurRepository.findById(validation.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur inconnu"));

        utilisateurActiver.setActif(true);
        utilisateurRepository.save(utilisateurActiver);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.utilisateurRepository.findByEmail(username).orElseThrow (()
                -> new UsernameNotFoundException(
                "Aucun utilisateur ne conrespond à cet identifiant"
        ));
    }

    public void updatePasswordByEmail(ChangementMotDePasseDto dto) {
        if (!dto.getNouveauMotDePasse().equals(dto.getConfirmerMotDePasse())) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur avec cet email non trouvé"));

        utilisateur.setMotDePasse(passwordEncoder.encode(dto.getNouveauMotDePasse()));
        utilisateurRepository.save(utilisateur);
    }

}
