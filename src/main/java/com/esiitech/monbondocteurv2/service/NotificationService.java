package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.model.DemandeMedecin;
import com.esiitech.monbondocteurv2.model.Validation;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
@Service
public class NotificationService {
    JavaMailSender javaMailSender;

    public NotificationService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void envoyer(Validation validation) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("notify@eservices-gabon.com");
            helper.setTo(validation.getUtilisateur().getEmail());
            helper.setSubject("Votre code d'activation");

            String htmlContent = """
            <div style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 30px;">
                <div style="max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                    <h2 style="text-align: center; color: #2c3e50;">Activation de compte</h2>
                    <p>Bonjour M/Mme/Mlle.<strong>%s</strong>,</p>
                    <p>Merci de vous être inscrit sur MonBonDocteur.</p>
                    <p>Voici votre code d'activation :</p>
                    <div style="text-align: center; font-size: 24px; font-weight: bold; margin: 20px 0; background: #eef; padding: 15px; border-radius: 5px;">%s</div>
                    <p>⏳ Ce code est valable pendant 60 minutes.</p>
                    <p style="color: #888; font-size: 12px; text-align: center;">
                        Si vous n'avez pas demandé cette inscription, veuillez ignorer ce message.
                    </p>
                    <p style="text-align: center; color: #aaa; margin-top: 20px;">— L’équipe MonBonDocteur</p>
                </div>
            </div>
        """.formatted(validation.getUtilisateur().getNom(), validation.getCode());

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void envoyerMedecin(Validation validation) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("notify@eservices-gabon.com");
            helper.setTo(validation.getMedecin().getEmail());
            helper.setSubject("Votre code d'activation");

            String htmlContent = """
            <div style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 30px;">
                <div style="max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                    <h2 style="text-align: center; color: #2c3e50;">Activation de compte</h2>
                    <p>Bonjour M/Mme/Mlle<strong>%s</strong>,</p>
                    <p>Merci de vous être inscrit sur MonBonDocteur.</p>
                    <p>Voici votre code d'activation :</p>
                    <div style="text-align: center; font-size: 24px; font-weight: bold; margin: 20px 0; background: #eef; padding: 15px; border-radius: 5px;">%s</div>
                    <p>⏳ Ce code est valable pendant 60 minutes.</p>
                    <p style="color: #888; font-size: 12px; text-align: center;">
                        Si vous n'avez pas demandé cette inscription, veuillez ignorer ce message.
                    </p>
                    <p style="text-align: center; color: #aaa; margin-top: 20px;">— L’équipe MonBonDocteur</p>
                </div>
            </div>
        """.formatted(validation.getMedecin().getNomMedecin(), validation.getCode());

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    public void envoyerStructure(Validation validation) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("notify@eservices-gabon.com");
            helper.setTo(validation.getStructureSanitaire().getEmail());
            helper.setSubject("Votre code d'activation");

            String htmlContent = """
            <div style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 30px;">
                <div style="max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                    <h2 style="text-align: center; color: #2c3e50;">Activation de compte</h2>
                    <p>Bonjour <strong>%s</strong>,</p>
                    <p>Merci de vous être inscrit sur MonBonDocteur.</p>
                    <p>Voici votre code d'activation :</p>
                    <div style="text-align: center; font-size: 24px; font-weight: bold; margin: 20px 0; background: #eef; padding: 15px; border-radius: 5px;">%s</div>
                    <p>⏳ Ce code est valable pendant 60 minutes.</p>
                    <p style="color: #888; font-size: 12px; text-align: center;">
                        Si vous n'avez pas demandé cette inscription, veuillez ignorer ce message.
                    </p>
                    <p style="text-align: center; color: #aaa; margin-top: 20px;">— L’équipe MonBonDocteur</p>
                </div>
            </div>
        """.formatted(validation.getStructureSanitaire().getNomStructureSanitaire(), validation.getCode());

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }



    public void envoyerAuPatient(String email, String nomPatient, String nomMedecin) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("notify@eservices-gabon.com");
            helper.setTo(email);
            helper.setSubject("Confirmation de votre rendez-vous");

            String htmlContent = """
            <div style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 30px;">
                <div style="max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                    <h2 style="text-align: center; color: #2c3e50;">MonBonDocteur 🩺</h2>
                    <h3 style="color: #2c3e50;">Bonjour <strong>%s</strong>,</h3>
                    <p>Votre rendez-vous avec le docteur <strong>%s</strong> a été <strong>confirmé</strong> avec succès.</p>

                    <p style="margin-top: 20px;">
                        📅 Veuillez vous assurer d’être disponible à la date et à l’heure convenues.
                        En cas d’empêchement, merci d’annuler ou modifier votre rendez-vous au moins 24h à l’avance.
                    </p>

                    <div style="margin: 30px 0; text-align: center;">
                        <a href="https://monbondocteur.com/login" style="background: #1e87f0; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px;">
                            Voir mes rendez-vous
                        </a>
                    </div>

                    <p style="color: #888; font-size: 12px; text-align: center;">
                        Ce message vous est envoyé automatiquement, merci de ne pas y répondre directement.
                    </p>

                    <p style="text-align: center; margin-top: 20px; color: #aaa;">
                        — L’équipe MonBonDocteur
                    </p>
                </div>
            </div>
            """.formatted(nomPatient, nomMedecin);

            helper.setText(htmlContent, true); // true = HTML
            javaMailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace(); // à remplacer par une vraie gestion d'erreur
        }
    }

    public void envoyerAuMedecin(String email, String nomMedecin, String nomPatient) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("notify@eservices-gabon.com");
            helper.setTo(email);
            helper.setSubject("Nouveau rendez-vous programmé");

            String htmlContent = """
            <div style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 30px;">
                <div style="max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                    <h2 style="text-align: center; color: #2c3e50;">Nouveau rendez-vous</h2>
                    <p>Bonjour Dr <strong>%s</strong>,</p>
                    <p>Un nouveau rendez-vous a été enregistré avec le patient : <strong>%s</strong>.</p>
                    <p>📅 Merci de consulter votre planning depuis votre espace personnel.</p>
                    <p style="text-align: center; margin-top: 30px;">
                        <a href="https://monbondocteur.com/login" style="background: #1e87f0; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px;">
                            Voir mon planning
                        </a>
                    </p>
                    <p style="text-align: center; color: #aaa; margin-top: 20px;">— L’équipe MonBonDocteur</p>
                </div>
            </div>
        """.formatted(nomMedecin, nomPatient);

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    public void envoyerBienvenueAuMedecin(String email, String nomMedecin) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("notify@eservices-gabon.com");
            helper.setTo(email);
            helper.setSubject("Bienvenue sur Mon Bon Docteur");

            String htmlContent = """
            <div style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 30px;">
                <div style="max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                    <h2 style="text-align: center; color: #2c3e50;">Bienvenue Dr %s 👨‍⚕️</h2>
                    <p>Merci d’avoir rejoint MonBonDocteur !</p>
                    <p>Vous pouvez maintenant gérer vos disponibilités, rendez-vous, et interagir avec vos patients.</p>
                    <div style="text-align: center; margin-top: 30px;">
                        <a href="https://monbondocteur.com/login" style="background: #1e87f0; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px;">
                            Accéder à mon espace
                        </a>
                    </div>
                    <p style="text-align: center; color: #aaa; margin-top: 20px;">— L’équipe MonBonDocteur</p>
                </div>
            </div>
        """.formatted(nomMedecin);

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    public void envoyerBienvenueAuStructures(String email, String nomStructureSanitaire) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("notify@eservices-gabon.com");
            helper.setTo(email);
            helper.setSubject("Bienvenue sur Mon Bon Docteur");

            String htmlContent = """
            <div style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 30px;">
                <div style="max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                    <h2 style="text-align: center; color: #2c3e50;">Bienvenue %s 🏥</h2>
                    <p>Merci d’avoir rejoint MonBonDocteur en tant que structure sanitaire partenaire.</p>
                    <p>Vous pouvez dès maintenant inscrire vos médecins et gérer les rendez-vous via votre tableau de bord.</p>
                    <div style="text-align: center; margin-top: 30px;">
                        <a href="https://monbondocteur.com/login" style="background: #1e87f0; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px;">
                            Accéder au tableau de bord
                        </a>
                    </div>
                    <p style="text-align: center; color: #aaa; margin-top: 20px;">— L’équipe MonBonDocteur</p>
                </div>
            </div>
        """.formatted(nomStructureSanitaire);

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    public void envoyerConfirmationDemandeMedecin(DemandeMedecin demande) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("notify@eservices-gabon.com");
            helper.setTo(demande.getEmail());
            helper.setSubject("Confirmation de votre demande d’inscription");

            String htmlContent = """
            <div style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 30px;">
                <div style="max-width: 600px; margin: auto; background: white; padding: 25px; border-radius: 10px; box-shadow: 0 0 8px rgba(0,0,0,0.05);">
                    <h2 style="text-align: center; color: #2c3e50;">Votre demande a bien été reçue 🩺</h2>
                    <p>Bonjour <strong>Dr %s %s</strong>,</p>
                    <p>Nous avons bien reçu votre demande d’inscription en tant que médecin sur la plateforme <strong>MonBonDocteur</strong>.</p>

                    <p>Notre équipe va examiner votre profil (spécialité : <strong>%s</strong>, grade : <strong>%s</strong>) dans les plus brefs délais.</p>

                    <p style="margin-top: 20px;">
                        Vous serez informé(e) par email dès que votre demande sera validée.
                    </p>

                    <p style="color: #888; font-size: 12px; text-align: center;">
                        Ce message vous est envoyé automatiquement. Merci de ne pas y répondre.
                    </p>

                    <p style="text-align: center; color: #aaa; margin-top: 20px;">— L’équipe MonBonDocteur</p>
                </div>
            </div>
        """.formatted(
                    demande.getPrenomMedecin(),
                    demande.getNomMedecin(),
                    demande.getRefSpecialite(),
                    demande.getRefGrade()
            );

            helper.setText(htmlContent, true);
            javaMailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}
