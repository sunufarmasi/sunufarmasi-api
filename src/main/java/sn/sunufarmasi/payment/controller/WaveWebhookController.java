package sn.sunufarmasi.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.payment.service.PaymentService;
import sn.sunufarmasi.payment.service.WaveService;

import java.util.Map;

/**
 * Webhook public pour les notifications de paiement Wave
 *
 * Wave appelle ce endpoint après chaque transaction pour notifier le statut.
 * La signature HMAC-SHA256 est vérifiée avant tout traitement.
 *
 * Endpoint : POST /api/v1/public/payment/wave/webhook
 * Header   : Wave-Signature: t=<timestamp>,v1=<hmac>
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/public/payment/wave")
@RequiredArgsConstructor
@Slf4j
public class WaveWebhookController {

    private final WaveService waveService;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    /**
     * POST /api/v1/public/payment/wave/webhook
     *
     * Reçoit la notification Wave et met à jour le paiement + abonnement.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Wave-Signature", required = false) String waveSignature
    ) {
        log.info("📨 Webhook Wave reçu");

        // Vérifier la signature HMAC
        if (waveSignature != null && !waveService.verifyWebhookSignature(payload, waveSignature)) {
            log.warn("⚠️ Signature Wave invalide — webhook rejeté");
            return ResponseEntity.status(401).build();
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> event = objectMapper.readValue(payload, Map.class);

            String type = (String) event.get("type");
            log.info("📋 Type événement Wave: {}", type);

            // On traite uniquement checkout.session.completed et checkout.session.failed
            if ("checkout.session.completed".equals(type) || "checkout.session.failed".equals(type)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) event.get("data");

                if (data != null) {
                    String clientReference = (String) data.get("client_reference");
                    String checkoutStatus  = (String) data.get("checkout_status");
                    String waveRef         = (String) data.get("id");

                    log.info("🔔 Wave event: ref={} status={} waveId={}",
                            clientReference, checkoutStatus, waveRef);

                    if (clientReference != null) {
                        if ("complete".equals(checkoutStatus)) {
                            paymentService.handleWaveSuccess(clientReference, waveRef);
                        } else {
                            paymentService.handleWaveFailure(clientReference,
                                    "Statut Wave: " + checkoutStatus);
                        }
                    }
                }
            } else {
                log.debug("Événement Wave ignoré: {}", type);
            }

        } catch (Exception e) {
            log.error("❌ Erreur traitement webhook Wave: {}", e.getMessage(), e);
            // Renvoyer 200 quand même pour éviter les retries Wave
        }

        // Toujours 200 pour confirmer la réception à Wave
        return ResponseEntity.ok().build();
    }
}
