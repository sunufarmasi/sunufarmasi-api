package sn.sunufarmasi.patient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.shared.dto.ApiResponse;

import sn.sunufarmasi.patient.entity.Patient;
import sn.sunufarmasi.patient.repository.PatientRepository;
import sn.sunufarmasi.patient.dto.request.RegisterPatientRequest;
import sn.sunufarmasi.patient.mapper.PatientMapper;
import sn.sunufarmasi.subscription.entity.Subscription;
import sn.sunufarmasi.subscription.entity.SubscriptionPlan;
import sn.sunufarmasi.subscription.entity.SubscriptionStatus;
import sn.sunufarmasi.subscription.repository.SubscriptionRepository;
import sn.sunufarmasi.subscription.repository.SubscriptionPlanRepository;
import sn.sunufarmasi.localisation.entity.Commune;
import sn.sunufarmasi.localisation.repository.CommuneRepository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Contrôleur d'authentification pour les patients (app mobile)
 * Endpoints PUBLICS - Pas de JWT requis
 *
 * Flow d'inscription:
 * 1. POST /register → Crée patient + envoie OTP
 * 2. POST /verify-otp → Vérifie OTP + retourne token
 * 3. Patient a 15 jours d'essai gratuit
 *
 * Flow de connexion:
 * 1. POST /login → Envoie OTP
 * 2. POST /verify-login → Vérifie OTP + retourne token
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
    private final PatientMapper patientMapper;

    // Stockage temporaire des OTP (en production: Redis ou base de données)
    private static final Map<String, OtpData> otpStore = new HashMap<>();

    // ═══════════════════════════════════════════════════════════════════════════════
    // INSCRIPTION
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/public/auth/patient/register
     * Inscription d'un nouveau patient avec essai gratuit 15 jours
     */
    @PostMapping("/register")
    @Transactional
    @Operation(summary = "Inscription patient", description = "Crée un compte patient avec 15 jours d'essai gratuit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(
            @Valid @RequestBody RegisterPatientRequest request) {
        log.info("POST /api/v1/public/auth/patient/register - telephone={}", request.telephone());

        // 1. Vérifier si le téléphone existe déjà
        if (patientRepository.existsByTelephone(request.telephone())) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Ce numéro de téléphone est déjà inscrit. Veuillez vous connecter.")
            );
        }

        // 2. Vérifier la commune
        Commune commune = null;
        if (request.communeId() != null && !request.communeId().isBlank()) {
            commune = communeRepository.findById(UUID.fromString(request.communeId()))
                    .orElse(null);
            if (commune == null) {
                log.warn("Commune non trouvée: {}", request.communeId());
            }
        }

        // 3. Créer le patient
        Patient patient = Patient.builder()
                .nomComplet(request.nomComplet())
                .telephone(request.telephone())
                .commune(commune)
                .dateNaissance(request.dateNaissance())
                .sexe(request.sexe())
                .adresse(request.adresse())
                .telephoneVerified(false) // Sera vérifié après OTP
                .emailVerified(false)
                .actif(true)
                .build();

        patient = patientRepository.save(patient);
        log.info("✅ Patient créé: {} - {}", patient.getId(), patient.getNomComplet());

        // 4. Créer l'abonnement gratuit de 15 jours
        Subscription subscription = createFreeTrialSubscription(patient);
        log.info("🎁 Essai gratuit activé jusqu'au {}", subscription.getExpiresAt());

        // 5. Générer et envoyer OTP
        String otp = generateAndSendOtp(request.telephone());

        // 6. Construire la réponse
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("patientId", patient.getId().toString());
        result.put("nomComplet", patient.getNomComplet());
        result.put("telephone", patient.getTelephone());
        result.put("message", "Inscription réussie ! Un code de vérification a été envoyé au " + request.telephone());
        result.put("requiresOtp", true);
        result.put("otpExpiresIn", "5 minutes");
        result.put("subscription", Map.of(
                "plan", "FREE_TRIAL",
                "planNom", "Essai gratuit",
                "dureeJours", 15,
                "expiresAt", subscription.getExpiresAt().toString()
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inscription réussie", result));
    }

    /**
     * POST /api/v1/public/auth/patient/verify-otp
     * Vérification du code OTP après inscription
     */
    @PostMapping("/verify-otp")
    @Transactional
    @Operation(summary = "Vérifier OTP", description = "Vérifie le code OTP et active le compte")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        log.info("POST /api/v1/public/auth/patient/verify-otp - telephone={}", request.telephone());

        // 1. Vérifier l'OTP
        OtpData otpData = otpStore.get(request.telephone());

        if (otpData == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Aucun code en attente pour ce numéro. Veuillez demander un nouveau code.")
            );
        }

        if (otpData.isExpired()) {
            otpStore.remove(request.telephone());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Le code a expiré. Veuillez demander un nouveau code.")
            );
        }

        if (!otpData.otp().equals(request.otp())) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Code incorrect. Veuillez vérifier et réessayer.")
            );
        }

        // 2. OTP valide - Marquer le téléphone comme vérifié
        Optional<Patient> patientOpt = patientRepository.findByTelephone(request.telephone());
        if (patientOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Patient non trouvé. Veuillez vous réinscrire.")
            );
        }

        Patient patient = patientOpt.get();
        patient.setTelephoneVerified(true);
        patient.updateLastLogin();
        patientRepository.save(patient);

        // 3. Supprimer l'OTP utilisé
        otpStore.remove(request.telephone());

        // 4. Récupérer l'abonnement actif
        Optional<Subscription> subscriptionOpt = subscriptionRepository
                .findByPatientIdAndStatus(patient.getId(), SubscriptionStatus.ACTIVE);

        // 5. Construire la réponse
        Map<String, Object> result = buildAuthResponse(patient, subscriptionOpt.orElse(null));

        log.info("✅ Téléphone vérifié pour patient: {}", patient.getId());

        return ResponseEntity.ok(ApiResponse.success("Téléphone vérifié avec succès", result));
    }

    /**
     * POST /api/v1/public/auth/patient/resend-otp
     * Renvoyer le code OTP
     */
    @PostMapping("/resend-otp")
    @Operation(summary = "Renvoyer OTP", description = "Renvoie un nouveau code de vérification")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resendOtp(
            @RequestParam String telephone) {
        log.info("POST /api/v1/public/auth/patient/resend-otp - telephone={}", telephone);

        // Vérifier que le patient existe
        if (!patientRepository.existsByTelephone(telephone)) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Aucun compte associé à ce numéro. Veuillez vous inscrire.")
            );
        }

        // Générer et envoyer un nouveau OTP
        generateAndSendOtp(telephone);

        Map<String, Object> result = Map.of(
                "message", "Un nouveau code a été envoyé au " + telephone,
                "expiresIn", "5 minutes"
        );

        return ResponseEntity.ok(ApiResponse.success("Code renvoyé", result));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // CONNEXION
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/public/auth/patient/login
     * Demander un code de connexion
     */
    @PostMapping("/login")
    @Operation(summary = "Demander connexion", description = "Envoie un code OTP pour se connecter")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @RequestParam String telephone) {
        log.info("POST /api/v1/public/auth/patient/login - telephone={}", telephone);

        // 1. Vérifier que le patient existe
        Optional<Patient> patientOpt = patientRepository.findByTelephone(telephone);
        if (patientOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Aucun compte associé à ce numéro. Veuillez vous inscrire.")
            );
        }

        Patient patient = patientOpt.get();

        // 2. Vérifier que le compte est actif
        if (!patient.isActif()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Votre compte a été désactivé. Contactez le support.")
            );
        }

        // 3. Générer et envoyer OTP
        generateAndSendOtp(telephone);

        Map<String, Object> result = Map.of(
                "message", "Un code de connexion a été envoyé au " + telephone,
                "requiresOtp", true,
                "patientId", patient.getId().toString(),
                "expiresIn", "5 minutes"
        );

        return ResponseEntity.ok(ApiResponse.success("Code envoyé", result));
    }

    /**
     * POST /api/v1/public/auth/patient/verify-login
     * Vérifier le code de connexion
     */
    @PostMapping("/verify-login")
    @Transactional
    @Operation(summary = "Vérifier connexion", description = "Vérifie le code OTP et connecte le patient")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyLogin(
            @Valid @RequestBody VerifyOtpRequest request) {
        log.info("POST /api/v1/public/auth/patient/verify-login - telephone={}", request.telephone());

        // Même logique que verify-otp
        return verifyOtp(request);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // VÉRIFICATION ABONNEMENT
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/public/auth/patient/check-subscription/{patientId}
     * Vérifier l'état de l'abonnement
     */
    @GetMapping("/check-subscription/{patientId}")
    @Transactional
    @Operation(summary = "Vérifier abonnement", description = "Retourne l'état de l'abonnement du patient")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkSubscription(
            @PathVariable UUID patientId) {
        log.info("GET /api/v1/public/auth/patient/check-subscription/{}", patientId);

        // 1. Vérifier que le patient existe
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (patientOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Patient patient = patientOpt.get();

        // 2. Chercher l'abonnement actif
        Optional<Subscription> activeSubOpt = subscriptionRepository
                .findByPatientIdAndStatus(patientId, SubscriptionStatus.ACTIVE);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("patientId", patientId.toString());
        result.put("nomComplet", patient.getNomComplet());

        if (activeSubOpt.isPresent()) {
            Subscription sub = activeSubOpt.get();

            // Vérifier si expiré
            if (sub.getExpiresAt().isBefore(LocalDateTime.now())) {
                // Marquer comme expiré
                sub.setStatus(SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(sub);

                result.put("hasActiveSubscription", false);
                result.put("status", "EXPIRED");
                result.put("canSearchMedicaments", false);
                result.put("message", "Votre abonnement a expiré. Renouvelez pour accéder à la recherche de médicaments.");
            } else {
                // Abonnement actif
                long daysRemaining = java.time.Duration.between(
                        LocalDateTime.now(), sub.getExpiresAt()).toDays();

                result.put("hasActiveSubscription", true);
                result.put("status", sub.getStatus().name());
                result.put("plan", sub.getPlan().getCode());
                result.put("planNom", sub.getPlan().getNom());
                result.put("expiresAt", sub.getExpiresAt().toString());
                result.put("daysRemaining", daysRemaining);
                result.put("canSearchMedicaments", true);
                result.put("isTrial", sub.isTrial());

                // Avertissement si expiration proche
                if (daysRemaining <= 3) {
                    result.put("warning", "Votre abonnement expire dans " + daysRemaining + " jour(s)");
                }
            }
        } else {
            result.put("hasActiveSubscription", false);
            result.put("status", "NO_SUBSCRIPTION");
            result.put("canSearchMedicaments", false);
            result.put("message", "Aucun abonnement actif. Souscrivez pour accéder à la recherche de médicaments.");
        }

        // 3. Lister les plans disponibles
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findByActifTrueOrderByOrdreAsc();
        result.put("availablePlans", plans.stream()
                .filter(p -> !"FREE_TRIAL".equals(p.getCode())) // Exclure le plan gratuit
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

    /**
     * GET /api/v1/public/auth/patient/check-phone/{telephone}
     * Vérifier si un téléphone est déjà inscrit
     */
    @GetMapping("/check-phone/{telephone}")
    @Operation(summary = "Vérifier téléphone", description = "Vérifie si un numéro est déjà inscrit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkPhone(
            @PathVariable String telephone) {
        log.info("GET /api/v1/public/auth/patient/check-phone/{}", telephone);

        boolean exists = patientRepository.existsByTelephone(telephone);

        Map<String, Object> result = Map.of(
                "telephone", telephone,
                "exists", exists,
                "message", exists
                        ? "Ce numéro est déjà inscrit. Vous pouvez vous connecter."
                        : "Ce numéro n'est pas inscrit. Vous pouvez créer un compte."
        );

        return ResponseEntity.ok(ApiResponse.success("Vérification", result));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Créer l'abonnement essai gratuit 15 jours
     */
    private Subscription createFreeTrialSubscription(Patient patient) {
        // Récupérer le plan FREE_TRIAL
        SubscriptionPlan freePlan = subscriptionPlanRepository.findByCode("FREE_TRIAL")
                .orElseThrow(() -> new RuntimeException(
                        "Plan FREE_TRIAL non trouvé. Exécutez seed-plans d'abord."));

        LocalDateTime now = LocalDateTime.now();

        Subscription subscription = Subscription.builder()
                .patient(patient)
                .plan(freePlan)
                .status(SubscriptionStatus.ACTIVE)
                .startsAt(now)
                .expiresAt(now.plusDays(freePlan.getDureeJours())) // +15 jours
                .isTrial(true)
                .autoRenew(false)
                .build();

        return subscriptionRepository.save(subscription);
    }

    /**
     * Générer et envoyer un OTP
     */
    private String generateAndSendOtp(String telephone) {
        // Générer un OTP à 6 chiffres
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Stocker avec expiration 5 minutes
        otpStore.put(telephone, new OtpData(otp, LocalDateTime.now().plusMinutes(5)));

        // TODO: En production, envoyer par SMS via Orange SMS API ou autre
        log.info("📱 OTP généré pour {}: {} (en prod: envoyer par SMS)", telephone, otp);

        // En dev, on peut aussi l'afficher dans les logs
        System.out.println("═══════════════════════════════════════════");
        System.out.println("📱 CODE OTP POUR " + telephone + " : " + otp);
        System.out.println("═══════════════════════════════════════════");

        return otp;
    }

    /**
     * Générer un token d'authentification
     */
    private String generateToken(Patient patient) {
        // TODO: En production, générer un vrai JWT avec Spring Security
        // Pour l'instant, token simple encodé en Base64
        String payload = String.format("%s:%s:%d",
                patient.getId(),
                patient.getTelephone(),
                System.currentTimeMillis());
        return Base64.getEncoder().encodeToString(payload.getBytes());
    }

    /**
     * Construire la réponse d'authentification
     */
    private Map<String, Object> buildAuthResponse(Patient patient, Subscription subscription) {
        Map<String, Object> result = new LinkedHashMap<>();

        // Infos patient
        result.put("patientId", patient.getId().toString());
        result.put("nomComplet", patient.getNomComplet());
        result.put("telephone", patient.getTelephone());
        result.put("email", patient.getEmail());
        result.put("telephoneVerified", patient.isTelephoneVerified());
        result.put("token", generateToken(patient));

        // Localisation
        if (patient.getCommune() != null) {
            Map<String, Object> localisation = new LinkedHashMap<>();
            localisation.put("communeId", patient.getCommune().getId().toString());
            localisation.put("communeNom", patient.getCommune().getNom());
            if (patient.getCommune().getDepartement() != null) {
                localisation.put("departementNom", patient.getCommune().getDepartement().getNom());
                if (patient.getCommune().getDepartement().getRegion() != null) {
                    localisation.put("regionNom", patient.getCommune().getDepartement().getRegion().getNom());
                }
            }
            result.put("localisation", localisation);
        }

        // Abonnement
        if (subscription != null) {
            long daysRemaining = java.time.Duration.between(
                    LocalDateTime.now(), subscription.getExpiresAt()).toDays();

            result.put("subscription", Map.of(
                    "plan", subscription.getPlan().getCode(),
                    "planNom", subscription.getPlan().getNom(),
                    "status", subscription.getStatus().name(),
                    "expiresAt", subscription.getExpiresAt().toString(),
                    "daysRemaining", Math.max(0, daysRemaining),
                    "isTrial", subscription.isTrial(),
                    "canSearchMedicaments", true
            ));
            result.put("canSearchMedicaments", true);
        } else {
            result.put("subscription", null);
            result.put("canSearchMedicaments", false);
        }

        return result;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // DTOs INTERNES
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Request pour vérification OTP
     */
    public record VerifyOtpRequest(
            @jakarta.validation.constraints.NotBlank(message = "Le téléphone est requis")
            String telephone,

            @jakarta.validation.constraints.NotBlank(message = "Le code OTP est requis")
            @jakarta.validation.constraints.Pattern(regexp = "^[0-9]{6}$", message = "Le code doit contenir 6 chiffres")
            String otp
    ) {}

    /**
     * Données OTP stockées temporairement
     */
    private record OtpData(String otp, LocalDateTime expiresAt) {
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }
}