package sn.sunufarmasi.subscription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.patient.entity.Patient;
import sn.sunufarmasi.subscription.dto.response.SubscriptionPlanResponse;
import sn.sunufarmasi.subscription.dto.response.SubscriptionResponse;
import sn.sunufarmasi.subscription.entity.Subscription;
import sn.sunufarmasi.subscription.entity.SubscriptionPlan;
import sn.sunufarmasi.subscription.entity.SubscriptionStatus;
import sn.sunufarmasi.subscription.repository.SubscriptionPlanRepository;
import sn.sunufarmasi.subscription.repository.SubscriptionRepository;
import sn.sunufarmasi.shared.constant.ErrorMessages;
import sn.sunufarmasi.shared.exception.BadRequestException;
import sn.sunufarmasi.shared.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des abonnements
 *
 * @author WeCan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;

    private static final String FREE_TRIAL_CODE = "FREE_TRIAL";
    private static final int FREE_TRIAL_DAYS = 15;

    // ═══════════════════════════════════════════════════════════
    // PLANS
    // ═══════════════════════════════════════════════════════════

    /**
     * Obtenir tous les plans disponibles
     */
    public List<SubscriptionPlanResponse> getAllPlans() {
        log.debug("Récupération de tous les plans");

        List<SubscriptionPlan> plans = planRepository.findByActifTrueOrderByOrdreAsc();

        return plans.stream()
                .map(this::mapToPlanResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir un plan par son code
     */
    public SubscriptionPlan getPlanByCode(String code) {
        return planRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.PLAN_TARIFAIRE_NOT_FOUND));
    }

    /**
     * Obtenir un plan par ID
     */
    public SubscriptionPlan getPlanById(String planId) {
        return planRepository.findById(UUID.fromString(planId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.PLAN_TARIFAIRE_NOT_FOUND));
    }

    // ═══════════════════════════════════════════════════════════
    // ESSAI GRATUIT
    // ═══════════════════════════════════════════════════════════

    /**
     * Activer l'essai gratuit pour un nouveau patient
     */
    @Transactional
    public Subscription activateFreeTrial(Patient patient) {
        log.info("🎁 Activation essai gratuit pour patient: {}", patient.getId());

        // Vérifier si déjà utilisé
        if (subscriptionRepository.existsByPatientIdAndIsTrialTrue(patient.getId())) {
            throw new BadRequestException(ErrorMessages.SUBSCRIPTION_TRIAL_ALREADY_USED);
        }

        // Récupérer le plan FREE_TRIAL
        SubscriptionPlan freeTrialPlan = getPlanByCode(FREE_TRIAL_CODE);

        // Créer l'abonnement
        Subscription subscription = Subscription.builder()
                .patient(patient)
                .plan(freeTrialPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startsAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(FREE_TRIAL_DAYS))
                .isTrial(true)
                .autoRenew(false)
                .build();

        subscription = subscriptionRepository.save(subscription);

        log.info("✅ Essai gratuit activé jusqu'au: {}", subscription.getExpiresAt());

        return subscription;
    }

    // ═══════════════════════════════════════════════════════════
    // ABONNEMENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Obtenir l'abonnement actif d'un patient
     */
    public Subscription getActiveSubscription(UUID patientId) {
        return subscriptionRepository
                .findActiveByPatientId(patientId, LocalDateTime.now())
                .orElse(null);
    }

    /**
     * Vérifier si un patient a un abonnement actif
     */
    public boolean hasActiveSubscription(UUID patientId) {
        Subscription sub = getActiveSubscription(patientId);
        return sub != null && sub.isActive();
    }

    /**
     * Créer un nouvel abonnement après paiement
     */
    @Transactional
    public Subscription createSubscription(Patient patient, SubscriptionPlan plan) {
        log.info("📝 Création abonnement {} pour patient: {}", plan.getCode(), patient.getId());

        // Désactiver l'ancien abonnement s'il existe
        Subscription oldSub = getActiveSubscription(patient.getId());
        if (oldSub != null) {
            oldSub.cancel("Nouvel abonnement souscrit");
            subscriptionRepository.save(oldSub);
        }

        // Créer le nouvel abonnement
        Subscription subscription = Subscription.builder()
                .patient(patient)
                .plan(plan)
                .status(SubscriptionStatus.ACTIVE)
                .startsAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(plan.getDureeJours()))
                .isTrial(false)
                .autoRenew(true)  // Par défaut activé
                .build();

        subscription = subscriptionRepository.save(subscription);

        log.info("✅ Abonnement créé jusqu'au: {}", subscription.getExpiresAt());

        return subscription;
    }

    /**
     * Renouveler un abonnement
     */
    @Transactional
    public Subscription renewSubscription(UUID subscriptionId) {
        log.info("🔄 Renouvellement abonnement: {}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.ABONNEMENT_NOT_FOUND));

        // Renouveler
        subscription.renew(subscription.getPlan().getDureeJours());
        subscription = subscriptionRepository.save(subscription);

        log.info("✅ Abonnement renouvelé jusqu'au: {}", subscription.getExpiresAt());

        return subscription;
    }

    /**
     * Obtenir l'abonnement actif d'un patient (DTO)
     */
    public SubscriptionResponse getPatientActiveSubscription(UUID patientId) {
        Subscription sub = getActiveSubscription(patientId);

        if (sub == null) {
            return null;
        }

        return mapToSubscriptionResponse(sub);
    }

    // ═══════════════════════════════════════════════════════════
    // MAPPERS
    // ═══════════════════════════════════════════════════════════

    private SubscriptionPlanResponse mapToPlanResponse(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.from(
                plan.getId().toString(),
                plan.getCode(),
                plan.getNom(),
                plan.getDescription(),
                plan.getPrix(),
                plan.getDureeJours(),
                plan.isAvecPublicite(),
                plan.isActif()
        );
    }

    private SubscriptionResponse mapToSubscriptionResponse(Subscription sub) {
        SubscriptionPlanResponse planResponse = mapToPlanResponse(sub.getPlan());

        boolean isActive = sub.isActive();
        boolean isExpired = sub.isExpired();
        boolean isExpiringSoon = sub.isExpiringSoon();
        Long daysRemaining = sub.getDaysRemaining();

        String message = SubscriptionResponse.generateMessage(
                isActive, isExpired, isExpiringSoon, daysRemaining, sub.isTrial()
        );

        return new SubscriptionResponse(
                sub.getId().toString(),
                planResponse,
                sub.getStatus(),
                sub.getStartsAt(),
                sub.getExpiresAt(),
                sub.isAutoRenew(),
                sub.isTrial(),
                isActive,
                isExpired,
                isExpiringSoon,
                daysRemaining,
                message
        );
    }
}