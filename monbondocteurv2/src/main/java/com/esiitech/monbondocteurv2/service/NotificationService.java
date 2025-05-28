package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.model.Validation;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class NotificationService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String emailFrom;

    // Injection du JavaMailSender via le constructeur
    public NotificationService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void envoyer(Validation validation) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailFrom);
        message.setTo(validation.getUtilisateur().getEmail());
        message.setSubject("Votre code d'activation");

        String texte = String.format(
                "Bonjour M/Mme %s,\n" +
                        "Nous vous informons que votre demande d'inscription a été reçue.\n" +
                        "Pour finaliser votre inscription, veuillez utiliser le code d'activation suivant : %s.\n" +
                        "Ce code est valable pour les 10 prochaines minutes.\n" +
                        "Si vous n'avez pas demandé cette inscription, veuillez ignorer ce message.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe de MonBondocteur.",
                validation.getUtilisateur().getNom(), validation.getCode());

        message.setText(texte);
        javaMailSender.send(message);
    }

    public void envoyerAuPatient(String email, String nomPatient, String nomMedecin) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailFrom);
        message.setTo(email);
        message.setSubject("Confirmation de votre rendez-vous");

        String texte = String.format(
                "Bonjour M/Mme %s,\n\n" +
                        "Nous vous confirmons que votre rendez-vous avec le docteur %s a bien été enregistré.\n\n" +
                        "📅 Veuillez vous assurer d’être disponible à la date et à l’heure convenues.\n" +
                        "Si vous souhaitez modifier ou annuler ce rendez-vous, merci de le faire au moins 24 heures à l’avance via notre plateforme MonBonDocteur.\n\n" +
                        "Cordialement,\n" +
                        "L’équipe MonBonDocteur.",
                nomPatient, nomMedecin
        );

        message.setText(texte);
        javaMailSender.send(message);
    }

    public void envoyerAuMedecin(String email, String nomMedecin, String nomPatient) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailFrom);
        message.setTo(email);
        message.setSubject("Nouveau rendez-vous programmé");

        String texte = String.format(
                "Bonjour Dr %s,\n\n" +
                        "Un nouveau rendez-vous vient d’être enregistré avec le patient : %s.\n\n" +
                        "📅 Merci de consulter votre planning pour prendre connaissance de ce rendez-vous.\n" +
                        "Cordialement,\n" +
                        "L’équipe MonBonDocteur.",
                nomMedecin, nomPatient
        );

        message.setText(texte);
        javaMailSender.send(message);
    }
}
