package sn.sunufarmasi.patient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.shared.dto.ApiResponse;
import sn.sunufarmasi.notification.service.EmailService;
import sn.sunufarmasi.security.JwtTokenProvider;

import sn.sunufarmasi.patient.entity.Patient;
import sn.sunufarmasi.patient.repository.PatientRepository;
import sn.sunufarmasi.patient.dto.request.RegisterPatientRequest;
import sn.sunufarmasi.subscription.entity.Subscription;
import sn.sunufarmasi.subscription.entity.SubscriptionPlan;
import sn.sunufarmasi.subscription.entity.SubscriptionStatus;
import sn.sunufarmasi.subscription.repository.SubscriptionRepository;
import sn.sunufarmasi.subscription.repository.SubscriptionPlanRepository;
import sn.sunufarmasi.localisation.entity.Commune;
import sn.sunufarmasi.localisation.repository.CommuneRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Contrôleur d'authentification patients (app mobile)
 *
 * Flow inscription :
 * 1. POST /register → crée patient, envoie OTP par EMAIL
 * 2. POST /verify-otp → vérifie OTP (usage unique), active compte, lie l'appareil
 *
 * Flow connexion :
 * 1. POST /login → envoie OTP à l'email du compte
 * 2. POST /verify-login → vérifie OTP + deviceId
 *
 * Règles :
 * - Email unique par compte
 * - Téléphone unique par compte
 * - OTP à usage unique, valide 10 min
 * - 1 compte = 1 appareil (deviceId stocké, vérifié à chaque connexion)
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/public/auth/patient")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Authentification Patient", description = "Inscription et connexion patients (public)")
public class PatientAuthController {

    private final PatientRepository patientRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final CommuneRepository communeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    // Stockage temporaire OTP — keyed by email (TODO prod: Redis)
    private static final Map<String, OtpData> otpStore = new HashMap<>();

    // ═══════════════════════════════════════════════════════════════════════════════
    // INSCRIPTION
    // ═══════════════════════════════════════════════════════════════════════════════

    @PostMapping("/register")
    @Transactional
    @Operation(summary = "Inscription patient", description = "Crée un compte + envoie OTP par email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(
            @Valid @RequestBody RegisterPatientRequest request) {
        log.info("POST /register - email={}, tel={}", request.email(), request.telephone());

        // 1. Unicité email
        if (patientRepository.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Cet email est déjà utilisé. Veuillez vous connecter.")
            );
        }

        // 2. Unicité téléphone
        if (patientRepository.existsByTelephone(request.telephone())) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Ce numéro de téléphone est déjà inscrit. Veuillez vous connecter.")
            );
        }

        // 3. Commune (optionnelle)
        Commune commune = null;
        if (request.communeId() != null && !request.communeId().isBlank()) {
            try {
                commune = communeRepository.findById(UUID.fromString(request.communeId())).orElse(null);
            } catch (IllegalArgumentException e) {
                log.warn("communeId invalide: {}", request.communeId());
            }
        }

        // 4. Créer le patient avec deviceId
        Patient patient = Patient.builder()
                .nomComplet(request.nomComplet())
                .telephone(request.telephone())
                .email(request.email())
                .deviceId(request.deviceId())
                .commune(commune)
                .dateNaissance(request.dateNaissance())
                .sexe(request.sexe())
                .adresse(request.adresse())
                .telephoneVerified(false)
                .emailVerified(false)
                .actif(true)
                .build();

        patient = patientRepository.save(patient);
        log.info("✅ Patient créé: id={} email={}", patient.getId(), patient.getEmail());

        // 5. Générer OTP et envoyer par email (essai gratuit activé après vérification email dans /verify-otp)
        String otp = generateOtp(request.email());
        emailService.sendOtpEmail(request.email(), otp, request.nomComplet());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("patientId", patient.getId().toString());
        result.put("email", patient.getEmail());
        result.put("message", "Inscription réussie ! Un code de vérification a été envoyé à " + request.email());
        result.put("requiresOtp", true);
        result.put("otpExpiresIn", "10 minutes");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inscription réussie", result));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // INSCRIPTION BASIQUE (sans email)
    // ═══════════════════════════════════════════════════════════════════════════════

    @PostMapping("/register-basic")
    @Transactional
    @Operation(summary = "Inscription basique sans email", description = "Crée un profil avec nom + téléphone + sexe + localisation. Email optionnel — à compléter pour débloquer le premium.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerBasic(
            @Valid @RequestBody RegisterBasicRequest request) {
        log.info("POST /register-basic - tel={}", request.telephone());

        // 1. Unicité téléphone
        if (patientRepository.existsByTelephone(request.telephone())) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Ce numéro est déjà enregistré. Connectez-vous depuis les paramètres.")
            );
        }

        // 2. Commune (optionnelle)
        Commune commune = null;
        if (request.communeId() != null && !request.communeId().isBlank()) {
            try {
                commune = communeRepository.findById(UUID.fromString(request.communeId())).orElse(null);
            } catch (IllegalArgumentException e) {
                log.warn("communeId invalide: {}", request.communeId());
            }
        }

        // 3. Créer le patient sans email
        Patient patient = Patient.builder()
                .nomComplet(request.nomComplet())
                .telephone(request.telephone())
                .sexe(request.sexe())
                .deviceId(request.deviceId())
                .commune(commune)
                .telephoneVerified(false)
                .emailVerified(false)
                .actif(true)
                .build();

        patient = patientRepository.save(patient);
        log.info("✅ Profil basique créé: id={}", patient.getId());

        // 4. Générer token (pas de trial — activé après vérification email)
        String token = generateToken(patient);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("patientId", patient.getId().toString());
        result.put("nomComplet", patient.getNomComplet());
        result.put("telephone", patient.getTelephone());
        result.put("token", token);
        result.put("emailRequired", true);
        result.put("canSearchMedicaments", false);
        result.put("message", "Profil créé ! Complétez avec un email pour débloquer les fonctionnalités premium.");

        if (patient.getCommune() != null) {
            Map<String, Object> loc = new LinkedHashMap<>();
            loc.put("communeId", patient.getCommune().getId().toString());
            loc.put("communeNom", patient.getCommune().getNom());
            if (patient.getCommune().getDepartement() != null) {
                loc.put("departementNom", patient.getCommune().getDepartement().getNom());
                if (patient.getCommune().getDepartement().getRegion() != null) {
                    loc.put("regionNom", patient.getCommune().getDepartement().getRegion().getNom());
                }
            }
            result.put("localisation", loc);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Profil créé", result));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // COMPLÉTER LE PROFIL (ajouter email → envoyer OTP → activer trial)
    // ═══════════════════════════════════════════════════════════════════════════════

    @PostMapping("/complete-profile")
    @Transactional
    @Operation(summary = "Compléter le profil avec email", description = "Ajoute un email à un profil basique existant et envoie un OTP de vérification")
    public ResponseEntity<ApiResponse<Map<String, Object>>> completeProfile(
            @RequestHeader("Authorization") String bearerToken,
            @RequestParam @Email @NotBlank String email) {

        String token = jwtTokenProvider.extractTokenFromBearer(bearerToken);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).body(
                    ApiResponse.error("Session expirée. Veuillez recréer votre profil.")
            );
        }

        String patientId = jwtTokenProvider.getUsernameFromToken(token);
        Optional<Patient> patientOpt;
        try {
            patientOpt = patientRepository.findById(UUID.fromString(patientId));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponse.error("Identifiant invalide."));
        }

        if (patientOpt.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResponse.error("Profil introuvable."));
        }

        Patient patient = patientOpt.get();
        String normalizedEmail = email.trim().toLowerCase();

        // Vérifier si déjà complété
        if (patient.getEmail() != null && !patient.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Ce profil a déjà un email associé. Utilisez la connexion.")
            );
        }

        // Vérifier unicité email
        if (patientRepository.existsByEmail(normalizedEmail)) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Cet email est déjà utilisé par un autre compte.")
            );
        }

        // Mettre à jour l'email
        patient.setEmail(normalizedEmail);
        patientRepository.save(patient);

        // Envoyer OTP
        String otp = generateOtp(normalizedEmail);
        emailService.sendOtpEmail(normalizedEmail, otp, patient.getNomComplet());

        log.info("✅ Email ajouté au profil {}: {}", patientId, normalizedEmail);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("patientId", patient.getId().toString());
        result.put("email", normalizedEmail);
        result.put("maskedEmail", maskEmail(normalizedEmail));
        result.put("requiresOtp", true);
        result.put("message", "Un code de vérification a été envoyé à " + maskEmail(normalizedEmail));

        return ResponseEntity.ok(ApiResponse.success("Code envoyé", result));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // VÉRIFICATION OTP
    // ═══════════════════════════════════════════════════════════════════════════════

    @PostMapping("/verify-otp")
    @Transactional
    @Operation(summary = "Vérifier OTP", description = "Vérifie le code email, active le compte, lie l'appareil")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        log.info("POST /verify-otp - email={}", request.email());

        // 1. Vérifier OTP
        OtpData otpData = otpStore.get(request.email());

        if (otpData == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Aucun code en attente pour cet email. Veuillez demander un nouveau code.")
            );
        }
        if (otpData.used()) {
            otpStore.remove(request.email());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Ce code a déjà été utilisé. Veuillez demander un nouveau code.")
            );
        }
        if (otpData.isExpired()) {
            otpStore.remove(request.email());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Le code a expiré. Veuillez demander un nouveau code.")
            );
        }
        if (!otpData.otp().equals(request.otp())) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Code incorrect. Vérifiez votre email et réessayez.")
            );
        }

        // 2. Marquer OTP comme utilisé (usage unique)
        otpStore.put(request.email(), otpData.markUsed());

        // 3. Trouver le patient
        Optional<Patient> patientOpt = patientRepository.findByEmail(request.email());
        if (patientOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Compte introuvable. Veuillez vous réinscrire.")
            );
        }

        Patient patient = patientOpt.get();

        // 4. Vérifier / lier le deviceId
        if (patient.getDeviceId() != null && !patient.getDeviceId().isBlank()
                && !patient.getDeviceId().equals(request.deviceId())) {
            log.warn("⚠️ Tentative de connexion depuis un autre appareil pour patient {}", patient.getId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Ce compte est déjà actif sur un autre appareil. Contactez le support pour transférer votre compte.")
            );
        }

        // 5. Activer le compte
        patient.setEmailVerified(true);
        patient.setDeviceId(request.deviceId());
        patient.updateLastLogin();
        patientRepository.save(patient);

        // 6. Récupérer l'abonnement actif si présent (pas de trial auto — activé via /activate-trial)
        Optional<Subscription> subOpt = subscriptionRepository
                .findByPatientIdAndStatus(patient.getId(), SubscriptionStatus.ACTIVE);
        Subscription activeSub = subOpt.orElse(null);

        Map<String, Object> result = buildAuthResponse(patient, activeSub);
        result.put("isNewPremium", false);
        log.info("✅ Compte activé: {}", patient.getEmail());

        return ResponseEntity.ok(ApiResponse.success("Compte activé avec succès", result));
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Renvoyer OTP")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resendOtp(
            @RequestParam @Email String email) {
        log.info("POST /resend-otp - email={}", email);

        Optional<Patient> patientOpt = patientRepository.findByEmail(email);
        if (patientOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Aucun compte associé à cet email.")
            );
        }

        String otp = generateOtp(email);
        emailService.sendOtpEmail(email, otp, patientOpt.get().getNomComplet());

        return ResponseEntity.ok(ApiResponse.success("Code renvoyé",
                Map.of("message", "Un nouveau code a été envoyé à " + email, "expiresIn", "10 minutes")));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // CONNEXION
    // ═══════════════════════════════════════════════════════════════════════════════

    @PostMapping("/login")
    @Operation(summary = "Demander connexion", description = "Envoie OTP par email (premium) ou retourne OTP à l'écran (utilisateur gratuit sans email)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @RequestParam String telephone,
            @RequestParam String deviceId) {

        // Normaliser le numéro de téléphone
        String normalizedPhone = telephone.trim();
        if (normalizedPhone.startsWith("7") && normalizedPhone.length() == 9) {
            normalizedPhone = "+221" + normalizedPhone;
        } else if (normalizedPhone.startsWith("221") && normalizedPhone.length() == 12) {
            normalizedPhone = "+" + normalizedPhone;
        }
        log.info("POST /login - telephone={}", normalizedPhone);

        // Trouver le patient par téléphone
        Optional<Patient> patientOpt = patientRepository.findByTelephone(normalizedPhone);
        if (patientOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Aucun compte associé à ce numéro de téléphone.")
            );
        }

        Patient patient = patientOpt.get();

        if (!patient.isActif()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Votre compte a été désactivé. Contactez le support.")
            );
        }

        // Vérifier que c'est le bon appareil
        if (patient.getDeviceId() != null && !patient.getDeviceId().isBlank()
                && !patient.getDeviceId().equals(deviceId)) {
            log.warn("Tentative login depuis un autre appareil pour patient {}", patient.getId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Contactez le support si vous avez changé d'appareil.")
            );
        }

        Map<String, Object> loginResp = new LinkedHashMap<>();
        loginResp.put("requiresOtp", true);
        loginResp.put("patientId", patient.getId().toString());

        if (patient.getEmail() != null && !patient.getEmail().isBlank()) {
            // Utilisateur premium/trial avec email → OTP envoyé par email
            String otp = generateOtp(patient.getEmail());
            emailService.sendOtpEmail(patient.getEmail(), otp, patient.getNomComplet());
            log.info("OTP envoyé par email pour patient {} (telephone={})", patient.getId(), normalizedPhone);

            loginResp.put("channel", "email");
            loginResp.put("maskedEmail", maskEmail(patient.getEmail()));
            loginResp.put("message", "Un code de connexion a été envoyé à " + maskEmail(patient.getEmail()));
        } else {
            // Utilisateur gratuit sans email → OTP visible dans la réponse
            String otpKey = "phone:" + normalizedPhone;
            String otp = generateOtp(otpKey);
            log.info("OTP généré à l'écran pour patient {} (telephone={})", patient.getId(), normalizedPhone);

            loginResp.put("channel", "screen");
            loginResp.put("otpCode", otp);
            loginResp.put("message", "Votre code de connexion est affiché ci-dessous.");
        }

        return ResponseEntity.ok(ApiResponse.success("Code généré", loginResp));
    }

    @PostMapping("/verify-login")
    @Transactional
    @Operation(summary = "Vérifier connexion")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyLogin(
            @Valid @RequestBody VerifyLoginRequest request) {

        // Normaliser le numéro de téléphone
        String normalizedPhone = request.telephone().trim();
        if (normalizedPhone.startsWith("7") && normalizedPhone.length() == 9) {
            normalizedPhone = "+221" + normalizedPhone;
        } else if (normalizedPhone.startsWith("221") && normalizedPhone.length() == 12) {
            normalizedPhone = "+" + normalizedPhone;
        }
        log.info("POST /verify-login - telephone={}", normalizedPhone);

        // Trouver le patient par téléphone
        Optional<Patient> patientOpt = patientRepository.findByTelephone(normalizedPhone);
        if (patientOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Aucun compte associé à ce numéro de téléphone.")
            );
        }

        Patient patient = patientOpt.get();

        // Déterminer la clé OTP selon que le patient a un email ou non
        String otpKey = (patient.getEmail() != null && !patient.getEmail().isBlank())
                ? patient.getEmail()
                : "phone:" + normalizedPhone;

        // Valider l'OTP
        OtpData otpData = otpStore.get(otpKey);

        if (otpData == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Aucun code en attente. Veuillez demander un nouveau code.")
            );
        }
        if (otpData.used()) {
            otpStore.remove(otpKey);
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Ce code a déjà été utilisé. Veuillez demander un nouveau code.")
            );
        }
        if (otpData.isExpired()) {
            otpStore.remove(otpKey);
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Le code a expiré. Veuillez demander un nouveau code.")
            );
        }
        if (!otpData.otp().equals(request.otp())) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Code incorrect. Vérifiez et réessayez.")
            );
        }

        // Marquer l'OTP comme utilisé (usage unique)
        otpStore.put(otpKey, otpData.markUsed());

        // Mettre à jour deviceId et lastLogin
        patient.setDeviceId(request.deviceId());
        patient.updateLastLogin();
        patientRepository.save(patient);

        // Trouver l'abonnement actif
        Optional<Subscription> subOpt = subscriptionRepository
                .findByPatientIdAndStatus(patient.getId(), SubscriptionStatus.ACTIVE);

        Map<String, Object> result = buildAuthResponse(patient, subOpt.orElse(null));
        log.info("Connexion réussie pour patient {} (telephone={})", patient.getId(), normalizedPhone);

        return ResponseEntity.ok(ApiResponse.success("Connexion réussie", result));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // VÉRIFICATION ABONNEMENT
    // ═══════════════════════════════════════════════════════════════════════════════

    @GetMapping("/check-subscription/{patientId}")
    @Transactional
    @Operation(summary = "Vérifier abonnement")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkSubscription(
            @PathVariable UUID patientId) {
        log.info("GET /check-subscription/{}", patientId);

        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (patientOpt.isEmpty()) return ResponseEntity.notFound().build();

        Patient patient = patientOpt.get();
        Optional<Subscription> activeSubOpt = subscriptionRepository
                .findByPatientIdAndStatus(patientId, SubscriptionStatus.ACTIVE);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("patientId", patientId.toString());
        result.put("nomComplet", patient.getNomComplet());

        if (activeSubOpt.isPresent()) {
            Subscription sub = activeSubOpt.get();
            if (sub.getExpiresAt().isBefore(LocalDateTime.now())) {
                sub.setStatus(SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(sub);
                result.put("hasActiveSubscription", false);
                result.put("status", "EXPIRED");
                result.put("canSearchMedicaments", false);
                result.put("requiresPayment", true);
                result.put("message", "Votre abonnement a expiré. Renouvelez pour accéder aux fonctionnalités premium.");
            } else {
                long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(
                        LocalDateTime.now().toLocalDate(), sub.getExpiresAt().toLocalDate());
                result.put("hasActiveSubscription", true);
                result.put("status", sub.getStatus().name());
                result.put("plan", sub.getPlan().getCode());
                result.put("planNom", sub.getPlan().getNom());
                result.put("expiresAt", sub.getExpiresAt().toString());
                result.put("daysRemaining", daysRemaining);
                result.put("canSearchMedicaments", true);
                result.put("isTrial", sub.isTrial());
                result.put("requiresPayment", false);
                if (daysRemaining <= 3) {
                    result.put("warning", "Votre abonnement expire dans " + daysRemaining + " jour(s)");
                }
            }
        } else if (patient.isPremiumActif() && patient.getDateFinPremium() != null
                && patient.getDateFinPremium().isAfter(LocalDate.now())) {
            // Fallback : premium activé directement sur le patient (sans Subscription liée)
            long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDate.now(), patient.getDateFinPremium());
            result.put("hasActiveSubscription", true);
            result.put("status", "ACTIVE");
            result.put("plan", "MONTHLY");
            result.put("planNom", "Premium Mensuel");
            result.put("expiresAt", patient.getDateFinPremium().atStartOfDay().toString());
            result.put("daysRemaining", daysRemaining);
            result.put("canSearchMedicaments", true);
            result.put("isTrial", false);
            result.put("requiresPayment", false);
        } else {
            result.put("hasActiveSubscription", false);
            result.put("status", "NO_SUBSCRIPTION");
            result.put("canSearchMedicaments", false);
            result.put("requiresPayment", true);
            result.put("message", "Abonnez-vous pour accéder aux fonctionnalités premium.");
        }

        // Plans disponibles
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findByActifTrueOrderByOrdreAsc();
        result.put("availablePlans", plans.stream()
                .filter(p -> !"FREE_TRIAL".equals(p.getCode()))
                .map(p -> Map.of(
                        "code", p.getCode(),
                        "nom", p.getNom(),
                        "description", p.getDescription() != null ? p.getDescription() : "",
                        "prix", p.getPrix(),
                        "prixFormate", p.getPrix() + " FCFA",
                        "dureeJours", p.getDureeJours()
                ))
                .toList());

        return ResponseEntity.ok(ApiResponse.success("État de l'abonnement", result));
    }

    @GetMapping("/check-phone/{telephone}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkPhone(@PathVariable String telephone) {
        boolean exists = patientRepository.existsByTelephone(telephone);
        return ResponseEntity.ok(ApiResponse.success("Vérification", Map.of(
                "telephone", telephone,
                "exists", exists,
                "message", exists ? "Ce numéro est déjà inscrit." : "Ce numéro est disponible."
        )));
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkEmail(@PathVariable String email) {
        boolean exists = patientRepository.existsByEmail(email.toLowerCase());
        return ResponseEntity.ok(ApiResponse.success("Vérification", Map.of(
                "email", email,
                "exists", exists,
                "message", exists ? "Cet email est déjà utilisé." : "Cet email est disponible."
        )));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // ACTIVATION ESSAI GRATUIT (demande explicite depuis Paramètres)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/public/auth/patient/activate-trial
     * Activer l'essai gratuit 15 jours pour un patient qui vient de vérifier son email.
     * Appelé depuis l'app mobile après vérification OTP dans Paramètres.
     */
    @PostMapping("/activate-trial")
    @Transactional
    @Operation(summary = "Activer l'essai gratuit", description = "Active l'essai 15 jours après vérification email depuis les Paramètres")
    public ResponseEntity<ApiResponse<Map<String, Object>>> activateTrial(
            @RequestHeader("Authorization") String bearerToken) {

        String token = jwtTokenProvider.extractTokenFromBearer(bearerToken);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).body(ApiResponse.error("Token invalide ou expiré."));
        }

        String patientId = jwtTokenProvider.getUsernameFromToken(token);
        Optional<Patient> patientOpt;
        try {
            patientOpt = patientRepository.findById(UUID.fromString(patientId));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponse.error("Identifiant invalide."));
        }

        if (patientOpt.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResponse.error("Profil introuvable."));
        }

        Patient patient = patientOpt.get();

        // Vérifier que l'email est bien vérifié
        if (!patient.isEmailVerified()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Vérifiez d'abord votre email pour activer l'essai gratuit.")
            );
        }

        // Vérifier que le trial n'a pas déjà été utilisé
        boolean hadTrial = subscriptionRepository.existsByPatientIdAndIsTrialTrue(patient.getId());
        if (hadTrial) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("L'essai gratuit a déjà été utilisé sur ce compte.")
            );
        }

        // Vérifier pas d'abonnement actif
        boolean hasActive = subscriptionRepository
                .findByPatientIdAndStatus(patient.getId(), SubscriptionStatus.ACTIVE)
                .isPresent();
        if (hasActive) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Vous avez déjà un abonnement actif.")
            );
        }

        // Activer l'essai
        Subscription sub = createFreeTrialSubscription(patient);
        patient.setPremiumActif(true);
        patient.setDateFinPremium(sub.getExpiresAt().toLocalDate());
        patient.setMontantPremium(0);
        patientRepository.save(patient);

        log.info("🎁 Essai gratuit activé depuis Paramètres: patient={} jusqu'au {}", patient.getId(), patient.getDateFinPremium());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("activated", true);
        result.put("expiresAt", sub.getExpiresAt().toString());
        result.put("daysRemaining", 15);
        result.put("message", "Essai gratuit de 15 jours activé !");

        return ResponseEntity.ok(ApiResponse.success("Essai gratuit activé", result));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // REFRESH TOKEN (SLIDING SESSION)
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/public/auth/patient/refresh-token
     *
     * Sliding session : si le token actuel est encore valide, on renvoie un nouveau
     * token patient de 7 jours. Flutter appelle cet endpoint au démarrage de l'app.
     *
     * - Token valide + patient actif → nouveau token 7j
     * - Token expiré → 401 → l'app renvoie l'utilisateur vers /login
     * - Patient suspendu → 401
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "Rafraîchir le token patient", description = "Sliding session : retourne un nouveau token 7 jours si l'actuel est encore valide")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshToken(
            @RequestHeader("Authorization") String bearerToken) {

        String token = jwtTokenProvider.extractTokenFromBearer(bearerToken);

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).body(
                    ApiResponse.error("Token invalide ou expiré. Veuillez vous reconnecter.")
            );
        }

        String patientId = jwtTokenProvider.getUsernameFromToken(token);

        Optional<sn.sunufarmasi.patient.entity.Patient> patientOpt;
        try {
            patientOpt = patientRepository.findById(UUID.fromString(patientId));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponse.error("Token invalide."));
        }

        if (patientOpt.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Compte introuvable."));
        }

        sn.sunufarmasi.patient.entity.Patient patient = patientOpt.get();

        if (!patient.isActif()) {
            return ResponseEntity.status(401).body(
                    ApiResponse.error("Votre compte a été désactivé. Contactez le support.")
            );
        }

        // Émettre un nouveau token 7 jours (sliding session)
        String newToken = jwtTokenProvider.generatePatientToken(patientId);

        log.info("🔄 Token patient rafraîchi: {}", patientId);

        return ResponseEntity.ok(ApiResponse.success("Token rafraîchi",
                Map.of(
                        "token", newToken,
                        "expiresIn", "7 jours",
                        "patientId", patientId
                )
        ));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES
    // ═══════════════════════════════════════════════════════════════════════════════

    private Subscription createFreeTrialSubscription(Patient patient) {
        SubscriptionPlan freePlan = subscriptionPlanRepository.findByCode("FREE_TRIAL")
                .orElseThrow(() -> new RuntimeException("Plan FREE_TRIAL non trouvé."));
        LocalDateTime now = LocalDateTime.now();
        Subscription sub = Subscription.builder()
                .patient(patient)
                .plan(freePlan)
                .status(SubscriptionStatus.ACTIVE)
                .startsAt(now)
                .expiresAt(now.plusDays(freePlan.getDureeJours()))
                .isTrial(true)
                .autoRenew(false)
                .build();
        return subscriptionRepository.save(sub);
    }

    private String generateOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStore.put(email, new OtpData(otp, LocalDateTime.now().plusMinutes(10), false));
        log.info("🔑 OTP généré pour {} (dev log uniquement)", email);
        return otp;
    }

    private String generateToken(Patient patient) {
        return jwtTokenProvider.generatePatientToken(patient.getId().toString());
    }

    private Map<String, Object> buildAuthResponse(Patient patient, Subscription subscription) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("patientId", patient.getId().toString());
        result.put("nomComplet", patient.getNomComplet());
        result.put("telephone", patient.getTelephone());
        result.put("email", patient.getEmail());
        result.put("emailVerified", patient.isEmailVerified());
        result.put("token", generateToken(patient));

        if (patient.getCommune() != null) {
            Map<String, Object> loc = new LinkedHashMap<>();
            loc.put("communeId", patient.getCommune().getId().toString());
            loc.put("communeNom", patient.getCommune().getNom());
            if (patient.getCommune().getDepartement() != null) {
                loc.put("departementNom", patient.getCommune().getDepartement().getNom());
                if (patient.getCommune().getDepartement().getRegion() != null) {
                    loc.put("regionNom", patient.getCommune().getDepartement().getRegion().getNom());
                }
            }
            result.put("localisation", loc);
        }

        if (subscription != null) {
            long days = Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDateTime.now().toLocalDate(), subscription.getExpiresAt().toLocalDate()));
            Map<String, Object> subMap = new LinkedHashMap<>();
            subMap.put("hasActiveSubscription", true);
            subMap.put("plan", subscription.getPlan().getCode());
            subMap.put("planNom", subscription.getPlan().getNom());
            subMap.put("status", subscription.getStatus().name());
            subMap.put("expiresAt", subscription.getExpiresAt().toString());
            subMap.put("daysRemaining", (int) days);
            subMap.put("isTrial", subscription.isTrial());
            subMap.put("canSearchMedicaments", true);
            result.put("subscription", subMap);
            result.put("canSearchMedicaments", true);
            result.put("requiresPayment", false);
        } else {
            result.put("subscription", null);
            result.put("canSearchMedicaments", false);
            result.put("requiresPayment", true);
        }

        return result;
    }

    /** Masquer l'email pour la sécurité: a***@gmail.com */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String local = parts[0];
        String visible = local.length() > 2 ? local.substring(0, 2) + "***" : local.charAt(0) + "***";
        return visible + "@" + parts[1];
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // DTOs INTERNES
    // ═══════════════════════════════════════════════════════════════════════════════

    public record RegisterBasicRequest(
            @NotBlank(message = "Le nom complet est obligatoire")
            @jakarta.validation.constraints.Size(min = 2, max = 255)
            String nomComplet,

            @NotBlank(message = "Le téléphone est obligatoire")
            String telephone,

            String sexe,

            String communeId,

            @NotBlank(message = "L'identifiant de l'appareil est obligatoire")
            String deviceId
    ) {
        public RegisterBasicRequest {
            if (telephone != null) {
                telephone = telephone.trim();
                if (telephone.startsWith("7") && telephone.length() == 9) {
                    telephone = "+221" + telephone;
                } else if (telephone.startsWith("221") && telephone.length() == 12) {
                    telephone = "+" + telephone;
                }
            }
        }
    }

    public record VerifyOtpRequest(
            @NotBlank(message = "L'email est requis")
            @Email
            String email,

            @NotBlank(message = "Le code OTP est requis")
            @jakarta.validation.constraints.Pattern(regexp = "^[0-9]{6}$", message = "Code à 6 chiffres")
            String otp,

            @NotBlank(message = "L'identifiant de l'appareil est requis")
            String deviceId
    ) {}

    private record OtpData(String otp, LocalDateTime expiresAt, boolean used) {
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
        OtpData markUsed() {
            return new OtpData(otp, expiresAt, true);
        }
    }

    public record VerifyLoginRequest(
            @NotBlank(message = "Le téléphone est requis")
            String telephone,

            @NotBlank(message = "Le code OTP est requis")
            @jakarta.validation.constraints.Pattern(regexp = "^[0-9]{6}$", message = "Code à 6 chiffres")
            String otp,

            @NotBlank(message = "L'identifiant de l'appareil est requis")
            String deviceId
    ) {}
}
