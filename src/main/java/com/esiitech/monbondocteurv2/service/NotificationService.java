package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.model.Validation;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Year;

@Service
public class NotificationService {

    private final JavaMailSender javaMailSender;

    @Value("${app.front.url:https://monbondocteur.com}")
    private String frontUrl;

    @Value("${app.support.email:contact@monbondocteur.com}")
    private String supportEmail;

    @Value("${app.mail.from:noreply@solutech-one.com}")
    private String fromEmail;

    private static final String LOGO_ID = "logoImage";

    private static final String PRIMARY = "#00A259";
    private static final String SECONDARY = "#5AB379";
    private static final String DARK = "#1a1a2e";
    private static final String GRAY = "#f4f5f7";
    private static final String TEXT = "#2d3748";
    private static final String MUTED = "#718096";
    private static final String BORDER = "#e2e8f0";

    public NotificationService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    // =====================================================
    // LOGO EMBARQUÉ
    // =====================================================

    private void attachLogoIfAvailable(MimeMessageHelper helper) {
        try {
            ClassPathResource resource = new ClassPathResource("static/images/logo.png");

            if (!resource.exists()) {
                return;
            }

            byte[] logoBytes = resource.getInputStream().readAllBytes();

            helper.addInline(
                    LOGO_ID,
                    new ByteArrayResource(logoBytes),
                    "image/png"
            );
        } catch (Exception ignored) {
            // Si le logo n'existe pas, l'email part quand même.
        }
    }

    // =====================================================
    // TEMPLATE UNIQUE
    // =====================================================

    private String buildTemplate(String title, String body, String actionLabel, String actionUrl) {
        return """
        <!DOCTYPE html>
        <html lang="fr">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
        </head>
        <body style="margin:0;padding:0;background-color:%s;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;">
            <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:%s;">
                <tr>
                    <td align="center" style="padding:40px 16px;">
                        <table width="100%%" cellpadding="0" cellspacing="0" style="max-width:520px;background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.04);">
                            
                            <tr>
                                <td style="background-color:%s;padding:28px 24px;text-align:center;">
                                    <img src="cid:%s" alt="monBonDocteur" style="height:36px;margin-bottom:12px;" />
                                    <h2 style="margin:0;font-size:18px;font-weight:600;color:#ffffff;line-height:1.3;">%s</h2>
                                </td>
                            </tr>
                            
                            <tr>
                                <td style="padding:28px 24px;">
                                    %s
                                </td>
                            </tr>
                            
                            %s
                            
                            <tr>
                                <td style="padding:0 24px;">
                                    <hr style="border:0;border-top:1px solid %s;margin:0;" />
                                </td>
                            </tr>
                            
                            <tr>
                                <td style="padding:20px 24px;text-align:center;">
                                    <p style="margin:0 0 6px;font-size:12px;color:%s;font-weight:500;">
                                        monBon<span style="color:%s;">Docteur</span>
                                    </p>
                                    <p style="margin:0;font-size:11px;color:%s;line-height:1.5;">
                                        Cet email a été envoyé automatiquement.<br/>
                                        <a href="mailto:%s" style="color:%s;text-decoration:none;">%s</a>
                                    </p>
                                </td>
                            </tr>
                            
                        </table>
                        
                        <p style="margin-top:14px;font-size:11px;color:%s;">
                            &copy; %d monBonDocteur. Tous droits r&eacute;serv&eacute;s.
                        </p>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """.formatted(
                GRAY,
                GRAY,
                PRIMARY,
                LOGO_ID,
                escapeHtml(title),
                body,
                actionLabel != null && actionUrl != null
                        ? buildActionButton(actionLabel, actionUrl)
                        : "",
                BORDER,
                MUTED,
                SECONDARY,
                MUTED,
                escapeHtml(supportEmail),
                SECONDARY,
                escapeHtml(supportEmail),
                MUTED,
                Year.now().getValue()
        );
    }

    private String buildActionButton(String label, String url) {
        return """
        <tr>
            <td style="padding:4px 24px 28px;text-align:center;">
                <a href="%s" style="display:inline-block;padding:12px 32px;background-color:%s;color:#ffffff;text-decoration:none;border-radius:6px;font-size:14px;font-weight:600;">
                    %s
                </a>
            </td>
        </tr>
        """.formatted(
                escapeHtml(url),
                PRIMARY,
                escapeHtml(label)
        );
    }

    // =====================================================
    // CARTES EMAIL
    // =====================================================

    private String buildActivationCard(String nom, String code) {
        return """
        <p style="margin:0 0 6px;font-size:15px;color:%s;">Bonjour <strong style="color:%s;">%s</strong>,</p>
        <p style="margin:0 0 20px;font-size:14px;color:%s;line-height:1.6;">
            Merci d'avoir rejoint <strong>monBonDocteur</strong>. Pour activer votre compte, utilisez le code ci-dessous&nbsp;:
        </p>
        
        <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:20px;">
            <tr>
                <td style="background-color:%s;border-radius:6px;padding:18px;text-align:center;">
                    <span style="font-size:28px;font-weight:700;color:%s;letter-spacing:5px;font-family:'Courier New',monospace;">%s</span>
                </td>
            </tr>
        </table>
        
        <p style="margin:0 0 6px;font-size:12px;color:%s;">
            Ce code expire dans <strong>10 minutes</strong>.
        </p>
        <p style="margin:0;font-size:12px;color:%s;">
            Si vous n'&ecirc;tes pas &agrave; l'origine de cette demande, ignorez cet email.
        </p>
        """.formatted(
                TEXT,
                DARK,
                escapeHtml(nom),
                TEXT,
                GRAY,
                PRIMARY,
                escapeHtml(code),
                MUTED,
                MUTED
        );
    }

    private String buildRdvPatientCard(String nomPatient, String nomMedecin, String date, String heure, String motif) {
        String dateSafe = isBlank(date) ? "Non précisée" : date;
        String heureSafe = isBlank(heure) ? "Non précisée" : heure;

        String motifRow = "";
        if (!isBlank(motif)) {
            motifRow = """
            <tr>
                <td style="padding:5px 0;font-size:13px;color:%s;">Motif</td>
                <td style="padding:5px 0;font-size:13px;color:%s;font-weight:500;">%s</td>
            </tr>
            """.formatted(MUTED, DARK, escapeHtml(motif));
        }

        return """
        <p style="margin:0 0 6px;font-size:15px;color:%s;">Bonjour <strong style="color:%s;">%s</strong>,</p>
        <p style="margin:0 0 20px;font-size:14px;color:%s;line-height:1.6;">
            Votre rendez-vous a &eacute;t&eacute; <strong style="color:%s;">confirm&eacute;</strong> avec le <strong>Dr %s</strong>.
        </p>
        
        <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:20px;border:1px solid %s;border-radius:6px;overflow:hidden;">
            <tr>
                <td style="background-color:%s;padding:10px 16px;font-size:11px;font-weight:600;color:%s;text-transform:uppercase;letter-spacing:0.5px;">
                    D&eacute;tails du rendez-vous
                </td>
            </tr>
            <tr>
                <td style="padding:14px 16px;">
                    <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr>
                            <td style="padding:5px 0;font-size:13px;color:%s;width:90px;">Date</td>
                            <td style="padding:5px 0;font-size:13px;color:%s;font-weight:500;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding:5px 0;font-size:13px;color:%s;">Heure</td>
                            <td style="padding:5px 0;font-size:13px;color:%s;font-weight:500;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding:5px 0;font-size:13px;color:%s;">M&eacute;decin</td>
                            <td style="padding:5px 0;font-size:13px;color:%s;font-weight:500;">Dr %s</td>
                        </tr>
                        %s
                    </table>
                </td>
            </tr>
        </table>
        """.formatted(
                TEXT,
                DARK,
                escapeHtml(nomPatient),
                TEXT,
                SECONDARY,
                escapeHtml(nomMedecin),
                BORDER,
                GRAY,
                MUTED,
                MUTED,
                DARK,
                escapeHtml(dateSafe),
                MUTED,
                DARK,
                escapeHtml(heureSafe),
                MUTED,
                DARK,
                escapeHtml(nomMedecin),
                motifRow
        );
    }

    private String buildRdvMedecinCard(String nomMedecin, String nomPatient, String date, String heure) {
        String dateSafe = isBlank(date) ? "Non précisée" : date;
        String heureSafe = isBlank(heure) ? "Non précisée" : heure;

        return """
        <p style="margin:0 0 6px;font-size:15px;color:%s;">Bonjour <strong style="color:%s;">Dr %s</strong>,</p>
        <p style="margin:0 0 20px;font-size:14px;color:%s;line-height:1.6;">
            Un nouveau rendez-vous a &eacute;t&eacute; enregistr&eacute; avec le patient <strong>%s</strong>.
        </p>
        
        <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:20px;border:1px solid %s;border-radius:6px;overflow:hidden;">
            <tr>
                <td style="background-color:%s;padding:10px 16px;font-size:11px;font-weight:600;color:%s;text-transform:uppercase;letter-spacing:0.5px;">
                    D&eacute;tails du rendez-vous
                </td>
            </tr>
            <tr>
                <td style="padding:14px 16px;">
                    <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr>
                            <td style="padding:5px 0;font-size:13px;color:%s;width:90px;">Date</td>
                            <td style="padding:5px 0;font-size:13px;color:%s;font-weight:500;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding:5px 0;font-size:13px;color:%s;">Heure</td>
                            <td style="padding:5px 0;font-size:13px;color:%s;font-weight:500;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding:5px 0;font-size:13px;color:%s;">Patient</td>
                            <td style="padding:5px 0;font-size:13px;color:%s;font-weight:500;">%s</td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
        """.formatted(
                TEXT,
                DARK,
                escapeHtml(nomMedecin),
                TEXT,
                escapeHtml(nomPatient),
                BORDER,
                GRAY,
                MUTED,
                MUTED,
                DARK,
                escapeHtml(dateSafe),
                MUTED,
                DARK,
                escapeHtml(heureSafe),
                MUTED,
                DARK,
                escapeHtml(nomPatient)
        );
    }

    private String buildWelcomeCard(String nom, String role, String identifiant, String motDePasse) {
        String passwordRow = "";

        if (!isBlank(motDePasse)) {
            passwordRow = """
            <tr>
                <td style="padding:5px 0;font-size:13px;color:%s;">Mot de passe</td>
                <td style="padding:5px 0;font-size:13px;color:%s;font-weight:600;font-family:'Courier New',monospace;">%s</td>
            </tr>
            """.formatted(MUTED, DARK, escapeHtml(motDePasse));
        }

        String passwordNote = !isBlank(motDePasse)
                ? "Pensez &agrave; changer votre mot de passe apr&egrave;s votre premi&egrave;re connexion."
                : "Vous pouvez maintenant vous connecter &agrave; votre espace.";

        return """
        <p style="margin:0 0 6px;font-size:15px;color:%s;">Bonjour <strong style="color:%s;">%s</strong>,</p>
        <p style="margin:0 0 20px;font-size:14px;color:%s;line-height:1.6;">
            Bienvenue sur <strong>monBonDocteur</strong>. Votre compte <strong style="color:%s;">%s</strong> a &eacute;t&eacute; cr&eacute;&eacute; avec succ&egrave;s.
        </p>
        
        <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:20px;border:1px solid %s;border-radius:6px;overflow:hidden;">
            <tr>
                <td style="background-color:%s;padding:10px 16px;font-size:11px;font-weight:600;color:%s;text-transform:uppercase;letter-spacing:0.5px;">
                    Vos identifiants
                </td>
            </tr>
            <tr>
                <td style="padding:14px 16px;">
                    <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr>
                            <td style="padding:5px 0;font-size:13px;color:%s;width:110px;">Identifiant</td>
                            <td style="padding:5px 0;font-size:13px;color:%s;font-weight:600;font-family:'Courier New',monospace;">%s</td>
                        </tr>
                        %s
                    </table>
                </td>
            </tr>
        </table>
        
        <p style="margin:0;font-size:12px;color:%s;">
            %s
        </p>
        """.formatted(
                TEXT,
                DARK,
                escapeHtml(nom),
                TEXT,
                SECONDARY,
                escapeHtml(role),
                BORDER,
                GRAY,
                MUTED,
                MUTED,
                DARK,
                escapeHtml(identifiant),
                passwordRow,
                MUTED,
                passwordNote
        );
    }

    private String buildSimpleCard(String nom, String message) {
        return """
        <p style="margin:0 0 6px;font-size:15px;color:%s;">Bonjour <strong style="color:%s;">%s</strong>,</p>
        <p style="margin:0;font-size:14px;color:%s;line-height:1.6;">%s</p>
        """.formatted(
                TEXT,
                DARK,
                escapeHtml(nom),
                TEXT,
                message
        );
    }

    // =====================================================
    // ACTIVATION
    // =====================================================

    public void envoyer(Validation validation) {
        envoyerActivation(
                validation.getUtilisateur().getEmail(),
                validation.getUtilisateur().getNom(),
                validation.getCode()
        );
    }

    public void envoyerMedecin(Validation validation) {
        envoyerActivation(
                validation.getMedecin().getEmail(),
                validation.getMedecin().getNomMedecin(),
                validation.getCode()
        );
    }

    public void envoyerStructure(Validation validation) {
        envoyerActivation(
                validation.getStructureSanitaire().getEmail(),
                validation.getStructureSanitaire().getNomStructureSanitaire(),
                validation.getCode()
        );
    }

    private void envoyerActivation(String email, String nom, String code) {
        String encodedCode = URLEncoder.encode(code, StandardCharsets.UTF_8);
        String activationUrl = appUrl("/activation?code=" + encodedCode);

        String body = buildActivationCard(nom, code);
        String html = buildTemplate("Activation de votre compte", body, "Activer mon compte", activationUrl);

        sendEmail(email, "Code d'activation", html);
    }

    // =====================================================
    // RENDEZ-VOUS
    // =====================================================

    public void envoyerAuPatient(String email, String nomPatient, String nomMedecin) {
        envoyerAuPatient(email, nomPatient, nomMedecin, null, null, null);
    }

    public void envoyerAuPatient(String email, String nomPatient, String nomMedecin, String date, String heure, String motif) {
        String body = buildRdvPatientCard(nomPatient, nomMedecin, date, heure, motif)
                + "<p style='margin:0;font-size:13px;color:" + MUTED + ";'>Vous pouvez consulter et g&eacute;rer vos rendez-vous depuis votre espace patient.</p>";

        String html = buildTemplate(
                "Rendez-vous confirmé",
                body,
                "Voir mes rendez-vous",
                appUrl("/mes-rendez-vous")
        );

        sendEmail(email, "Confirmation de votre rendez-vous", html);
    }

    public void envoyerAuMedecin(String email, String nomMedecin, String nomPatient) {
        envoyerAuMedecin(email, nomMedecin, nomPatient, null, null);
    }

    public void envoyerAuMedecin(String email, String nomMedecin, String nomPatient, String date, String heure) {
        String body = buildRdvMedecinCard(nomMedecin, nomPatient, date, heure)
                + "<p style='margin:0;font-size:13px;color:" + MUTED + ";'>Connectez-vous pour consulter votre agenda.</p>";

        String html = buildTemplate(
                "Nouveau rendez-vous",
                body,
                "Voir mon agenda",
                appUrl("/medecin/agenda")
        );

        sendEmail(email, "Nouveau rendez-vous", html);
    }

    // =====================================================
    // BIENVENUE / IDENTIFIANTS
    // =====================================================

    public void envoyerBienvenueAuMedecin(String email, String nomMedecin, Long idMedecin) {
        envoyerBienvenueAuMedecin(
                email,
                nomMedecin,
                idMedecin != null ? String.valueOf(idMedecin) : "",
                null
        );
    }

    public void envoyerBienvenueAuMedecin(String email, String nomMedecin, String identifiant, String motDePasse) {
        String body = buildWelcomeCard("Dr " + nomMedecin, "Médecin", identifiant, motDePasse);

        String html = buildTemplate(
                "Bienvenue Dr " + escapeHtml(nomMedecin),
                body,
                "Accéder à mon espace",
                appUrl("/login")
        );

        sendEmail(email, "Bienvenue sur monBonDocteur", html);
    }

    public void envoyerAccuseEnregistrementStructure(String email, String nomStructureSanitaire) {
        String body = buildSimpleCard(
                nomStructureSanitaire,
                "Votre demande d'enregistrement a bien &eacute;t&eacute; re&ccedil;ue. Notre &eacute;quipe va l'examiner dans les plus brefs d&eacute;lais. Vous recevrez un email d&egrave;s que votre compte sera activ&eacute;."
        );

        String html = buildTemplate("Demande reçue", body, null, null);

        sendEmail(email, "Confirmation de réception", html);
    }

    public void envoyerIdentifiantsStructure(String email, String nomStructureSanitaire, String idStructure, String motDePassePlain) {
        String body = buildWelcomeCard(
                nomStructureSanitaire,
                "Structure sanitaire",
                idStructure,
                motDePassePlain
        );

        String html = buildTemplate(
                "Vos identifiants",
                body,
                "Se connecter",
                appUrl("/login")
        );

        sendEmail(email, "Vos identifiants monBonDocteur", html);
    }

    public void envoyerLienReinitMdpStructure(String email, String nomStructureSanitaire, String resetUrl) {
        String body = buildSimpleCard(
                nomStructureSanitaire,
                "Vous avez demand&eacute; la r&eacute;initialisation de votre mot de passe. Cliquez sur le bouton ci-dessous pour d&eacute;finir un nouveau mot de passe. Ce lien est valable pendant 60 minutes et utilisable une seule fois."
        );

        String html = buildTemplate(
                "Réinitialisation du mot de passe",
                body,
                "Réinitialiser mon mot de passe",
                resetUrl
        );

        sendEmail(email, "Réinitialisation de mot de passe", html);
    }

    // =====================================================
    // ENVOI FINAL
    // =====================================================

    private void sendEmail(String to, String subject, String html) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            attachLogoIfAvailable(helper);

            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur envoi email: " + e.getMessage(), e);
        }
    }

    // =====================================================
    // UTILITAIRES
    // =====================================================

    private String appUrl(String path) {
        if (path == null || path.isBlank()) {
            return frontUrl;
        }

        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }

        String base = frontUrl != null ? frontUrl.trim() : "https://monbondocteur.com";

        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        return base + path;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String escapeHtml(Object value) {
        if (value == null) {
            return "";
        }

        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}