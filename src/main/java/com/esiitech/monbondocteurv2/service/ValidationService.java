package com.esiitech.monbondocteurv2.service;


import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.model.Utilisateur;
import com.esiitech.monbondocteurv2.model.Validation;
import com.esiitech.monbondocteurv2.repository.ValidationRipository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
        Instant expiration = creation.plus(60, MINUTES);
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
            validation.setExpiration(now.plus(60, MINUTES));

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
        Instant expiration = creation.plus(60, MINUTES);
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
            validation.setExpiration(now.plus(60, MINUTES));
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
        Instant expiration = creation.plus(60, MINUTES);
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
            validation.setExpiration(now.plus(60, MINUTES));

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
}
