package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.StructureSanitaireDto;
import com.esiitech.monbondocteurv2.mapper.StructureSanitaireMapper;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class StructureSanitaireService {

    @Autowired
    private StructureSanitaireRepository repository;

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

        // Sauvegarder dans la base de données
        structureSanitaire = repository.save(structureSanitaire);

        // Retourner le DTO de la structure sanitaire sauvegardée avec l'URL de la photo
        return mapper.toDto(structureSanitaire);
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
    public StructureSanitaireDto findById(Long id) {
        StructureSanitaire structureSanitaire = repository.findById(id).orElseThrow(() -> new RuntimeException("Structure sanitaire non trouvée"));
        return mapper.toDto(structureSanitaire);
    }

    /**
     * Mettre à jour les informations d'une structure sanitaire.
     */
    public StructureSanitaireDto update(Long id, StructureSanitaireDto dto, MultipartFile photo) throws IOException {
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
    public void deleteById(Long id) {
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
}
