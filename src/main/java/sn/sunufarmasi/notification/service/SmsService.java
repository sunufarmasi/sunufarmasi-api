package sn.sunufarmasi.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service d'envoi de SMS
 * Support Orange SMS API Sénégal et autres providers
 * Fonctionne en mode simulation si non configuré
 *
 * @author WeCan
 * @since 1.0.0
 */
@Service
@Slf4j
public class SmsService {

    private final RestTemplate restTemplate;
    private final boolean smsConfigured;

    @Value("${sms.provider:ORANGE}")
    private String provider;

    @Value("${sms.api.url:https://api.orange.com/smsmessaging/v1/outbound}")
    private String apiUrl;

    @Value("${sms.api.key:}")
    private String apiKey;

    @Value("${sms.sender:SunuFarmasi}")
    private String sender;

    @Value("${sms.enabled:false}")
    private boolean enabled;

    /**
     * Constructeur avec injection optionnelle de RestTemplate
     */
    @Autowired(required = false)
    public SmsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.smsConfigured = (restTemplate != null);

        if (!smsConfigured) {
            log.warn("⚠️ RestTemplate non configuré - Les SMS seront uniquement loggés");
        } else {
            log.info("✅ Service SMS initialisé");
        }
    }

    /**
     * Constructeur par défaut
     */
    public SmsService() {
        this.restTemplate = null;
        this.smsConfigured = false;
        log.warn("⚠️ SmsService initialisé sans RestTemplate - Mode simulation");
    }

    /**
     * Envoyer un SMS
     */
    public String envoyer(String telephone, String message) {
        log.info("📱 Envoi SMS à {}: {}", masquerNumero(telephone), tronquer(message, 50));

        // Mode simulation si désactivé ou non configuré
        if (!enabled || !smsConfigured || apiKey == null || apiKey.isEmpty()) {
            return logSmsSimulation(telephone, message);
        }

        // Formater le numéro
        String numero = formaterNumero(telephone);

        // Tronquer le message à 160 caractères
        String msg = tronquer(message, 160);

        try {
            return switch (provider.toUpperCase()) {
                case "ORANGE" -> envoyerOrange(numero, msg);
                case "TWILIO" -> envoyerTwilio(numero, msg);
                case "INFOBIP" -> envoyerInfobip(numero, msg);
                default -> {
                    log.warn("Provider SMS inconnu: {}", provider);
                    yield logSmsSimulation(telephone, message);
                }
            };
        } catch (Exception e) {
            log.error("❌ Erreur envoi SMS: {}", e.getMessage());
            // En cas d'erreur, logger au lieu de throw
            return logSmsSimulation(telephone, message);
        }
    }

    /**
     * Simuler l'envoi et logger
     */
    private String logSmsSimulation(String telephone, String message) {
        String refId = "SIM-" + UUID.randomUUID().toString().substring(0, 8);

        log.info("""
            
            ══════════════════════════════════════════════════════
            📱 SMS SIMULÉ (sms.enabled=false ou non configuré)
            ══════════════════════════════════════════════════════
            À: {}
            Message: {}
            Référence: {}
            ══════════════════════════════════════════════════════
            """, telephone, tronquer(message, 160), refId);

        return refId;
    }

    /**
     * Orange SMS API Sénégal
     */
    private String envoyerOrange(String numero, String message) {
        log.debug("Envoi via Orange SMS API");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        Map<String, String> outbound = new HashMap<>();
        outbound.put("address", "tel:" + numero);
        outbound.put("senderAddress", "tel:" + sender);
        outbound.put("message", message);
        body.put("outboundSMSMessageRequest", outbound);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + "/tel:" + sender + "/requests",
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String ref = "ORANGE-" + UUID.randomUUID().toString().substring(0, 8);
                log.info("✅ SMS envoyé via Orange: {}", ref);
                return ref;
            } else {
                throw new RuntimeException("Erreur Orange API: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ Erreur Orange SMS: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Twilio SMS API
     */
    private String envoyerTwilio(String numero, String message) {
        log.debug("Envoi via Twilio");
        // TODO: Implémenter Twilio
        String ref = "TWILIO-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("SMS Twilio (TODO): {}", ref);
        return ref;
    }

    /**
     * Infobip SMS API
     */
    private String envoyerInfobip(String numero, String message) {
        log.debug("Envoi via Infobip");
        // TODO: Implémenter Infobip
        String ref = "INFOBIP-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("SMS Infobip (TODO): {}", ref);
        return ref;
    }

    /**
     * Formater le numéro au format international sénégalais
     */
    private String formaterNumero(String telephone) {
        if (telephone == null) return "";

        String num = telephone.replaceAll("[^0-9+]", "");

        // Sénégal - numéro court (77, 78, 76, 70, etc.)
        if (num.matches("^7[0678]\\d{7}$")) {
            return "+221" + num;
        }

        // Avec indicatif sans +
        if (num.startsWith("221") && num.length() == 12) {
            return "+" + num;
        }

        // Déjà au format international
        if (num.startsWith("+221")) {
            return num;
        }

        // Si déjà au format international autre pays
        if (num.startsWith("+")) {
            return num;
        }

        // Par défaut, ajouter +221 (Sénégal)
        return "+221" + num;
    }

    /**
     * Masquer le numéro pour les logs (RGPD)
     */
    private String masquerNumero(String numero) {
        if (numero == null || numero.length() < 6) return "****";
        return numero.substring(0, 4) + "****" + numero.substring(numero.length() - 2);
    }

    /**
     * Tronquer un message
     */
    private String tronquer(String message, int maxLength) {
        if (message == null) return "";
        if (message.length() <= maxLength) return message;
        return message.substring(0, maxLength - 3) + "...";
    }

    /**
     * Vérifier si SMS est réellement actif
     */
    public boolean estActif() {
        return enabled && smsConfigured && apiKey != null && !apiKey.isEmpty();
    }

    /**
     * Envoyer un code OTP par SMS
     */
    public String envoyerOtp(String telephone, String code) {
        String message = "SunuFarmasi - Votre code de verification: " + code + ". Valide 10 min.";
        return envoyer(telephone, message);
    }

    /**
     * Envoyer une notification de garde
     */
    public String envoyerNotificationGarde(String telephone, String pharmacie, String date) {
        String message = "SunuFarmasi - Rappel: " + pharmacie + " est de garde le " + date;
        return envoyer(telephone, message);
    }
}