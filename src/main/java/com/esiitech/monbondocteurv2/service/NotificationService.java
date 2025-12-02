package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.model.Validation;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
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

            String logoSvgUrl = "https://moubengou-bodri.highticketdeveloper.com/image/LOGO-MON.svg";
            String logoPngUrl = "https://moubengou-bodri.highticketdeveloper.com/image/LOGO-MON.png"; // ← mets un PNG accessible ici

            String htmlContent = """
        <div style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 30px;">
          <div style="max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
            <div style="text-align:center;margin-bottom:16px">
              <picture>
                <source srcset="%s" type="image/svg+xml">
                <img src="%s" alt="MonBonDocteur" style="height:48px"/>
              </picture>
            </div>
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
        """.formatted(
                    logoSvgUrl,
                    logoPngUrl,
                    validation.getStructureSanitaire().getNomStructureSanitaire(),
                    validation.getCode()
            );

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (Exception e) {
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


    public void envoyerBienvenueAuMedecin(String email, String nomMedecin, Long idMedecin) {
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
                <p>Votre identifiant professionnel est : <strong>%s</strong></p>
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
        """.formatted(nomMedecin, idMedecin);

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }



    public void envoyerAccuseEnregistrementStructure(String email,
                                                    String nomStructureSanitaire) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("notify@eservices-gabon.com");
            helper.setTo(email);
            helper.setSubject("Demande d’enregistrement de la structure sanitaire « " + nomStructureSanitaire + " »");

            // (Optionnel) logo hébergé en PNG – évitez SVG en email
            String logoUrlPng = "https://moubengou-bodri.highticketdeveloper.com/image/LOGO-MON.png";

            String html = """
<div style="font-family: Arial, sans-serif; background:#fff; padding:20px;">
  <div style="max-width:900px; margin:auto;">
    <div style="text-align:left; margin-bottom:12px;">
      <img src="%1$s" alt="MonBonDocteur" height="26" style="display:inline-block;border:0;outline:none;text-decoration:none;">
    </div>

    <h1 style="font-size:22px; font-weight:600; margin:0 0 24px; color:#111;">
      Demande d’enregistrement de la structure sanitaire « %2$s »
    </h1>

    <p style="margin:0 0 18px; color:#111;">Bonjour Madame/Monsieur,</p>

    <p style="margin:0 0 12px; color:#111; line-height:1.5;">
      Nous accusons réception de votre demande de création de la structure sanitaire dénommée
      <strong>« %2$s »</strong>. Avant de créer votre compte, nous allons d’abord procéder à la vérification
      auprès du Ministère de la Santé des informations que vous nous avez communiquées.
      <em>Ce processus prendra quelques jours.</em>
    </p>

    <p style="margin:18px 0 12px; color:#111; line-height:1.5;">
      Si à l’issue de cette vérification tout va bien, nous allons vous communiquer à l’adresse
      <a href="mailto:%3$s" style="color:#1a73e8; text-decoration:underline;">%3$s</a>
      les informations vous permettant de vous connecter et d’exploiter notre plateforme.
    </p>

    <p style="margin:24px 0 0; color:#111;">
      Support technique de
      <a href="https://monbondocteur.com" style="color:#1a73e8; text-decoration:underline;">Monbondocteur</a>
    </p>
  </div>
</div>
""".formatted(
                    logoUrlPng,              // %1$s
                    nomStructureSanitaire,   // %2$s
                    email                    // %3$s (utilisé 2 fois)
            );

            helper.setText(html, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void envoyerIdentifiantsStructure(String email,
                                             String nomStructureSanitaire,
                                             String idStructure,
                                             String motDePassePlain) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("notify@eservices-gabon.com");
            helper.setTo(email);
            helper.setSubject("Bienvenue sur Mon Bon Docteur");

            String logoUrlPng = "https://moubengou-bodri.highticketdeveloper.com/image/LOGO-MON.png";
            String loginUrl   = "https://monbondocteur.com/login";

            String html = """
        <div style="font-family: Arial, sans-serif; background:#fff; padding:24px;">
          <div style="max-width:900px; margin:auto; color:#111; line-height:1.55;">
            
            <div style="text-align:left; margin-bottom:12px;">
              <img src="%1$s" alt="MonBonDocteur" height="26" style="display:inline-block;border:0;outline:none;text-decoration:none;">
            </div>

            <h1 style="font-size:24px; font-weight:700; margin:0 0 24px;">Bienvenue sur Mon Bon Docteur</h1>

            <p style="margin:0 0 16px;">Bonjour Madame/Monsieur,</p>

            <p style="margin:0 0 16px;">
              Après vérification des informations fournies, nous avons le plaisir de vous informer que votre
              structure sanitaire « <strong>%2$s</strong> » a été créée avec succès.
            </p>

            <p style="margin:16px 0 16px;">
              <span style="display:block; margin:6px 0;">Votre ID est : <strong>%3$s</strong></span>
              <span style="display:block; margin:6px 0;">Votre email de conextion : <strong>%6$s</strong></span>            
              <span style="display:block; margin:6px 0;">Votre mot de passe conextion : <strong>%4$s</strong></span>
            </p>

            <p style="margin:0 0 24px;">
              Avec ces informations, vous pouvez maintenant vous connecter à notre plateforme.
            </p>

            <div style="text-align:center; margin:28px 0 8px;">
              <a href="%5$s"
                 style="display:inline-block; padding:12px 28px; background:#3B82F6; color:#fff; text-decoration:none; border-radius:8px; font-weight:600;">
                 Mon bon docteur
              </a>
            </div>
          </div>
        </div>
        """.formatted(logoUrlPng, nomStructureSanitaire, idStructure, motDePassePlain, loginUrl, email);

            helper.setText(html, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void envoyerLienReinitMdpStructure(String email, String nomStructureSanitaire, String resetUrl) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("notify@eservices-gabon.com");
            helper.setTo(email);
            helper.setSubject("Réinitialisation de votre mot de passe");

            String logoUrlPng = "https://moubengou-bodri.highticketdeveloper.com/image/LOGO-MON.png";
            String html = """
        <div style="font-family: Arial, sans-serif; background:#fff; padding:20px;">
          <div style="max-width:900px; margin:auto;">
            <div style="text-align:left; margin-bottom:12px;">
              <img src="%1$s" alt="MonBonDocteur" height="40" style="display:inline-block;border:0;outline:none;text-decoration:none;">
            </div>

            <h1 style="font-size:22px; font-weight:600; margin:0 0 24px; color:#111;">
              Réinitialisation de mot de passe — %2$s
            </h1>

            <p style="margin:0 0 12px; color:#111; line-height:1.5;">
              Nous avons reçu une demande de réinitialisation du mot de passe pour votre compte.<br>
              Si vous êtes à l’origine de cette demande, cliquez sur le bouton ci-dessous pour définir un nouveau mot de passe.<br>
              <strong>Ce lien est valable pendant 60 minutes et utilisable une seule fois.</strong>
            </p>

            <p style="text-align:center; margin:24px 0;">
              <a href="%3$s" style="background:#1e87f0; color:#fff; padding:14px 28px; font-size:16px; border-radius:6px; text-decoration:none;">
                Réinitialiser mon mot de passe
              </a>
            </p>

            <p style="margin:0; color:#555;">
              Si le bouton ci-dessus ne fonctionne pas, copiez et collez ce lien dans votre navigateur :
            </p>
            <p style="word-break:break-all; color:#1a73e8; font-size:14px;">%3$s</p>

            <p style="margin-top:24px; color:#111;">— L’équipe MonBonDocteur</p>
          </div>
        </div>
        """.formatted(logoUrlPng, nomStructureSanitaire, resetUrl);

            helper.setText(html, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
