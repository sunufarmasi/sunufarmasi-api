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

    @Value("${spring.mail.username:sunufarmasi@gmail.com}")
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
     * Notification à une pharmacie quand elle est ajoutée à un syndicat
     */
    @Async
    public void notifierPharmacieDansSyndicat(String emailPharmacie, String nomPharmacie,
                                               String nomSyndicat, String codeSyndicat) {
        String subject = "Votre pharmacie a été intégrée au réseau " + nomSyndicat;
        String html = """
                <html><body style="font-family:Arial,sans-serif;background:#f5f5f5;padding:20px;">
                <div style="max-width:600px;margin:0 auto;background:white;border-radius:12px;overflow:hidden;">
                  <div style="background:linear-gradient(135deg,#10b981,#059669);padding:30px;text-align:center;">
                    <h1 style="color:white;margin:0;">SunuFarmasi</h1>
                    <p style="color:#d1fae5;margin:8px 0 0;">Notification Syndicat</p>
                  </div>
                  <div style="padding:30px;">
                    <h2 style="color:#1f2937;">Bienvenue dans le réseau de garde !</h2>
                    <p style="color:#4b5563;line-height:1.6;">
                      La pharmacie <strong>%s</strong> a été enregistrée dans le système de garde
                      de <strong>%s</strong> (code : <code>%s</code>).<br><br>
                      Elle apparaîtra désormais dans l'application mobile SunuFarmasi lors des gardes planifiées.
                    </p>
                    <div style="background:#ecfdf5;border-left:4px solid #10b981;padding:16px;margin:20px 0;border-radius:0 8px 8px 0;">
                      <p style="margin:0;color:#065f46;font-weight:500;">✅ Intégration réussie au réseau de garde</p>
                    </div>
                  </div>
                  <div style="background:#f9fafb;padding:20px;text-align:center;border-top:1px solid #e5e7eb;">
                    <p style="color:#9ca3af;font-size:12px;margin:0;">© 2026 SunuFarmasi</p>
                  </div>
                </div></body></html>
                """.formatted(nomPharmacie, nomSyndicat, codeSyndicat);
        sendEmail(emailPharmacie, subject, html);
    }

    /**
     * Email de bienvenue à un nouveau syndicat avec ses identifiants
     */
    @Async
    public void notifierNouveauSyndicat(String emailSyndicat, String nomSyndicat,
                                         String username, String motDePasse) {
        String subject = "🎉 Bienvenue sur SunuFarmasi — Vos accès sont prêts !";
        String html = """
                <html><body style="font-family:Arial,sans-serif;background:#f5f5f5;padding:20px;margin:0;">
                <div style="max-width:600px;margin:0 auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                  <div style="background:linear-gradient(135deg,#10b981,#059669);padding:32px 30px;text-align:center;">
                    <h1 style="color:white;margin:0;font-size:26px;letter-spacing:1px;">SunuFarmasi</h1>
                    <p style="color:#d1fae5;margin:8px 0 0;font-size:14px;">La plateforme numérique de la pharmacie au Sénégal</p>
                  </div>
                  <div style="padding:32px 30px;">
                    <h2 style="color:#1f2937;margin-top:0;">Bienvenue, %s ! 🎉</h2>
                    <p style="color:#374151;line-height:1.7;font-size:15px;">
                      Votre syndicat est maintenant connecté à <strong>SunuFarmasi</strong>, la plateforme
                      qui digitalise les pharmacies et les connecte à leur population.
                    </p>

                    <!-- Identifiants -->
                    <div style="background:#f3f4f6;border-radius:10px;padding:20px;margin:20px 0;">
                      <p style="margin:0 0 6px;color:#6b7280;font-size:13px;text-transform:uppercase;font-weight:600;">Vos identifiants de connexion</p>
                      <p style="margin:8px 0 4px;font-size:15px;"><strong>Username :</strong> <code style="background:#e5e7eb;padding:3px 8px;border-radius:4px;font-size:14px;">%s</code></p>
                      <p style="margin:4px 0 0;font-size:15px;"><strong>Mot de passe :</strong> <code style="background:#e5e7eb;padding:3px 8px;border-radius:4px;font-size:14px;">%s</code></p>
                    </div>
                    <p style="color:#ef4444;font-size:14px;margin-top:0;">⚠️ Changez votre mot de passe dès la première connexion.</p>

                    <!-- Ce que vous pouvez faire -->
                    <h3 style="color:#1f2937;margin-top:24px;">Ce que vous pouvez faire dès maintenant :</h3>
                    <ul style="color:#374151;line-height:1.9;font-size:14px;padding-left:20px;">
                      <li>📋 Gérer votre réseau de pharmacies membres</li>
                      <li>📅 Créer et publier les plannings de garde</li>
                      <li>📱 Vos pharmacies sont visibles en temps réel sur l'application mobile</li>
                      <li>💬 Contacter le support en cas de besoin</li>
                    </ul>
                    <p style="color:#374151;font-size:14px;line-height:1.7;">
                      Et ce n'est qu'un début — <strong>vérification de disponibilité de médicaments,
                      commande en ligne, livraison</strong> et d'autres fonctionnalités arrivent prochainement.
                    </p>

                    <!-- Phase de test -->
                    <div style="background:#fffbeb;border:2px solid #fbbf24;border-radius:10px;padding:20px;margin:24px 0;">
                      <p style="margin:0 0 8px;color:#92400e;font-weight:700;font-size:15px;">🚧 Phase de lancement — Accès offert</p>
                      <p style="margin:0;color:#78350f;line-height:1.7;font-size:14px;">
                        Vous faites partie des <strong>premiers syndicats</strong> à adopter SunuFarmasi.
                        Pendant cette phase de lancement, votre accès est <strong>entièrement offert</strong>.<br><br>
                        À la mise en production officielle, un abonnement mensuel sera proposé à
                        <strong>5 500 FCFA/mois</strong>,
                        incluant support prioritaire et toutes les fonctionnalités actuelles et futures.
                        Vos retours nous aident à construire la meilleure plateforme possible — merci de votre confiance !
                      </p>
                    </div>

                    <p style="color:#6b7280;font-size:13px;">Des questions ? Contactez-nous : <a href="mailto:sunufarmasi@gmail.com" style="color:#10b981;">sunufarmasi@gmail.com</a></p>
                  </div>
                  <div style="background:#f9fafb;padding:20px 30px;text-align:center;border-top:1px solid #e5e7eb;">
                    <p style="color:#9ca3af;font-size:12px;margin:0;">© 2026 SunuFarmasi · Notre Pharmacie, Votre Santé</p>
                  </div>
                </div></body></html>
                """.formatted(nomSyndicat, username, motDePasse);
        sendEmail(emailSyndicat, subject, html);
    }

    /**
     * Rappel de renouvellement abonnement — Pharmacie
     * Inclut numéro, lien Wave et instructions pour envoyer le reçu
     */
    @Async
    public void notifierRappelAbonnementPharmacie(String emailPharmacie, String nomPharmacie,
                                                   String telephone, String dateExpiration,
                                                   String montant) {
        String subject = "⚠️ Rappel : renouvellement abonnement SunuFarmasi";
        String waveLink = "https://pay.wave.com/m/M_sn_zChNJsQ96QXG/c/sn/";
        String contactEmail = "sunufarmasi@gmail.com";
        String html = """
                <html><body style="font-family:Arial,sans-serif;background:#f5f5f5;padding:20px;margin:0;">
                <div style="max-width:600px;margin:0 auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">

                  <!-- HEADER -->
                  <div style="background:linear-gradient(135deg,#f59e0b,#d97706);padding:32px 30px;text-align:center;">
                    <h1 style="color:white;margin:0;font-size:26px;letter-spacing:1px;">SunuFarmasi</h1>
                    <p style="color:#fef3c7;margin:6px 0 0;font-size:14px;">La plateforme numérique de la pharmacie au Sénégal</p>
                  </div>

                  <!-- CORPS -->
                  <div style="padding:32px 30px;">
                    <h2 style="color:#92400e;margin-top:0;">⚠️ Votre abonnement arrive à expiration</h2>

                    <p style="color:#374151;line-height:1.7;font-size:15px;">
                      Bonjour <strong>%s</strong>,<br><br>
                      Votre abonnement SunuFarmasi expire le <strong style="color:#dc2626;">%s</strong>.<br>
                      Sans renouvellement, votre pharmacie ne sera plus visible dans l'application
                      et ne pourra plus participer aux plannings de garde.
                    </p>

                    <!-- Détails pharmacie -->
                    <div style="background:#f3f4f6;border-radius:10px;padding:18px;margin:20px 0;">
                      <p style="margin:0 0 6px;color:#6b7280;font-size:13px;text-transform:uppercase;font-weight:600;">Votre pharmacie</p>
                      <p style="margin:0 0 4px;font-size:15px;font-weight:600;color:#111827;">%s</p>
                      <p style="margin:0;color:#6b7280;font-size:14px;">📞 %s</p>
                    </div>

                    <!-- Montant -->
                    <div style="background:#fffbeb;border:2px solid #fbbf24;border-radius:10px;padding:20px;margin:20px 0;text-align:center;">
                      <p style="margin:0 0 4px;color:#92400e;font-size:14px;font-weight:600;">MONTANT À PAYER</p>
                      <p style="margin:0;color:#d97706;font-size:32px;font-weight:900;">%s FCFA</p>
                      <p style="margin:4px 0 0;color:#92400e;font-size:13px;">Abonnement annuel — 1 an</p>
                    </div>

                    <!-- Payer via Wave -->
                    <h3 style="color:#1f2937;margin-bottom:12px;">Payer via Wave</h3>
                    <div style="background:#eff6ff;border:1px solid #bfdbfe;border-radius:10px;padding:20px;margin:16px 0;">
                      <p style="margin:0 0 14px;color:#374151;line-height:1.7;font-size:14px;">
                        Veuillez Payer SunuFarmasi avec Wave en cliquant sur ce bouton :<br>
                      </p>
                      <a href="%s" style="display:inline-block;background:#2563eb;color:white;padding:13px 28px;border-radius:8px;text-decoration:none;font-weight:700;font-size:15px;">
                        Payer sur Wave maintenant
                      </a>
                      <p style="margin:12px 0 0;color:#6b7280;font-size:12px;">
                        Ajoutez cet expéditeur à vos contacts pour rendre le lien cliquable.
                      </p>
                    </div>

                    <!-- Instructions reçu -->
                    <div style="background:#ecfdf5;border-left:4px solid #10b981;padding:16px 20px;margin:20px 0;border-radius:0 10px 10px 0;">
                      <p style="margin:0 0 8px;color:#065f46;font-weight:700;font-size:14px;">📧 Après paiement — Instructions importantes</p>
                      <p style="margin:0;color:#047857;line-height:1.7;font-size:14px;">
                        Une fois votre paiement Wave effectué, <strong>envoyez votre reçu de paiement
                        par email à :</strong><br>
                        <a href="mailto:%s" style="color:#059669;font-weight:700;font-size:15px;">%s</a><br><br>
                        Votre abonnement sera renouvelé sous <strong>24h ouvrables</strong>.
                      </p>
                    </div>

                    <p style="color:#9ca3af;font-size:13px;margin-top:24px;">
                      Des questions ? Contactez-nous : <a href="mailto:%s" style="color:#10b981;">%s</a>
                    </p>
                  </div>

                  <!-- FOOTER -->
                  <div style="background:#f9fafb;padding:20px 30px;text-align:center;border-top:1px solid #e5e7eb;">
                    <p style="color:#9ca3af;font-size:12px;margin:0;">© 2026 SunuFarmasi · Notre Pharmacie, Votre Santé</p>
                  </div>
                </div></body></html>
                """.formatted(nomPharmacie, dateExpiration, nomPharmacie, telephone,
                              montant, waveLink, waveLink, waveLink,
                              contactEmail, contactEmail, contactEmail, contactEmail);
        sendEmail(emailPharmacie, subject, html);
    }

    /**
     * Confirmation de paiement / activation abonnement — Pharmacie
     */
    @Async
    public void notifierConfirmationPaiementPharmacie(String emailPharmacie, String nomPharmacie,
                                                       String telephone, String dateExpiration,
                                                       String montant) {
        String subject = "✅ Paiement confirmé — Abonnement SunuFarmasi actif";
        String html = """
                <html><body style="font-family:Arial,sans-serif;background:#f5f5f5;padding:20px;margin:0;">
                <div style="max-width:600px;margin:0 auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">

                  <!-- HEADER -->
                  <div style="background:linear-gradient(135deg,#10b981,#059669);padding:32px 30px;text-align:center;">
                    <h1 style="color:white;margin:0;font-size:26px;letter-spacing:1px;">SunuFarmasi</h1>
                    <p style="color:#d1fae5;margin:6px 0 0;font-size:14px;">La plateforme numérique de la pharmacie au Sénégal</p>
                  </div>

                  <!-- CORPS -->
                  <div style="padding:32px 30px;">
                    <div style="text-align:center;margin-bottom:24px;">
                      <div style="display:inline-block;background:#ecfdf5;border-radius:50%%;width:64px;height:64px;line-height:64px;font-size:32px;">✅</div>
                    </div>
                    <h2 style="color:#065f46;text-align:center;margin-top:0;">Paiement confirmé !</h2>

                    <p style="color:#374151;line-height:1.7;font-size:15px;">
                      Bonjour <strong>%s</strong>,<br><br>
                      Votre paiement a bien été enregistré. Votre pharmacie est maintenant
                      <strong style="color:#059669;">active</strong> sur SunuFarmasi et visible dans l'application mobile.
                    </p>

                    <!-- Récapitulatif -->
                    <div style="background:#f3f4f6;border-radius:10px;padding:20px;margin:20px 0;">
                      <p style="margin:0 0 6px;color:#6b7280;font-size:13px;text-transform:uppercase;font-weight:600;">Récapitulatif</p>
                      <table style="width:100%%;border-collapse:collapse;">
                        <tr><td style="padding:6px 0;color:#6b7280;font-size:14px;">Pharmacie</td><td style="padding:6px 0;font-weight:600;color:#111827;text-align:right;font-size:14px;">%s</td></tr>
                        <tr><td style="padding:6px 0;color:#6b7280;font-size:14px;">Téléphone</td><td style="padding:6px 0;font-weight:600;color:#111827;text-align:right;font-size:14px;">%s</td></tr>
                        <tr><td style="padding:6px 0;color:#6b7280;font-size:14px;">Montant payé</td><td style="padding:6px 0;font-weight:700;color:#059669;text-align:right;font-size:16px;">%s FCFA</td></tr>
                        <tr style="border-top:1px solid #e5e7eb;"><td style="padding:10px 0 6px;color:#6b7280;font-size:14px;">Abonnement valide jusqu'au</td><td style="padding:10px 0 6px;font-weight:700;color:#1d4ed8;text-align:right;font-size:15px;">%s</td></tr>
                      </table>
                    </div>

                    <div style="background:#ecfdf5;border-left:4px solid #10b981;padding:16px 20px;margin:20px 0;border-radius:0 10px 10px 0;">
                      <p style="margin:0;color:#065f46;font-size:14px;line-height:1.6;">
                        🏥 Votre pharmacie est désormais visible par les patients dans l'application.<br>
                        📅 Votre syndicat peut maintenant vous inclure dans les plannings de garde.
                      </p>
                    </div>

                    <p style="color:#9ca3af;font-size:13px;margin-top:24px;">
                      Des questions ? <a href="mailto:sunufarmasi@gmail.com" style="color:#10b981;">sunufarmasi@gmail.com</a>
                    </p>
                  </div>

                  <!-- FOOTER -->
                  <div style="background:#f9fafb;padding:20px 30px;text-align:center;border-top:1px solid #e5e7eb;">
                    <p style="color:#9ca3af;font-size:12px;margin:0;">© 2026 SunuFarmasi · Notre Pharmacie, Votre Santé</p>
                  </div>
                </div></body></html>
                """.formatted(nomPharmacie, nomPharmacie, telephone, montant, dateExpiration);
        sendEmail(emailPharmacie, subject, html);
    }

    /**
     * Rappel de paiement d'abonnement
     */
    @Async
    public void notifierRappelPaiement(String emailSyndicat, String nomSyndicat,
                                        String montant, String dateEcheance) {
        String subject = "⚠️ Rappel de paiement — SunuFarmasi";
        String html = """
                <html><body style="font-family:Arial,sans-serif;background:#f5f5f5;padding:20px;">
                <div style="max-width:600px;margin:0 auto;background:white;border-radius:12px;overflow:hidden;">
                  <div style="background:linear-gradient(135deg,#f59e0b,#d97706);padding:30px;text-align:center;">
                    <h1 style="color:white;margin:0;">⚠️ Rappel de paiement</h1>
                  </div>
                  <div style="padding:30px;">
                    <p style="color:#4b5563;line-height:1.6;">
                      Bonjour <strong>%s</strong>,<br><br>
                      Votre abonnement arrive à échéance le <strong>%s</strong>.<br>
                      Montant dû : <strong style="color:#10b981;font-size:18px;">%s FCFA</strong>
                    </p>
                    <div style="background:#fffbeb;border-left:4px solid #f59e0b;padding:16px;margin:20px 0;border-radius:0 8px 8px 0;">
                      <p style="margin:0;color:#92400e;">Sans paiement, votre compte sera suspendu à l'échéance.</p>
                    </div>
                    <p style="color:#6b7280;font-size:14px;">Contactez <a href="mailto:sunufarmasi@gmail.com">sunufarmasi@gmail.com</a> pour effectuer le paiement.</p>
                  </div>
                  <div style="background:#f9fafb;padding:20px;text-align:center;border-top:1px solid #e5e7eb;">
                    <p style="color:#9ca3af;font-size:12px;margin:0;">© 2026 SunuFarmasi</p>
                  </div>
                </div></body></html>
                """.formatted(nomSyndicat, dateEcheance, montant);
        sendEmail(emailSyndicat, subject, html);
    }

    /**
     * Notification réponse à un ticket
     */
    @Async
    public void notifierReponseTicket(String emailSyndicat, String nomSyndicat,
                                       String sujetTicket, String reponse) {
        String subject = "Réponse à votre ticket — SunuFarmasi";
        String html = """
                <html>
                    <body style="font-family:Arial,sans-serif;background:#f5f5f5;padding:20px;">
                        <div style="max-width:600px;margin:0 auto;background:white;border-radius:12px;overflow:hidden;">
                          <div style="background:linear-gradient(135deg,#10b981,#059669);padding:30px;text-align:center;">
                            <h1 style="color:white;margin:0;">SunuFarmasi Support</h1>
                          </div>
                          <div style="padding:30px;">
                            <h2 style="color:#1f2937;">Réponse à votre ticket</h2>
                            <p style="color:#4b5563;">Bonjour <strong>%s</strong>,</p>
                            <p style="color:#4b5563;">Concernant : <em>%s</em></p>
                            <div style="background:#f3f4f6;border-radius:8px;padding:20px;margin:20px 0;border-left:4px solid #10b981;">
                              <p style="margin:0;color:#1f2937;line-height:1.6;">%s</p>
                            </div>
                            <p style="color:#6b7280;font-size:14px;">Pour toute question complémentaire, créez un nouveau ticket depuis votre tableau de bord.</p>
                          </div>
                          <div style="background:#f9fafb;padding:20px;text-align:center;border-top:1px solid #e5e7eb;">
                            <p style="color:#9ca3af;font-size:12px;margin:0;">© 2026 SunuFarmasi</p>
                          </div>
                        </div>
                    </body>
                </html>
                """.formatted(nomSyndicat, sujetTicket, reponse);
        sendEmail(emailSyndicat, subject, html);
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

//pharmacien.almadies@gmail.com
//+221775678901