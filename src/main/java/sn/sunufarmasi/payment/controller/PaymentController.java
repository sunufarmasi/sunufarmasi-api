package sn.sunufarmasi.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.patient.entity.Patient;
import sn.sunufarmasi.patient.repository.PatientRepository;
import sn.sunufarmasi.payment.dto.request.InitiatePaymentRequest;
import sn.sunufarmasi.payment.dto.response.PaymentResponse;
import sn.sunufarmasi.payment.service.PaymentService;
import sn.sunufarmasi.shared.constant.ErrorMessages;
import sn.sunufarmasi.shared.constant.SuccessMessages;
import sn.sunufarmasi.shared.dto.ApiResponse;
import sn.sunufarmasi.shared.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST pour les paiements
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Paiements", description = "API de gestion des paiements")
@SecurityRequirement(name = "bearer-jwt")
public class PaymentController {

    private final PaymentService paymentService;
    private final PatientRepository patientRepository;

    // ═══════════════════════════════════════════════════════════
    // INITIER PAIEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * POST /api/v1/payments/initiate
     * Initier un paiement pour un abonnement
     */
    @PostMapping("/initiate")
    @Operation(summary = "Initier un paiement", description = "Initier un paiement Orange Money/Wave pour un abonnement")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request,
            Authentication authentication
    ) {
        log.info("POST /api/v1/payments/initiate - User: {}", authentication.getName());

        UUID patientId = UUID.fromString(authentication.getName());
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        PaymentResponse payment = paymentService.initiatePayment(patient, request);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessMessages.PAYMENT_INITIATED, payment)
        );
    }

    /**
     * GET /api/v1/payments/{referenceInterne}
     * Vérifier le statut d'un paiement
     */
    @GetMapping("/{referenceInterne}")
    @Operation(summary = "Vérifier statut paiement", description = "Vérifier le statut d'un paiement par sa référence")
    public ResponseEntity<ApiResponse<PaymentResponse>> checkPaymentStatus(
            @PathVariable String referenceInterne,
            Authentication authentication
    ) {
        log.info("GET /api/v1/payments/{} - User: {}", referenceInterne, authentication.getName());

        PaymentResponse payment = paymentService.checkPaymentStatus(referenceInterne);

        return ResponseEntity.ok(
                ApiResponse.success(payment)
        );
    }

    /**
     * GET /api/v1/payments/history
     * Obtenir mon historique de paiements
     */
    @GetMapping("/history")
    @Operation(summary = "Historique paiements", description = "Obtenir l'historique de tous mes paiements")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentHistory(
            Authentication authentication
    ) {
        log.info("GET /api/v1/payments/history - User: {}", authentication.getName());

        UUID patientId = UUID.fromString(authentication.getName());
        List<PaymentResponse> payments = paymentService.getPatientPayments(patientId);

        return ResponseEntity.ok(
                ApiResponse.success(payments)
        );
    }
}