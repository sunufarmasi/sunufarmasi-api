package sn.sunufarmasi.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.patient.entity.Patient;
import sn.sunufarmasi.patient.repository.PatientRepository;
import sn.sunufarmasi.payment.dto.request.InitiatePaymentRequest;
import sn.sunufarmasi.payment.dto.response.PaymentResponse;
import sn.sunufarmasi.payment.entity.Payment;
import sn.sunufarmasi.payment.entity.PaymentStatus;
import sn.sunufarmasi.payment.repository.PaymentRepository;
import sn.sunufarmasi.shared.exception.BadRequestException;
import sn.sunufarmasi.shared.exception.ResourceNotFoundException;
import sn.sunufarmasi.subscription.entity.Subscription;
import sn.sunufarmasi.subscription.entity.SubscriptionPlan;
import sn.sunufarmasi.subscription.service.SubscriptionService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des paiements
 * Mock Orange Money/Wave pour développement
 *
 * @author WeCan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionService subscriptionService;
    private final WaveService waveService;
    private final PatientRepository patientRepository;

    // ═══════════════════════════════════════════════════════════
    // INITIATION PAIEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Initier un paiement pour un abonnement
     */
    @Transactional
    public PaymentResponse initiatePayment(Patient patient, InitiatePaymentRequest request) {
        log.info("💳 Initiation paiement pour patient: {}", patient.getId());

        // Récupérer le plan
        SubscriptionPlan plan = subscriptionService.getPlanById(request.planId());

        // Vérifier que ce n'est pas FREE_TRIAL
        if ("FREE_TRIAL".equals(plan.getCode())) {
            throw new BadRequestException("Impossible de payer pour l'essai gratuit");
        }

        // Créer le paiement en PENDING
        String ref = Payment.generateReferenceInterne();
        Payment payment = Payment.builder()
                .patient(patient)
                .montant(plan.getPrix())
                .methode(request.methode())
                .status(PaymentStatus.PENDING)
                .telephonePaiement(request.telephonePaiement())
                .referenceInterne(ref)
                .planId(plan.getId().toString())
                .build();

        // Pour Wave : créer la session checkout
        if (request.methode() == sn.sunufarmasi.payment.entity.PaymentMethod.WAVE) {
            WaveService.WaveCheckoutSession session =
                    waveService.createCheckoutSession(plan.getPrix(), ref);
            payment.setWaveCheckoutId(session.id());
            payment.setWaveCheckoutUrl(session.waveCheckoutUrl());
            log.info("🌊 Session Wave créée: {} → {}", session.id(), session.waveCheckoutUrl());
        }

        payment = paymentRepository.save(payment);
        log.info("📝 Paiement créé: {} - {} FCFA - {}",
                payment.getReferenceInterne(), payment.getMontant(), payment.getMethode());

        // Pour les méthodes non-Wave (futur) : mock
        if (request.methode() != sn.sunufarmasi.payment.entity.PaymentMethod.WAVE) {
            boolean success = mockPaymentProvider(payment);
            if (success) {
                payment.markAsSuccess("MOCK-" + System.currentTimeMillis());
                Subscription subscription = subscriptionService.createSubscription(patient, plan);
                payment.setSubscription(subscription);
                log.info("✅ Paiement mock réussi - Abonnement: {}", subscription.getId());
            } else {
                payment.markAsFailed("Échec simulé");
                log.warn("❌ Paiement mock échoué: {}", payment.getReferenceInterne());
            }
            payment = paymentRepository.save(payment);
        }

        return mapToPaymentResponse(payment);
    }

    /**
     * Mock du fournisseur de paiement (Orange Money/Wave)
     * EN PRODUCTION: Remplacer par vraie intégration API
     */
    private boolean mockPaymentProvider(Payment payment) {
        log.info("🔄 Mock: Appel API {} pour {} FCFA",
                payment.getMethode(), payment.getMontant());

        try {
            // Simuler délai réseau
            Thread.sleep(2000);

            // Mock: 90% de succès
            boolean success = Math.random() < 0.9;

            if (success) {
                log.info("✅ Mock: Paiement approuvé par {}", payment.getMethode());
            } else {
                log.warn("❌ Mock: Paiement refusé par {}", payment.getMethode());
            }

            return success;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Erreur mock paiement", e);
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // WEBHOOK WAVE
    // ═══════════════════════════════════════════════════════════

    /**
     * Traiter un paiement Wave réussi (appelé par WaveWebhookController)
     *
     * @param referenceInterne  Notre référence (client_reference Wave)
     * @param waveTransactionId ID de transaction Wave
     */
    @Transactional
    public void handleWaveSuccess(String referenceInterne, String waveTransactionId) {
        log.info("✅ Wave succès: ref={} waveId={}", referenceInterne, waveTransactionId);

        Payment payment = paymentRepository.findByReferenceInterne(referenceInterne)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé: " + referenceInterne));

        if (payment.isSuccess()) {
            log.info("⚠️ Paiement déjà traité: {}", referenceInterne);
            return;
        }

        payment.markAsSuccess(waveTransactionId);

        if (payment.getSubscription() == null && payment.getPlanId() != null) {
            SubscriptionPlan plan = subscriptionService.getPlanById(payment.getPlanId());
            Subscription subscription = subscriptionService.createSubscription(payment.getPatient(), plan);
            payment.setSubscription(subscription);
            log.info("🎉 Abonnement activé: patient={} plan={}",
                    payment.getPatient().getId(), plan.getCode());
        }

        paymentRepository.save(payment);
    }

    /**
     * Traiter un paiement Wave échoué
     */
    @Transactional
    public void handleWaveFailure(String referenceInterne, String reason) {
        log.warn("❌ Wave échec: ref={} raison={}", referenceInterne, reason);

        Payment payment = paymentRepository.findByReferenceInterne(referenceInterne)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé: " + referenceInterne));

        if (!payment.isPending()) {
            log.info("⚠️ Paiement déjà finalisé ({}): {}", payment.getStatus(), referenceInterne);
            return;
        }

        payment.markAsFailed(reason);
        paymentRepository.save(payment);
    }

    // ═══════════════════════════════════════════════════════════
    // VÉRIFICATION PAIEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Vérifier le statut d'un paiement
     */
    public PaymentResponse checkPaymentStatus(String referenceInterne) {
        log.debug("Vérification statut paiement: {}", referenceInterne);

        Payment payment = paymentRepository.findByReferenceInterne(referenceInterne)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));

        return mapToPaymentResponse(payment);
    }

    /**
     * Obtenir l'historique des paiements d'un patient
     */
    public List<PaymentResponse> getPatientPayments(UUID patientId) {
        log.debug("Récupération historique paiements patient: {}", patientId);

        List<Payment> payments = paymentRepository.findByPatientIdOrderByCreatedAtDesc(patientId);

        return payments.stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    // PAIEMENT MANUEL (Wave B2B sans API)
    // ═══════════════════════════════════════════════════════════

    /**
     * Soumettre une demande de paiement manuel
     * L'utilisateur a envoyé l'argent via Wave B2B et fournit sa référence de transaction
     */
    @Transactional
    public PaymentResponse submitManualPayment(Patient patient, String planId, String waveReference) {
        log.info("📝 Paiement manuel soumis: patient={} plan={} ref={}", patient.getId(), planId, waveReference);

        SubscriptionPlan plan = subscriptionService.getPlanById(planId);

        if ("FREE_TRIAL".equals(plan.getCode())) {
            throw new BadRequestException("Impossible de payer pour l'essai gratuit");
        }

        String ref = Payment.generateReferenceInterne();
        Payment payment = Payment.builder()
                .patient(patient)
                .montant(plan.getPrix())
                .methode(sn.sunufarmasi.payment.entity.PaymentMethod.WAVE)
                .status(PaymentStatus.PENDING_VALIDATION)
                .referenceExterne(waveReference)
                .referenceInterne(ref)
                .planId(plan.getId().toString())
                .build();

        payment = paymentRepository.save(payment);
        log.info("✅ Demande manuelle enregistrée: {} — en attente de validation admin", ref);

        return mapToPaymentResponse(payment);
    }

    /**
     * Valider un paiement manuel (admin uniquement)
     * Active l'abonnement du patient
     */
    @Transactional
    public PaymentResponse validateManualPayment(String referenceInterne) {
        log.info("✅ Validation paiement manuel: {}", referenceInterne);

        Payment payment = paymentRepository.findByReferenceInterne(referenceInterne)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé: " + referenceInterne));

        if (payment.getStatus() != PaymentStatus.PENDING_VALIDATION) {
            throw new BadRequestException("Ce paiement ne peut pas être validé (statut: " + payment.getStatus() + ")");
        }

        payment.markAsSuccess(payment.getReferenceExterne());

        SubscriptionPlan plan = subscriptionService.getPlanById(payment.getPlanId());
        // upgradeToMonthly annule l'essai gratuit en cours si existant et crée l'abonnement payant
        int mois = Math.max(1, plan.getDureeJours() / 30);
        Subscription subscription = subscriptionService.upgradeToMonthly(payment.getPatient(), mois);
        payment.setSubscription(subscription);

        // Mettre à jour les champs premium du patient
        Patient patient = payment.getPatient();
        patient.setPremiumActif(true);
        LocalDate fin = LocalDate.now().plusDays(plan.getDureeJours());
        patient.setDateFinPremium(fin);
        patient.setMontantPremium(plan.getPrix());
        patient.setReferencePaiement(payment.getReferenceExterne());
        patientRepository.save(patient);

        paymentRepository.save(payment);
        log.info("🎉 Paiement validé — abonnement activé: patient={} jusqu'au {}", payment.getPatient().getId(), fin);

        return mapToPaymentResponse(payment);
    }

    /**
     * Rejeter un paiement manuel (admin uniquement)
     */
    @Transactional
    public PaymentResponse rejectManualPayment(String referenceInterne, String motif) {
        log.warn("❌ Rejet paiement manuel: {} motif={}", referenceInterne, motif);

        Payment payment = paymentRepository.findByReferenceInterne(referenceInterne)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé: " + referenceInterne));

        if (payment.getStatus() != PaymentStatus.PENDING_VALIDATION) {
            throw new BadRequestException("Ce paiement ne peut pas être rejeté (statut: " + payment.getStatus() + ")");
        }

        payment.markAsFailed(motif != null ? motif : "Rejeté par l'administrateur");
        paymentRepository.save(payment);

        return mapToPaymentResponse(payment);
    }

    /**
     * Lister les paiements en attente de validation
     */
    public List<PaymentResponse> getPendingValidation() {
        return paymentRepository.findByStatusOrderByCreatedAtDesc(PaymentStatus.PENDING_VALIDATION)
                .stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    // WEBHOOK (pour production)
    // ═══════════════════════════════════════════════════════════

    /**
     * Traiter le callback du fournisseur de paiement
     * À implémenter en production
     */
    @Transactional
    public void handlePaymentCallback(String referenceExterne, String status, String message) {
        log.info("📥 Callback paiement reçu: {} - Status: {}", referenceExterne, status);

        Payment payment = paymentRepository.findByReferenceExterne(referenceExterne)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));

        if ("SUCCESS".equals(status)) {
            payment.markAsSuccess(referenceExterne);

            // Créer l'abonnement si pas encore fait
            if (payment.getSubscription() == null) {
                Subscription subscription = subscriptionService.createSubscription(
                        payment.getPatient(),
                        subscriptionService.getPlanById(payment.getSubscription().getPlan().getId().toString())
                );
                payment.setSubscription(subscription);
            }

        } else {
            payment.markAsFailed(message);
        }

        paymentRepository.save(payment);

        log.info("✅ Callback traité pour paiement: {}", payment.getReferenceInterne());
    }

    // ═══════════════════════════════════════════════════════════
    // MAPPER
    // ═══════════════════════════════════════════════════════════

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        String patientNom = payment.getPatient() != null ? payment.getPatient().getNomComplet() : null;
        String patientId  = payment.getPatient() != null ? payment.getPatient().getId().toString() : null;
        String planCode   = null;
        if (payment.getPlanId() != null) {
            try {
                planCode = subscriptionService.getPlanById(payment.getPlanId()).getCode();
            } catch (Exception ignored) {}
        }
        return new PaymentResponse(
                payment.getId().toString(),
                payment.getMontant(),
                PaymentResponse.formatMontant(payment.getMontant()),
                payment.getMethode(),
                payment.getStatus(),
                payment.getReferenceInterne(),
                payment.getReferenceExterne(),
                payment.getTelephonePaiement(),
                payment.getErrorMessage(),
                payment.getWaveCheckoutUrl(),
                payment.getPaidAt(),
                payment.getCreatedAt(),
                patientNom,
                patientId,
                planCode
        );
    }
}