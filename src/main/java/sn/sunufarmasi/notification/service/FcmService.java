package sn.sunufarmasi.notification.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service FCM pour les notifications push mobiles
 * Utilise Firebase Cloud Messaging pour notifier les patients
 * Fonctionne en mode dégradé si Firebase n'est pas configuré
 */
@Service
@Slf4j
public class FcmService {

    private boolean isFirebaseAvailable() {
        try {
            return !FirebaseApp.getApps().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Envoyer une notification à un token FCM spécifique
     */
    @Async
    public void envoyerNotification(String fcmToken, String titre, String corps, Map<String, String> data) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.warn("FCM token vide - notification ignorée");
            return;
        }

        if (!isFirebaseAvailable()) {
            log.info("[FCM-SIMULATION] Notif à {}: {} - {}", fcmToken.substring(0, Math.min(10, fcmToken.length())), titre, corps);
            return;
        }

        try {
            Message.Builder builder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(titre)
                            .setBody(corps)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build());

            if (data != null && !data.isEmpty()) {
                builder.putAllData(data);
            }

            String response = FirebaseMessaging.getInstance().send(builder.build());
            log.info("Notification FCM envoyée: {}", response);

        } catch (FirebaseMessagingException e) {
            log.error("Erreur envoi notification FCM: {}", e.getMessage());
        }
    }

    /**
     * Envoyer une notification à un topic (tous les abonnés)
     */
    @Async
    public void envoyerNotificationTopic(String topic, String titre, String corps, Map<String, String> data) {
        if (!isFirebaseAvailable()) {
            log.info("[FCM-SIMULATION] Notif topic '{}': {} - {}", topic, titre, corps);
            return;
        }
        try {
            Message.Builder builder = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(titre)
                            .setBody(corps)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build());

            if (data != null && !data.isEmpty()) {
                builder.putAllData(data);
            }

            String response = FirebaseMessaging.getInstance().send(builder.build());
            log.info("Notification FCM topic '{}' envoyée: {}", topic, response);

        } catch (FirebaseMessagingException e) {
            log.error("Erreur envoi notification FCM topic {}: {}", topic, e.getMessage());
        }
    }

    /**
     * Envoyer à une liste de tokens (multicast)
     */
    @Async
    public void envoyerMulticast(List<String> tokens, String titre, String corps, Map<String, String> data) {
        if (tokens == null || tokens.isEmpty()) return;

        if (!isFirebaseAvailable()) {
            log.info("[FCM-SIMULATION] Multicast à {} tokens: {} - {}", tokens.size(), titre, corps);
            return;
        }

        try {
            MulticastMessage.Builder builder = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder()
                            .setTitle(titre)
                            .setBody(corps)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build());

            if (data != null && !data.isEmpty()) {
                builder.putAllData(data);
            }

            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(builder.build());
            log.info("Notification FCM multicast: {}/{} réussies",
                    response.getSuccessCount(), tokens.size());

        } catch (FirebaseMessagingException e) {
            log.error("Erreur envoi notification FCM multicast: {}", e.getMessage());
        }
    }

    /**
     * Notifier tous les patients qu'un planning de gardes a été publié
     * Topic: "gardes-{regionCode}" ou "gardes-all"
     */
    @Async
    public void notifierGardesPubliees(String regionCode, String nomSyndicat, String dateGarde) {
        String topic = "gardes-" + regionCode.toLowerCase().replaceAll("[^a-z0-9-]", "");
        String titre = "🏥 Gardes disponibles !";
        String corps = nomSyndicat + " a publié les pharmacies de garde pour " + dateGarde;

        Map<String, String> data = Map.of(
                "type", "GARDES_PUBLIEES",
                "region", regionCode,
                "syndicat", nomSyndicat
        );

        envoyerNotificationTopic(topic, titre, corps, data);
        // Aussi notifier le topic global
        envoyerNotificationTopic("gardes-all", titre, corps, data);

        log.info("Notification gardes publiées envoyée pour région: {}", regionCode);
    }
}
