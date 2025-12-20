package sn.sunufarmasi.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sn.sunufarmasi.auth.service.SmsService;

/**
 * Implémentation du service SMS (MODE DEV)
 *
 * EN PRODUCTION, remplacer par:
 * - Twilio
 * - Orange SMS API (Sénégal)
 * - AWS SNS
 * - Autre provider SMS
 *
 * @author WeCan
 * @since 1.0.0
 */
@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Override
    public void sendSms(String telephone, String message) {
        // MODE DEV: Logger le SMS au lieu de l'envoyer
        log.info("📱 ═══════════════════════════════════════════");
        log.info("📱 SMS à envoyer:");
        log.info("📱 Destinataire: {}", telephone);
        log.info("📱 Message: {}", message);
        log.info("📱 ═══════════════════════════════════════════");

        // TODO PRODUCTION: Intégrer un vrai service SMS
        /*
        Exemple avec Twilio:

        Twilio.init(accountSid, authToken);
        Message.creator(
            new PhoneNumber(telephone),
            new PhoneNumber(twilioNumber),
            message
        ).create();
        */

        /*
        Exemple avec Orange SMS API (Sénégal):

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.orange.com/smsmessaging/v1/outbound/..."))
            .header("Authorization", "Bearer " + accessToken)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        */
    }

    @Override
    public boolean isAvailable() {
        // MODE DEV: Toujours disponible (logging)
        return true;

        // TODO PRODUCTION: Vérifier la connexion au service SMS
        /*
        try {
            // Ping le service SMS
            return true;
        } catch (Exception e) {
            return false;
        }
        */
    }
}