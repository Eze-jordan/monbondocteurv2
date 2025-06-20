package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.MedecinDto;
import com.esiitech.monbondocteurv2.mapper.MedecinMapper;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.repository.MedecinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedecinService {

    @Autowired
    private MedecinRepository repository;

    @Autowired
    private MedecinMapper mapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Value("${app.upload.dir.medecins}")  // Dossier où les photos des médecins sont stockées
    private String uploadDir;

    /**
     * Enregistrer un médecin avec la photo envoyée en tant que fichier.
     */
    public MedecinDto save(MedecinDto dto, MultipartFile photo) throws IOException {
        // Valider les données du DTO avant de les sauvegarder
        validateMedecinDto(dto);

        // Convertir le DTO en entité
        Medecin entity = mapper.toEntity(dto);

        // Sauvegarder la photo si elle existe
        if (photo != null && !photo.isEmpty()) {
            String photoPath = savePhoto(photo); // Sauvegarder la photo et obtenir son chemin
            entity.setPhotoPath(photoPath);
        }

        // Encoder le mot de passe
        entity.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));

        // Sauvegarder dans la base de données
        Medecin savedMedecin = repository.save(entity);

        // Convertir l'entité sauvegardée en DTO et retourner le DTO
        return mapper.toDto(savedMedecin);
    }

    /**
     * Sauvegarder la photo et retourner son chemin
     */
    private String savePhoto(MultipartFile photo) throws IOException {
        // Créer un nom unique pour la photo
        String photoName = System.currentTimeMillis() + "_" + photo.getOriginalFilename();
        Path path = Paths.get(uploadDir, photoName);

        // Créer les répertoires si nécessaires
        Files.createDirectories(path.getParent());

        // Sauvegarder le fichier
        Files.write(path, photo.getBytes());

        return "/uploads/medecins/" + photoName;  // Retourner l'URL relative pour afficher l'image
    }

    /**
     * Valider les données du DTO du médecin.
     */
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

        // Ajoutez des validations supplémentaires si nécessaire
    }

    /**
     * Mettre à jour un médecin.
     */
    public MedecinDto update(Long id, MedecinDto dto, MultipartFile photo) throws IOException {
        Medecin entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Médecin non trouvé"));

        // Mettre à jour les informations
        entity.setNomMedecin(dto.getNomMedecin());
        entity.setPrenomMedecin(dto.getPrenomMedecin());
        entity.setEmail(dto.getEmail());
        entity.setRefGrade(dto.getRefGrade());
        entity.setRefSpecialite(dto.getRefSpecialite());
        entity.setActif(dto.isActif());

        // Mettre à jour la photo si elle est présente
        if (photo != null && !photo.isEmpty()) {
            String photoPath = savePhoto(photo); // Sauvegarder la photo
            entity.setPhotoPath(photoPath);
        }

        // Sauvegarder les modifications
        Medecin updatedMedecin = repository.save(entity);

        return mapper.toDto(updatedMedecin);
    }



    /**
     * Supprimer un médecin par son ID.
     */
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * Convertir le chemin de la photo en une URL complète.
     * Cela permet d'afficher la photo depuis le front-end via une URL publique.
     */


    // 1. Méthode findAll()
    public List<MedecinDto> findAll() {
        List<Medecin> medecins = repository.findAll();
        return medecins.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    // 2. Méthode findByEmail()
    public MedecinDto findByEmail(String email) {
        Medecin entity = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Médecin avec cet email non trouvé"));
        return mapper.toDto(entity);
    }

    // 3. Méthode updateStatus()
    public MedecinDto updateStatus(Long id, boolean actif) {
        Medecin entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));
        entity.setActif(actif);
        Medecin updatedMedecin = repository.save(entity);
        return mapper.toDto(updatedMedecin);
    }

    // 4. Méthode deleteByEmail()
    public void deleteByEmail(String email) {
        Medecin entity = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Médecin avec cet email non trouvé"));
        repository.delete(entity);
    }

    // 5. Méthode countAll()
    public long countAll() {
        return repository.count();
    }

    // 6. Méthode getPhoto()
    public byte[] getPhoto(Long id) throws IOException {
        Medecin entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));
        String photoPath = entity.getPhotoPath();
        if (photoPath != null && !photoPath.isEmpty()) {
            Path path = Paths.get(uploadDir, photoPath.substring(photoPath.lastIndexOf("/") + 1));
            return Files.readAllBytes(path);
        } else {
            throw new RuntimeException("Aucune photo trouvée pour ce médecin.");
        }
    }

    // 7. Méthode searchBySpeciality()
    public List<MedecinDto> searchBySpeciality(String speciality) {
        List<Medecin> medecins = repository.findByRefSpecialite(speciality);
        return medecins.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    // 8. Méthode getActiveMedecins()
    public List<MedecinDto> getActiveMedecins() {
        List<Medecin> medecins = repository.findByActif(true);
        return medecins.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
