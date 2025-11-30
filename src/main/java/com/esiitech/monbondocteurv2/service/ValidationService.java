package com.esiitech.monbondocteurv2.service;


import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.model.Utilisateur;
import com.esiitech.monbondocteurv2.model.Validation;
import com.esiitech.monbondocteurv2.repository.ValidationRipository;
import org.springframework.transaction.annotation.Transactional; // <-- celui-ci
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;

@Service
public class ValidationService {
    private ValidationRipository validationRipository;
    private NotificationService notificationService;
    public void enregister (Utilisateur utilisateur) {
        Validation validation = new Validation();
        validation.setUtilisateur(utilisateur);
        Instant creation = Instant.now();
        validation.setCreation(creation);
        Instant expiration = creation.plus(1, MINUTES);
        validation.setExpiration(expiration);

        Random random = new Random();
        int randomInteger = random.nextInt(999999);
        String code = String.format("%06d", randomInteger);

        validation.setCode(code);
        validation.setId("validation-" + UUID.randomUUID());
        this.validationRipository.save(validation);
        this.notificationService.envoyer(validation);
    }
    public void renvoyerCode(Utilisateur utilisateur) {
        Validation validation = validationRipository.findByUtilisateur(utilisateur)
                .orElseThrow(() -> new RuntimeException("Aucun code trouvé pour cet utilisateur"));

        Instant now = Instant.now();
        if (validation.getExpiration().isBefore(now)) {
            // Créer un nouveau code
            int randomInteger = new Random().nextInt(999999);
            String newCode = String.format("%06d", randomInteger);

            validation.setCode(newCode);
            validation.setCreation(now);
            validation.setExpiration(now.plus(1, MINUTES));

            validation.setId("validation-" + UUID.randomUUID());
            validationRipository.save(validation);
            notificationService.envoyer(validation);
        } else {
            throw new RuntimeException("Le code actuel est encore valide");
        }
    }
    // Méthode pour enregistrer un code de validation pour un Médecin
    public void enregisterMedecin(Medecin savedMedecin) {
        Validation validation = new Validation();
        validation.setMedecin(savedMedecin);  // Associe un médecin à la validation
        Instant creation = Instant.now();
        validation.setCreation(creation);
        Instant expiration = creation.plus(1, MINUTES);
        validation.setExpiration(expiration);

        Random random = new Random();
        int randomInteger = random.nextInt(999999);
        String code = String.format("%06d", randomInteger);

        validation.setCode(code);
        validation.setId("validation-" + UUID.randomUUID());
        this.validationRipository.save(validation);
        this.notificationService.envoyerMedecin(validation); // Envoie le code de validation
    }
    public void renvoyerCodeMedecin(Medecin savedMedecin ) {
        Validation validation = validationRipository.findByMedecin(savedMedecin)
                .orElseThrow(() -> new RuntimeException("Aucun code trouvé pour cet utilisateur"));

        Instant now = Instant.now();
        if (validation.getExpiration().isBefore(now)) {
            // Créer un nouveau code
            int randomInteger = new Random().nextInt(999999);
            String newCode = String.format("%06d", randomInteger);

            validation.setCode(newCode);
            validation.setCreation(now);
            validation.setExpiration(now.plus(1, MINUTES));
            validation.setId("validation-" + UUID.randomUUID());
            validationRipository.save(validation);
            notificationService.envoyerMedecin(validation);
        } else {
            throw new RuntimeException("Le code actuel est encore valide");
        }
    }

    // Méthode pour enregistrer un code de validation pour un Médecin
    public void enregisterStructure(StructureSanitaire structureSanitaire) {
        Validation validation = new Validation();
        validation.setStructureSanitaire(structureSanitaire);  // Associe un médecin à la validation
        Instant creation = Instant.now();
        validation.setCreation(creation);
        Instant expiration = creation.plus(1, MINUTES);
        validation.setExpiration(expiration);

        Random random = new Random();
        int randomInteger = random.nextInt(999999);
        String code = String.format("%06d", randomInteger);

        validation.setCode(code);

        validation.setId("validation-" + UUID.randomUUID());
        this.validationRipository.save(validation);
        this.notificationService.envoyerStructure(validation); // Envoie le code de validation
    }
    public void renvoyerCodeStructure(StructureSanitaire structureSanitaire ) {
        Validation validation = validationRipository.findByStructureSanitaire(structureSanitaire)
                .orElseThrow(() -> new RuntimeException("Aucun code trouvé pour cet utilisateur"));

        Instant now = Instant.now();
        if (validation.getExpiration().isBefore(now)) {
            // Créer un nouveau code
            int randomInteger = new Random().nextInt(999999);
            String newCode = String.format("%06d", randomInteger);

            validation.setCode(newCode);
            validation.setCreation(now);
            validation.setExpiration(now.plus(1, MINUTES));

            validation.setId("validation-" + UUID.randomUUID());
            validationRipository.save(validation);
            notificationService.envoyerStructure(validation);
        } else {
            throw new RuntimeException("Le code actuel est encore valide");
        }
    }


    public Validation lireEnFonctionDuCode(String code){
        return  this.validationRipository.findByCode(code).orElseThrow(()
                ->new RuntimeException("votre code est invalide"));
    }

    public ValidationRipository getValidationRipository() {
        return validationRipository;
    }

    public void setValidationRipository(ValidationRipository validationRipository) {
        this.validationRipository = validationRipository;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public ValidationService(ValidationRipository validationRipository, NotificationService notificationService) {
        this.validationRipository = validationRipository;
        this.notificationService = notificationService;
    }

    public void supprimerParId(String id) {
        if (!validationRipository.existsById(id)) {
            throw new RuntimeException("Aucune validation trouvée avec cet ID : " + id);
        }
        validationRipository.deleteById(id);
    }
    public void supprimerParUtilisateur(Utilisateur utilisateur) {
        Validation validation = validationRipository.findByUtilisateur(utilisateur)
                .orElseThrow(() -> new RuntimeException("Aucune validation trouvée pour cet utilisateur"));
        validationRipository.delete(validation);
    }
    public void supprimerParMedecin(Medecin medecin) {
        Validation validation = validationRipository.findByMedecin(medecin)
                .orElseThrow(() -> new RuntimeException("Aucune validation trouvée pour ce médecin"));
        validationRipository.delete(validation);
    }
    public void supprimerParStructure(StructureSanitaire structureSanitaire) {
        Validation validation = validationRipository.findByStructureSanitaire(structureSanitaire)
                .orElseThrow(() -> new RuntimeException("Aucune validation trouvée pour cette structure"));
        validationRipository.delete(validation);
    }

    public void supprimerParUtilisateurId(String utilisateurId) {
        Validation v = validationRipository.findByUtilisateur_Id(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Aucune validation trouvée pour cet utilisateur"));
        validationRipository.delete(v);
    }

    public void supprimerParMedecinId(String medecinId) {
        Validation v = validationRipository.findByMedecin_Id(medecinId)
                .orElseThrow(() -> new RuntimeException("Aucune validation trouvée pour ce médecin"));
        validationRipository.delete(v);
    }

    public void supprimerParStructureId(String structureId) {
        Validation v = validationRipository.findByStructureSanitaire_Id(structureId)
                .orElseThrow(() -> new RuntimeException("Aucune validation trouvée pour cette structure"));
        validationRipository.delete(v);
    }


    @Transactional(readOnly = true)
    public List<Validation> getAllValidationsByType(String type) {
        if (type == null || type.isBlank()) {
            return validationRipository.findAll(); // ton existant
        }
        switch (type.trim().toLowerCase()) {
            case "utilisateur":
            case "users":
            case "u":
                return validationRipository.findAllByUtilisateurIsNotNullOrderByCreationDesc();
            case "medecin":
            case "médecin":
            case "m":
                return validationRipository.findAllByMedecinIsNotNullOrderByCreationDesc();
            case "structure":
            case "structuresanitaire":
            case "s":
                return validationRipository.findAllByStructureSanitaireIsNotNullOrderByCreationDesc();
            default:
                throw new IllegalArgumentException("Type invalide. Attendu: utilisateur | medecin | structure");
        }
    }

    @Transactional(readOnly = true)
    public Map<String, List<Validation>> getAllValidationsGrouped() {
        return Map.of(
                "utilisateurs", validationRipository.findAllByUtilisateurIsNotNullOrderByCreationDesc(),
                "medecins", validationRipository.findAllByMedecinIsNotNullOrderByCreationDesc(),
                "structures", validationRipository.findAllByStructureSanitaireIsNotNullOrderByCreationDesc()
        );
    }

    @Transactional(readOnly = true)
    public List<Validation> getAllValidations() {
        return validationRipository.findAllByOrderByCreationDesc();
    }
}
