//package sn.sunufarmasi.notification.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import java.util.UUID;
//
///**
// * Service d'envoi d'emails
// *
// * @author WeCan
// * @since 1.0.0
// */
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//
//    @Value("${spring.mail.username:noreply@sunufarmasi.sn}")
//    private String fromEmail;
//
//    @Value("${app.name:SunuFarmasi}")
//    private String appName;
//
//    /**
//     * Envoyer un email simple
//     */
//    public String envoyer(String to, String sujet, String contenu) {
//        log.info("Envoi email à {}: {}", to, sujet);
//
//        try {
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setFrom(fromEmail);
//            message.setTo(to);
//            message.setSubject("[" + appName + "] " + sujet);
//            message.setText(contenu);
//
//            mailSender.send(message);
//
//            String ref = "EMAIL-" + UUID.randomUUID().toString().substring(0, 8);
//            log.info("Email envoyé: {}", ref);
//            return ref;
//
//        } catch (Exception e) {
//            log.error("Erreur envoi email: {}", e.getMessage());
//            throw new RuntimeException("Échec envoi email: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Envoyer un email HTML
//     */
//    public String envoyerHtml(String to, String sujet, String contenuHtml) {
//        log.info("Envoi email HTML à {}: {}", to, sujet);
//
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setFrom(fromEmail);
//            helper.setTo(to);
//            helper.setSubject("[" + appName + "] " + sujet);
//            helper.setText(contenuHtml, true);
//
//            mailSender.send(message);
//
//            String ref = "EMAIL-" + UUID.randomUUID().toString().substring(0, 8);
//            log.info("Email HTML envoyé: {}", ref);
//            return ref;
//
//        } catch (MessagingException e) {
//            log.error("Erreur envoi email HTML: {}", e.getMessage());
//            throw new RuntimeException("Échec envoi email: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Email de bienvenue
//     */
//    @Async
//    public void envoyerBienvenue(String email, String nom, String motDePasse) {
//        String sujet = "Bienvenue sur " + appName;
//        String contenu = String.format("""
//                Bonjour %s,
//
//                Bienvenue sur %s !
//
//                Votre compte a été créé avec succès.
//
//                Vos identifiants de connexion :
//                - Email : %s
//                - Mot de passe temporaire : %s
//
//                Nous vous recommandons de changer votre mot de passe lors de votre première connexion.
//
//                Cordialement,
//                L'équipe %s
//                """, nom, appName, email, motDePasse, appName);
//
//        envoyer(email, sujet, contenu);
//    }
//
//    /**
//     * Email de réinitialisation de mot de passe
//     */
//    @Async
//    public void envoyerResetPassword(String email, String nom, String token) {
//        String sujet = "Réinitialisation de mot de passe";
//        String lien = "https://app.sunufarmasi.sn/reset-password?token=" + token;
//        String contenu = String.format("""
//                Bonjour %s,
//
//                Vous avez demandé la réinitialisation de votre mot de passe.
//
//                Cliquez sur le lien suivant pour créer un nouveau mot de passe :
//                %s
//
//                Ce lien expire dans 24 heures.
//
//                Si vous n'avez pas fait cette demande, ignorez cet email.
//
//                Cordialement,
//                L'équipe %s
//                """, nom, lien, appName);
//
//        envoyer(email, sujet, contenu);
//    }
//
//    /**
//     * Email d'alerte stock
//     */
//    @Async
//    public void envoyerAlerteStock(String email, String pharmacie, String produit, String alerte) {
//        String sujet = "Alerte Stock - " + produit;
//        String contenu = String.format("""
//                Alerte Stock - %s
//
//                Pharmacie : %s
//                Produit : %s
//
//                %s
//
//                Connectez-vous à votre espace pour plus de détails.
//
//                Cordialement,
//                L'équipe %s
//                """, alerte, pharmacie, produit, alerte, appName);
//
//        envoyer(email, sujet, contenu);
//    }
//
//    /**
//     * Email de rappel garde
//     */
//    @Async
//    public void envoyerRappelGarde(String email, String nom, String dateGarde) {
//        String sujet = "Rappel - Garde du " + dateGarde;
//        String contenu = String.format("""
//                Bonjour %s,
//
//                Ceci est un rappel pour votre garde prévue le %s.
//
//                N'oubliez pas de vous préparer !
//
//                Cordialement,
//                L'équipe %s
//                """, nom, dateGarde, appName);
//
//        envoyer(email, sujet, contenu);
//    }
//
//    /**
//     * Email résumé quotidien
//     */
//    @Async
//    public void envoyerResumeQuotidien(String email, String nom, String resume) {
//        String sujet = "Résumé quotidien";
//        envoyer(email, sujet, "Bonjour " + nom + ",\n\n" + resume + "\n\nCordialement,\nL'équipe " + appName);
//    }
//}



package sn.sunufarmasi.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service d'envoi d'emails
 * Fonctionne en mode dégradé si JavaMailSender n'est pas configuré
 *
 * @author WeCan
 * @since 1.0.0
 */
@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final boolean emailEnabled;

    @Value("${spring.mail.username:noreply@sunufarmasi.sn}")
    private String fromEmail;

    @Value("${app.name:SunuFarmasi}")
    private String appName;

    /**
     * Constructeur avec injection optionnelle
     * Si JavaMailSender n'est pas configuré, le service fonctionne en mode log uniquement
     */
    @Autowired(required = false)
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        this.emailEnabled = (mailSender != null);

        if (!emailEnabled) {
            log.warn("⚠️ JavaMailSender non configuré - Les emails seront uniquement loggés");
        } else {
            log.info("✅ Service email initialisé");
        }
    }

    /**
     * Constructeur par défaut si aucun JavaMailSender n'est disponible
     */
    public EmailService() {
        this.mailSender = null;
        this.emailEnabled = false;
        log.warn("⚠️ EmailService initialisé sans JavaMailSender - Mode simulation");
    }

    /**
     * Envoyer un email simple
     */
    @Async
    public void sendEmail(String to, String subject, String content) {
        if (!emailEnabled) {
            logEmailSimulation(to, subject, content);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);  // true = HTML

            mailSender.send(message);
            log.info("✅ Email envoyé à: {}", to);

        } catch (MessagingException e) {
            log.error("❌ Erreur envoi email à {}: {}", to, e.getMessage());
        }
    }

    /**
     * Envoyer un email HTML
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        sendEmail(to, subject, htmlContent);
    }

    /**
     * Envoyer un code OTP par email
     */
    @Async
    public void sendOtpEmail(String to, String otp, String userName) {
        String subject = appName + " - Votre code de vérification";
        String content = buildOtpEmailTemplate(otp, userName);
        sendEmail(to, subject, content);
    }

    /**
     * Envoyer une notification de bienvenue
     */
    @Async
    public void sendWelcomeEmail(String to, String userName) {
        String subject = "Bienvenue sur " + appName + " !";
        String content = buildWelcomeEmailTemplate(userName);
        sendEmail(to, subject, content);
    }

    /**
     * Envoyer une notification de garde
     */
    @Async
    public void sendGardeNotification(String to, String pharmacieNom, String dateGarde, String typeGarde) {
        String subject = appName + " - Notification de garde";
        String content = buildGardeNotificationTemplate(pharmacieNom, dateGarde, typeGarde);
        sendEmail(to, subject, content);
    }

    /**
     * Envoyer un rappel de garde (J-1)
     */
    @Async
    public void sendGardeRappel(String to, String pharmacieNom, String dateGarde) {
        String subject = appName + " - Rappel: Garde demain";
        String content = buildGardeRappelTemplate(pharmacieNom, dateGarde);
        sendEmail(to, subject, content);
    }

    /**
     * Vérifier si le service email est actif
     */
    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES - TEMPLATES
    // ═══════════════════════════════════════════════════════════

    private void logEmailSimulation(String to, String subject, String content) {
        log.info("""
            
            ══════════════════════════════════════════════════════
            📧 EMAIL SIMULÉ (JavaMailSender non configuré)
            ══════════════════════════════════════════════════════
            À: {}
            Sujet: {}
            ──────────────────────────────────────────────────────
            {}
            ══════════════════════════════════════════════════════
            """, to, subject, content.replaceAll("<[^>]*>", "").trim());
    }

    private String buildOtpEmailTemplate(String otp, String userName) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <h2 style="color: #2E7D32;">%s</h2>
                <p>Bonjour %s,</p>
                <p>Votre code de vérification est :</p>
                <div style="background-color: #f5f5f5; padding: 20px; text-align: center; margin: 20px 0;">
                    <span style="font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #1976D2;">%s</span>
                </div>
                <p>Ce code expire dans <strong>10 minutes</strong>.</p>
                <p style="color: #666;">Si vous n'avez pas demandé ce code, ignorez cet email.</p>
                <hr style="margin-top: 30px;">
                <p style="font-size: 12px; color: #999;">L'équipe %s</p>
            </body>
            </html>
            """, appName, userName != null ? userName : "Cher utilisateur", otp, appName);
    }

    private String buildWelcomeEmailTemplate(String userName) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <h2 style="color: #2E7D32;">Bienvenue sur %s ! 🎉</h2>
                <p>Bonjour %s,</p>
                <p>Votre compte a été créé avec succès.</p>
                <p>Vous pouvez maintenant :</p>
                <ul>
                    <li>Rechercher les pharmacies de garde</li>
                    <li>Localiser les pharmacies proches</li>
                    <li>Gérer vos ordonnances</li>
                </ul>
                <p>Merci de votre confiance !</p>
                <hr style="margin-top: 30px;">
                <p style="font-size: 12px; color: #999;">L'équipe %s</p>
            </body>
            </html>
            """, appName, userName, appName);
    }

    private String buildGardeNotificationTemplate(String pharmacieNom, String dateGarde, String typeGarde) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <h2 style="color: #2E7D32;">📋 Notification de Garde</h2>
                <p>Bonjour,</p>
                <p>Vous êtes programmé(e) pour une garde :</p>
                <div style="background-color: #E8F5E9; padding: 15px; border-radius: 8px; margin: 20px 0;">
                    <p><strong>Pharmacie :</strong> %s</p>
                    <p><strong>Date :</strong> %s</p>
                    <p><strong>Type :</strong> %s</p>
                </div>
                <p>Merci de confirmer votre disponibilité.</p>
                <hr style="margin-top: 30px;">
                <p style="font-size: 12px; color: #999;">L'équipe %s</p>
            </body>
            </html>
            """, pharmacieNom, dateGarde, typeGarde, appName);
    }

    private String buildGardeRappelTemplate(String pharmacieNom, String dateGarde) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <h2 style="color: #FF9800;">⏰ Rappel - Garde demain</h2>
                <p>Bonjour,</p>
                <p>Ceci est un rappel pour votre garde de <strong>demain</strong> :</p>
                <div style="background-color: #FFF3E0; padding: 15px; border-radius: 8px; margin: 20px 0;">
                    <p><strong>Pharmacie :</strong> %s</p>
                    <p><strong>Date :</strong> %s</p>
                </div>
                <p>Bonne garde !</p>
                <hr style="margin-top: 30px;">
                <p style="font-size: 12px; color: #999;">L'équipe %s</p>
            </body>
            </html>
            """, pharmacieNom, dateGarde, appName);
    }
}