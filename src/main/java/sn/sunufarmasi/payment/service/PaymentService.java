package sn.sunufarmasi.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.patient.entity.Patient;
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

        // Créer le paiement
        Payment payment = Payment.builder()
                .patient(patient)
                .montant(plan.getPrix())
                .methode(request.methode())
                .status(PaymentStatus.PENDING)
                .telephonePaiement(request.telephonePaiement())
                .referenceInterne(Payment.generateReferenceInterne())
                .build();

        payment = paymentRepository.save(payment);

        log.info("📝 Paiement créé: {} - Montant: {} FCFA",
                payment.getReferenceInterne(), payment.getMontant());

        // Mock: Appel API Orange Money/Wave
        boolean paymentSuccess = mockPaymentProvider(payment);

        if (paymentSuccess) {
            // Marquer comme réussi
            String refExterne = "OM-" + System.currentTimeMillis();
            payment.markAsSuccess(refExterne);
            payment = paymentRepository.save(payment);

            // Créer l'abonnement
            Subscription subscription = subscriptionService.createSubscription(patient, plan);
            payment.setSubscription(subscription);
            payment = paymentRepository.save(payment);

            log.info("✅ Paiement réussi - Abonnement créé: {}", subscription.getId());
        } else {
            payment.markAsFailed("Échec du paiement mobile");
            payment = paymentRepository.save(payment);

            log.error("❌ Paiement échoué: {}", payment.getReferenceInterne());
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
                payment.getPaidAt(),
                payment.getCreatedAt()
        );
    }
}