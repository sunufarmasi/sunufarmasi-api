package sn.sunufarmasi.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

/**
 * Service d'intégration Wave Business Checkout
 *
 * Doc API : https://docs.wave.com/business/checkout
 *
 * Flow :
 * 1. POST /v1/checkout/sessions → reçoit wave_launch_url
 * 2. Flutter ouvre wave_launch_url dans le navigateur
 * 3. Utilisateur paie dans Wave
 * 4. Wave appelle notre webhook avec le statut
 * 5. On active l'abonnement
 *
 * @author WeCan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WaveService {

    @Value("${wave.api-key:}")
    private String apiKey;

    @Value("${wave.webhook-secret:}")
    private String webhookSecret;

    @Value("${wave.api-url:https://api.wave.com/v1}")
    private String apiUrl;

    @Value("${wave.success-url:sunufarmasi://payment/success}")
    private String successUrl;

    @Value("${wave.error-url:sunufarmasi://payment/error}")
    private String errorUrl;

    /**
     * Créer une session de checkout Wave
     *
     * @param montant          Montant en FCFA (XOF)
     * @param referenceInterne Notre référence interne (client_reference)
     * @return WaveCheckoutSession avec id + wave_launch_url
     */
    public WaveCheckoutSession createCheckoutSession(int montant, String referenceInterne) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("⚠️ WAVE_API_KEY non configurée — mode simulation");
            return simulateCheckoutSession(montant, referenceInterne);
        }

        try {
            WebClient client = WebClient.builder()
                    .baseUrl(apiUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            Map<String, Object> body = Map.of(
                    "currency", "XOF",
                    "amount", String.valueOf(montant),
                    "success_url", successUrl + "?ref=" + referenceInterne,
                    "error_url", errorUrl + "?ref=" + referenceInterne,
                    "client_reference", referenceInterne
            );

            Map<?, ?> response = client.post()
                    .uri("/checkout/sessions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Réponse vide de Wave API");
            }

            String id = (String) response.get("id");
            String checkoutUrl = (String) response.get("wave_launch_url");

            log.info("✅ Session Wave créée: id={} ref={}", id, referenceInterne);

            return new WaveCheckoutSession(id, checkoutUrl, (String) response.get("checkout_status"));

        } catch (WebClientResponseException e) {
            log.error("❌ Erreur Wave API {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erreur Wave API : " + e.getMessage());
        }
    }

    /**
     * Vérifier la signature HMAC-SHA256 d'un webhook Wave
     * Header: Wave-Signature: t=timestamp,v1=signature
     */
    public boolean verifyWebhookSignature(String payload, String waveSignatureHeader) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("⚠️ WAVE_WEBHOOK_SECRET non configuré — signature ignorée en dev");
            return true;
        }

        try {
            // Extraire timestamp et signature
            String[] parts = waveSignatureHeader.split(",");
            String timestamp = null;
            String signature = null;
            for (String part : parts) {
                if (part.startsWith("t=")) timestamp = part.substring(2);
                if (part.startsWith("v1=")) signature = part.substring(3);
            }

            if (timestamp == null || signature == null) return false;

            // Construire le message signé
            String signedPayload = timestamp + "." + payload;

            // Calculer HMAC-SHA256
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"
            ));
            byte[] digest = mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
            String computed = HexFormat.of().formatHex(digest);

            return computed.equals(signature);

        } catch (Exception e) {
            log.error("❌ Erreur vérification signature Wave: {}", e.getMessage());
            return false;
        }
    }

    /** Mode simulation si WAVE_API_KEY absent (dev local) */
    private WaveCheckoutSession simulateCheckoutSession(int montant, String referenceInterne) {
        String fakeId = "cos_dev_" + System.currentTimeMillis();
        // En dev, on simule une URL Wave (ne fonctionne pas vraiment)
        String fakeUrl = "https://pay.wave.com/c/demo?ref=" + referenceInterne + "&amount=" + montant;
        log.info("🔧 [DEV] Session Wave simulée: id={}", fakeId);
        return new WaveCheckoutSession(fakeId, fakeUrl, "pending");
    }

    // ── DTO interne ────────────────────────────────────────────
    public record WaveCheckoutSession(
            String id,
            String waveCheckoutUrl,
            String status
    ) {}
}
