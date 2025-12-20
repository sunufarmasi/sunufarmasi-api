package sn.sunufarmasi.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.payment.entity.Payment;
import sn.sunufarmasi.payment.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour Payment
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /**
     * Trouver un paiement par référence interne
     */
    Optional<Payment> findByReferenceInterne(String referenceInterne);

    /**
     * Trouver un paiement par référence externe
     */
    Optional<Payment> findByReferenceExterne(String referenceExterne);

    /**
     * Trouver tous les paiements d'un patient
     */
    List<Payment> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    /**
     * Trouver les paiements d'un abonnement
     */
    List<Payment> findBySubscriptionIdOrderByCreatedAtDesc(UUID subscriptionId);

    /**
     * Trouver les paiements en attente (timeout après 10 minutes)
     */
    @Query("""
        SELECT p FROM Payment p 
        WHERE p.status = 'PENDING' 
        AND p.createdAt < :timeout
        """)
    List<Payment> findPendingTimeout(@Param("timeout") LocalDateTime timeout);

    /**
     * Trouver les paiements réussis d'un patient
     */
    List<Payment> findByPatientIdAndStatusOrderByCreatedAtDesc(
            UUID patientId,
            PaymentStatus status
    );

    /**
     * Compter les paiements réussis d'un patient
     */
    long countByPatientIdAndStatus(UUID patientId, PaymentStatus status);
}