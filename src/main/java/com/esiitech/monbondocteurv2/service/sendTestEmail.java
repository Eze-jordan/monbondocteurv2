package com.esiitech.monbondocteurv2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class sendTestEmail {
    @Autowired
    private JavaMailSender javaMailSender;

    @RequestMapping("/send-test-email")
    public String sendTestEmail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("notify@eservices-gabon.com");
        message.setTo("votre-email@example.com");
        message.setSubject("Test Email");
        message.setText("Ceci est un test d'email.");

        try {
            javaMailSender.send(message);
            return "E-mail envoyé avec succès!";
        } catch (MailException e) {
            e.printStackTrace();
            return "Erreur lors de l'envoi de l'e-mail.";
        }
    }

}
