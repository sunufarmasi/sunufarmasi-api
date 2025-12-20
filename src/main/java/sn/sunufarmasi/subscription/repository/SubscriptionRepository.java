package sn.sunufarmasi.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.subscription.entity.Subscription;
import sn.sunufarmasi.subscription.entity.SubscriptionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour Subscription
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    /**
     * Trouver l'abonnement actif d'un patient
     */
    @Query("""
        SELECT s FROM Subscription s 
        WHERE s.patient.id = :patientId 
        AND s.status = 'ACTIVE' 
        AND s.expiresAt > :now
        ORDER BY s.expiresAt DESC
        LIMIT 1
        """)
    Optional<Subscription> findActiveByPatientId(
            @Param("patientId") UUID patientId,
            @Param("now") LocalDateTime now
    );

    /**
     * Trouver tous les abonnements d'un patient
     */
    List<Subscription> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    /**
     * Trouver un abonnement par patient et statut
     * NÉCESSAIRE pour seedSubscriptions
     */
    Optional<Subscription> findByPatientIdAndStatus(UUID patientId, SubscriptionStatus status);

    /**
     * Trouver les abonnements qui expirent bientôt
     */
    @Query("""
        SELECT s FROM Subscription s 
        WHERE s.status = 'ACTIVE' 
        AND s.expiresAt BETWEEN :now AND :threshold
        """)
    List<Subscription> findExpiringSoon(
            @Param("now") LocalDateTime now,
            @Param("threshold") LocalDateTime threshold
    );

    /**
     * Trouver les essais gratuits expirés
     */
    List<Subscription> findByIsTrialTrueAndExpiresAtBeforeAndStatus(
            LocalDateTime expirationDate,
            SubscriptionStatus status
    );

    /**
     * Trouver les abonnements expirés
     */
    @Query("""
        SELECT s FROM Subscription s 
        WHERE s.status = 'ACTIVE' 
        AND s.expiresAt < :now
        """)
    List<Subscription> findExpired(@Param("now") LocalDateTime now);

    /**
     * Vérifier si un patient a déjà utilisé son essai gratuit
     */
    boolean existsByPatientIdAndIsTrialTrue(UUID patientId);
}