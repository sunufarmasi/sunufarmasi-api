package sn.sunufarmasi.subscription.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.subscription.dto.response.SubscriptionPlanResponse;
import sn.sunufarmasi.subscription.dto.response.SubscriptionResponse;
import sn.sunufarmasi.subscription.service.SubscriptionService;
import sn.sunufarmasi.shared.dto.ApiResponse;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST pour les abonnements
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Abonnements", description = "API de gestion des abonnements")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // ═══════════════════════════════════════════════════════════
    // PLANS (PUBLIC)
    // ═══════════════════════════════════════════════════════════

    /**
     * GET /api/v1/subscriptions/plans
     * Obtenir tous les plans disponibles (PUBLIC)
     */
    @GetMapping("/plans")
    @Operation(summary = "Obtenir les plans d'abonnement", description = "Liste tous les plans disponibles")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getAllPlans() {
        log.info("GET /api/v1/subscriptions/plans");

        List<SubscriptionPlanResponse> plans = subscriptionService.getAllPlans();

        return ResponseEntity.ok(
                ApiResponse.success(plans)
        );
    }

    // ═══════════════════════════════════════════════════════════
    // MON ABONNEMENT (AUTHENTICATED)
    // ═══════════════════════════════════════════════════════════

    /**
     * GET /api/v1/subscriptions/me
     * Obtenir mon abonnement actif
     */
    @GetMapping("/me")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Obtenir mon abonnement", description = "Récupérer l'abonnement actif du patient connecté")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getMySubscription(Authentication authentication) {
        log.info("GET /api/v1/subscriptions/me - User: {}", authentication.getName());

        UUID patientId = UUID.fromString(authentication.getName());
        SubscriptionResponse subscription = subscriptionService.getPatientActiveSubscription(patientId);

//        if (subscription == null) {
//            return ResponseEntity.ok(
//                    ApiResponse.success(null, "Aucun abonnement actif")
//            );
//        }

        return ResponseEntity.ok(
                ApiResponse.success(subscription)
        );
    }

    /**
     * GET /api/v1/subscriptions/me/status
     * Vérifier si j'ai un abonnement actif
     */
    @GetMapping("/me/status")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Vérifier statut abonnement", description = "Vérifier si le patient a un abonnement actif")
    public ResponseEntity<ApiResponse<Boolean>> checkSubscriptionStatus(Authentication authentication) {
        log.info("GET /api/v1/subscriptions/me/status - User: {}", authentication.getName());

        UUID patientId = UUID.fromString(authentication.getName());
        boolean hasActive = subscriptionService.hasActiveSubscription(patientId);

        return ResponseEntity.ok(
                ApiResponse.success(hasActive)
        );
    }
}