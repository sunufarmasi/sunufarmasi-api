package sn.sunufarmasi.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.auth.dto.request.LoginRequest;
import sn.sunufarmasi.auth.dto.request.RefreshTokenRequest;
import sn.sunufarmasi.auth.dto.request.VerifyOtpRequest;
import sn.sunufarmasi.auth.dto.response.LoginResponse;
import sn.sunufarmasi.auth.service.AuthService;
import sn.sunufarmasi.patient.dto.request.RegisterPatientRequest;
import sn.sunufarmasi.shared.constant.SuccessMessages;
import sn.sunufarmasi.shared.dto.ApiResponse;

/**
 * Controller REST pour l'authentification
 * Routes publiques (pas d'authentification requise)
 *
 * NOUVEAU WORKFLOW :
 * - Inscription : Téléphone + Nom + Commune → OTP envoyé
 * - Vérif OTP : Code → Compte activé + JWT + Essai gratuit 15j
 * - Login : Téléphone → OTP → Code → JWT
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "API d'authentification (Login, Register, OTP)")
public class AuthController {

    private final AuthService authService;

    // ═══════════════════════════════════════════════════════════
    // INSCRIPTION
    // ═══════════════════════════════════════════════════════════

    /**
     * POST /api/v1/auth/register
     * Inscription d'un nouveau patient
     */
    @PostMapping("/register")
    @Operation(
            summary = "Inscription patient",
            description = "Créer un compte patient et envoyer un code OTP de vérification. " +
                    "L'essai gratuit de 15 jours est activé automatiquement."
    )
    public ResponseEntity<ApiResponse<String>> registerPatient(
            @Valid @RequestBody RegisterPatientRequest request
    ) {
        log.info("POST /api/v1/auth/register - telephone: {}", request.telephone());

        String codeOtp = authService.registerPatient(request);

        // En développement, on retourne le code OTP
        // En production, on retourne juste un message de succès
        String message = String.format(
                "%s Code OTP (DEV): %s",
                SuccessMessages.AUTH_REGISTRATION_SUCCESS,
                codeOtp
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(message));
    }

    /**
     * POST /api/v1/auth/verify-otp
     * Vérifier le code OTP après inscription
     */
    @PostMapping("/verify-otp")
    @Operation(
            summary = "Vérifier le code OTP",
            description = "Vérifier le code OTP reçu par SMS, activer le compte et recevoir les tokens JWT"
    )
    public ResponseEntity<ApiResponse<LoginResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request
    ) {
        log.info("POST /api/v1/auth/verify-otp - telephone: {}", request.telephone());

        LoginResponse response = authService.verifyOtp(request);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessMessages.AUTH_ACCOUNT_VERIFIED, response)
        );
    }

    /**
     * POST /api/v1/auth/resend-otp
     * Renvoyer le code OTP
     */
    @PostMapping("/resend-otp")
    @Operation(
            summary = "Renvoyer le code OTP",
            description = "Générer et envoyer un nouveau code OTP"
    )
    public ResponseEntity<ApiResponse<String>> resendOtp(
            @RequestParam String telephone
    ) {
        log.info("POST /api/v1/auth/resend-otp - telephone: {}", telephone);

        String codeOtp = authService.resendOtp(telephone);

        // En développement, on retourne le code OTP
        String message = String.format(
                "%s Code OTP (DEV): %s",
                SuccessMessages.OTP_RESENT,
                codeOtp
        );

        return ResponseEntity.ok(
                ApiResponse.success(message)
        );
    }

    // ═══════════════════════════════════════════════════════════
    // CONNEXION
    // ═══════════════════════════════════════════════════════════

    /**
     * POST /api/v1/auth/login
     * Demander un OTP pour se connecter
     */
    @PostMapping("/login")
    @Operation(
            summary = "Demander OTP login",
            description = "Envoyer un code OTP au téléphone pour se connecter"
    )
    public ResponseEntity<ApiResponse<String>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("POST /api/v1/auth/login - telephone: {}", request.telephone());

        String codeOtp = authService.requestLoginOtp(request);

        // En développement, on retourne le code OTP
        String message = String.format(
                "%s Code OTP (DEV): %s",
                SuccessMessages.OTP_SENT,
                codeOtp
        );

        return ResponseEntity.ok(
                ApiResponse.success(message)
        );
    }

    /**
     * POST /api/v1/auth/login/verify
     * Se connecter avec le code OTP
     */
    @PostMapping("/login/verify")
    @Operation(
            summary = "Connexion avec OTP",
            description = "Se connecter avec le code OTP reçu et obtenir les tokens JWT"
    )
    public ResponseEntity<ApiResponse<LoginResponse>> loginWithOtp(
            @Valid @RequestBody VerifyOtpRequest request
    ) {
        log.info("POST /api/v1/auth/login/verify - telephone: {}", request.telephone());

        LoginResponse response = authService.loginWithOtp(request);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessMessages.AUTH_ACCOUNT_VERIFIED, response)
        );
    }

    /**
     * POST /api/v1/auth/refresh-token
     * Rafraîchir le token JWT
     */
    @PostMapping("/refresh-token")
    @Operation(
            summary = "Rafraîchir le token",
            description = "Obtenir un nouveau access token avec le refresh token"
    )
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        log.info("POST /api/v1/auth/refresh-token");

        LoginResponse response = authService.refreshToken(request.refreshToken());


        return ResponseEntity.ok(
                ApiResponse.success(SuccessMessages.AUTH_ACCOUNT_VERIFIED, response)
        );
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * GET /api/v1/auth/health
     * Health check pour l'API d'authentification
     */
    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Vérifier que l'API d'authentification fonctionne"
    )
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Auth API is running")
        );
    }
}