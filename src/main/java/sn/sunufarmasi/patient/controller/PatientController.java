package sn.sunufarmasi.patient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import sn.sunufarmasi.patient.dto.request.UpdatePatientRequest;
import sn.sunufarmasi.patient.dto.response.PatientResponse;
import sn.sunufarmasi.patient.service.PatientService;
import sn.sunufarmasi.shared.constant.SuccessMessages;
import sn.sunufarmasi.shared.dto.ApiResponse;

/**
 * Controller REST pour les patients (utilisateurs authentifiés)
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Patients", description = "API de gestion du profil patient")
@SecurityRequirement(name = "bearer-jwt")
public class PatientController {

    private final PatientService patientService;

    // ═══════════════════════════════════════════════════════════
    // PROFIL
    // ═══════════════════════════════════════════════════════════

    /**
     * GET /api/v1/patients/me
     * Obtenir le profil du patient connecté
     */
    @GetMapping("/me")
    @Operation(summary = "Obtenir mon profil", description = "Récupérer les informations du patient connecté")
    public ResponseEntity<ApiResponse<PatientResponse>> getMyProfile(Authentication authentication) {
        log.info("GET /api/v1/patients/me - User: {}", authentication.getName());

        String patientId = authentication.getName(); // Le JWT contient l'ID
        PatientResponse patient = patientService.getPatientById(patientId);

        return ResponseEntity.ok(
                ApiResponse.success(patient)
        );
    }

    /**
     * PUT /api/v1/patients/me
     * Mettre à jour le profil du patient connecté
     */
    @PutMapping("/me")
    @Operation(summary = "Mettre à jour mon profil")
    public ResponseEntity<ApiResponse<PatientResponse>> updateMyProfile(
            @Valid @RequestBody UpdatePatientRequest request,
            Authentication authentication
    ) {
        log.info("PUT /api/v1/patients/me - User: {}", authentication.getName());

        String patientId = authentication.getName();
        PatientResponse patient = patientService.updatePatient(patientId, request);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessMessages.PROFILE_UPDATED, patient)
        );
    }

    /**
     * DELETE /api/v1/patients/me
     * Désactiver mon compte
     */
    // ═══════════════════════════════════════════════════════════
    // ADMIN - Gestion globale patients
    // ═══════════════════════════════════════════════════════════

    /**
     * GET /api/v1/patients — Liste tous les patients (ADMIN)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Liste tous les patients (Admin)")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(patientService.getAll()));
    }

    /**
     * GET /api/v1/patients/{id} — Détail d'un patient (ADMIN)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Détail d'un patient (Admin)")
    public ResponseEntity<ApiResponse<PatientResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getPatientById(id.toString())));
    }

    /**
     * PUT /api/v1/patients/{id}/activer-premium — Activer le premium (ADMIN)
     */
    @PutMapping("/{id}/activer-premium")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activer l'abonnement premium d'un patient")
    public ResponseEntity<ApiResponse<PatientResponse>> activerPremium(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "1") int mois,
            @RequestParam(required = false) String reference
    ) {
        PatientResponse response = patientService.activerPremium(id, mois, reference);
        return ResponseEntity.ok(ApiResponse.success("Premium activé pour " + mois + " mois", response));
    }

    /**
     * PUT /api/v1/patients/{id}/suspendre-premium — Suspendre le premium (ADMIN)
     */
    @PutMapping("/{id}/suspendre-premium")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Suspendre l'abonnement premium d'un patient")
    public ResponseEntity<ApiResponse<PatientResponse>> suspendrePremium(@PathVariable UUID id) {
        PatientResponse response = patientService.suspendrePremium(id);
        return ResponseEntity.ok(ApiResponse.success("Premium suspendu", response));
    }

    /**
     * PUT /api/v1/patients/{id}/suspendre — Suspendre le compte (ADMIN)
     */
    @PutMapping("/{id}/suspendre")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Suspendre le compte d'un patient")
    public ResponseEntity<ApiResponse<Void>> suspendreCompte(@PathVariable UUID id) {
        patientService.suspendreCompte(id);
        return ResponseEntity.ok(ApiResponse.success("Compte suspendu", null));
    }

    /**
     * PUT /api/v1/patients/{id}/reactiver — Réactiver le compte (ADMIN)
     */
    @PutMapping("/{id}/reactiver")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Réactiver le compte d'un patient")
    public ResponseEntity<ApiResponse<Void>> reactiverCompte(@PathVariable UUID id) {
        patientService.reactiverCompte(id);
        return ResponseEntity.ok(ApiResponse.success("Compte réactivé", null));
    }

    /**
     * DELETE /api/v1/patients/{id} — Supprimer définitivement un patient (ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer définitivement un patient")
    public ResponseEntity<ApiResponse<Void>> supprimerPatient(@PathVariable UUID id) {
        patientService.supprimerPatient(id);
        return ResponseEntity.ok(ApiResponse.success("Patient supprimé", null));
    }
}