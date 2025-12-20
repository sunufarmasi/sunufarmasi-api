package sn.sunufarmasi.patient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
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
//    @DeleteMapping("/me")
//    @Operation(summary = "Désactiver mon compte")
//    public ResponseEntity<ApiResponse<Void>> deactivateMyAccount(Authentication authentication) {
//        log.info("DELETE /api/v1/patients/me - User: {}", authentication.getName());
//
//        String patientId = authentication.getName();
//        patientService.deactivatePatient(patientId);
//
//        return ResponseEntity.ok(
//                ApiResponse.success(SuccessMessages.ACCOUNT_DEACTIVATED)
//        );
//    }
}