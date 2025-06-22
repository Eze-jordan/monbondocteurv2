package com.esiitech.monbondocteurv2.service;


import com.esiitech.monbondocteurv2.model.Utilisateur;
import com.esiitech.monbondocteurv2.model.Validation;
import com.esiitech.monbondocteurv2.repository.ValidationRipository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;

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
        Instant expiration = creation.plus(1, HOURS);
        validation.setExpiration(expiration);

        Random random = new Random();
        int randomInteger = random.nextInt(999999);
        String code = String.format("%06d", randomInteger);

        validation.setCode(code);

        this.validationRipository.save(validation);
        this.notificationService.envoyer(validation);
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
