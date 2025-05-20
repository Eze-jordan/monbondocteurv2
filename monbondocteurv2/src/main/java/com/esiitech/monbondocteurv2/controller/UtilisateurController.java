package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.UtilisateurDto;
import com.esiitech.monbondocteurv2.exception.ResourceNotFoundException;
import com.esiitech.monbondocteurv2.service.UtilisateurService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UtilisateurController {

    @Autowired
    private UtilisateurService utilisateurService;

    /**
     * Créer un nouvel utilisateur avec sa photo.
     * Le JSON contient les informations de l'utilisateur et la photo est envoyée en tant que MultipartFile.
     */
    @PostMapping("/create")
    public ResponseEntity<UtilisateurDto> createUtilisateur(
            @RequestParam(value = "photo", required = false) MultipartFile photo,  // Photo envoyée en tant que fichier, maintenant facultative
            @RequestParam("utilisateur") String utilisateurJson) throws IOException {

        // Convertir le JSON en DTO Utilisateur
        ObjectMapper objectMapper = new ObjectMapper();
        UtilisateurDto dto = objectMapper.readValue(utilisateurJson, UtilisateurDto.class);


        // Si la photo est manquante, définir une photo par défaut
        if (photo == null || photo.isEmpty()) {
            dto.setPhotoPath("/uploads/utilisateurs/default.jpg");
        }

        // Appeler le service pour enregistrer l'utilisateur avec la photo (ou la photo par défaut)
        UtilisateurDto savedUtilisateur = utilisateurService.save(dto, photo);

        return new ResponseEntity<>(savedUtilisateur, HttpStatus.CREATED);
    }
    @PostMapping("/activation")
    public ResponseEntity<String> activation(@RequestBody Map<String, String> activation) {
        try {
            this.utilisateurService.activation(activation);
            return ResponseEntity.ok("Compte activé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Récupérer un utilisateur par son ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDto> getUtilisateur(@PathVariable Long id) {
        UtilisateurDto utilisateurDto = utilisateurService.findById(id);

        // Si l'utilisateur n'est pas trouvé, lancer l'exception avec des informations personnalisées
        if (utilisateurDto == null) {
            throw new ResourceNotFoundException(
                    "Utilisateur",  // Nom de la ressource
                    "id",           // Le champ concerné
                    id,             // La valeur du champ
                    404             // Code d'erreur personnalisé
            );
        }

        return new ResponseEntity<>(utilisateurDto, HttpStatus.OK);
    }

    /**
     * Mettre à jour un utilisateur.
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<UtilisateurDto> updateUtilisateur(@PathVariable Long id, @RequestBody UtilisateurDto dto) {
        UtilisateurDto updatedUtilisateur = utilisateurService.update(id, dto);
        return new ResponseEntity<>(updatedUtilisateur, HttpStatus.OK);
    }

    /**
     * Supprimer un utilisateur par son email.
     */
    @DeleteMapping("/delete/{email}")
    public ResponseEntity<Void> deleteUtilisateur(@PathVariable String email) {
        utilisateurService.deleteByEmail(email);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Récupérer tous les utilisateurs.
     */
    @GetMapping("/all")
    public ResponseEntity<Iterable<UtilisateurDto>> getAllUtilisateurs() {
        Iterable<UtilisateurDto> utilisateurs = utilisateurService.getAllUsers();
        return new ResponseEntity<>(utilisateurs, HttpStatus.OK);
    }
}
