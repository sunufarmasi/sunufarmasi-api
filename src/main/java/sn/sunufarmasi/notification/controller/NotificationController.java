package sn.sunufarmasi.notification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.notification.dto.*;
import sn.sunufarmasi.notification.enums.*;
import sn.sunufarmasi.notification.service.NotificationService;
import sn.sunufarmasi.shared.dto.ApiResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    // ═══════════════════════════════════════════════════════════
    // NOTIFICATIONS UTILISATEUR
    // ═══════════════════════════════════════════════════════════

    /**
     * Mes notifications
     * GET /api/v1/notifications
     */
    @GetMapping("/notifications")
    public ResponseEntity<Page<NotificationResponse>> getMesNotifications(
            @RequestHeader("X-User-Id") UUID userId,
            Pageable pageable) {
        return ResponseEntity.ok(notificationService.getByUser(userId, pageable));
    }

    /**
     * Mes notifications non lues
     * GET /api/v1/notifications/non-lues
     */
    @GetMapping("/notifications/non-lues")
    public ResponseEntity<List<NotificationResponse>> getMesNotificationsNonLues(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(notificationService.getNonLues(userId));
    }

    /**
     * Nombre de non lues
     * GET /api/v1/notifications/count
     */
    @GetMapping("/notifications/count")
    public ResponseEntity<Long> getCountNonLues(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(notificationService.countNonLues(userId));
    }

    /**
     * Marquer comme lue
     * PUT /api/v1/notifications/{id}/lue
     */
    @PutMapping("/notifications/{id}/lue")
    public ResponseEntity<ApiResponse<Void>> marquerCommeLue(@PathVariable UUID id) {
        notificationService.marquerCommeLue(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marquée comme lue", null));
    }

    /**
     * Marquer toutes comme lues
     * PUT /api/v1/notifications/lues
     */
    @PutMapping("/notifications/lues")
    public ResponseEntity<ApiResponse<Void>> marquerToutesCommeLues(
            @RequestHeader("X-User-Id") UUID userId) {
        notificationService.marquerToutesCommeLues(userId);
        return ResponseEntity.ok(ApiResponse.success("Toutes les notifications marquées comme lues", null));
    }

    // ═══════════════════════════════════════════════════════════
    // CRÉATION (ADMIN/SYSTÈME)
    // ═══════════════════════════════════════════════════════════

    /**
     * Créer une notification
     * POST /api/v1/pharmacies/{pharmacieId}/notifications
     */
    @PostMapping("/pharmacies/{pharmacieId}/notifications")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<ApiResponse<NotificationResponse>> creer(
            @PathVariable UUID pharmacieId,
            @Valid @RequestBody CreateNotificationRequest request) {
        log.info("API - Création notification: {}", request.type());
        NotificationResponse response = notificationService.creer(pharmacieId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification créée", response));
    }

    // ═══════════════════════════════════════════════════════════
    // CONFIGURATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Ma configuration
     * GET /api/v1/notifications/config
     */
    @GetMapping("/notifications/config")
    public ResponseEntity<ConfigNotificationDTO> getMaConfig(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(notificationService.getConfig(userId));
    }

    /**
     * Mettre à jour ma configuration
     * PUT /api/v1/notifications/config
     */
    @PutMapping("/notifications/config")
    public ResponseEntity<ConfigNotificationDTO> updateMaConfig(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody ConfigNotificationDTO config) {
        return ResponseEntity.ok(notificationService.updateConfig(userId, config));
    }

    // ═══════════════════════════════════════════════════════════
    // ENUMS
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/notifications/enums/types")
    public ResponseEntity<TypeNotification[]> getTypes() {
        return ResponseEntity.ok(TypeNotification.values());
    }

    @GetMapping("/notifications/enums/canaux")
    public ResponseEntity<CanalNotification[]> getCanaux() {
        return ResponseEntity.ok(CanalNotification.values());
    }

    @GetMapping("/notifications/enums/statuts")
    public ResponseEntity<StatutNotification[]> getStatuts() {
        return ResponseEntity.ok(StatutNotification.values());
    }
}
