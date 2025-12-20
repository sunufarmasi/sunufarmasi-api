package sn.sunufarmasi.subscription.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sn.sunufarmasi.patient.entity.Patient;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity représentant un abonnement actif d'un patient
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(
        name = "subscriptions",
        indexes = {
                @Index(name = "idx_subscriptions_patient", columnList = "patient_id"),
                @Index(name = "idx_subscriptions_expires_at", columnList = "expires_at"),
                @Index(name = "idx_subscriptions_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Patient propriétaire de l'abonnement
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    /**
     * Plan d'abonnement
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    /**
     * Statut de l'abonnement
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    /**
     * Date de début
     */
    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    /**
     * Date d'expiration
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Renouvellement automatique
     */
    @Column(name = "auto_renew", nullable = false)
    @Builder.Default
    private boolean autoRenew = false;

    /**
     * C'est une période d'essai
     */
    @Column(name = "is_trial", nullable = false)
    @Builder.Default
    private boolean isTrial = false;

    /**
     * Date d'annulation
     */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    /**
     * Raison d'annulation
     */
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    /**
     * Date de création
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date de mise à jour
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Vérifier si l'abonnement est actif
     */
    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE &&
                expiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * Vérifier si l'abonnement est expiré
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Vérifier si l'abonnement expire bientôt (dans 3 jours)
     */
    public boolean isExpiringSoon() {
        LocalDateTime threeDaysFromNow = LocalDateTime.now().plusDays(3);
        return expiresAt.isBefore(threeDaysFromNow) && !isExpired();
    }

    /**
     * Obtenir le nombre de jours restants
     */
    public long getDaysRemaining() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).toDays();
    }

    /**
     * Annuler l'abonnement
     */
    public void cancel(String reason) {
        this.status = SubscriptionStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
        this.autoRenew = false;
    }

    /**
     * Renouveler l'abonnement
     */
    public void renew(int daysToAdd) {
        this.expiresAt = this.expiresAt.plusDays(daysToAdd);
        this.status = SubscriptionStatus.ACTIVE;
    }

    /**
     * Marquer comme expiré
     */
    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + id +
                ", status=" + status +
                ", startsAt=" + startsAt +
                ", expiresAt=" + expiresAt +
                ", isTrial=" + isTrial +
                '}';
    }
}