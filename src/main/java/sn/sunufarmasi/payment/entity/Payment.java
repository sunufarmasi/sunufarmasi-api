package sn.sunufarmasi.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sn.sunufarmasi.patient.entity.Patient;
import sn.sunufarmasi.subscription.entity.Subscription;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity représentant un paiement
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payments_patient", columnList = "patient_id"),
                @Index(name = "idx_payments_subscription", columnList = "subscription_id"),
                @Index(name = "idx_payments_reference", columnList = "reference_externe"),
                @Index(name = "idx_payments_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Patient qui effectue le paiement
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    /**
     * Abonnement associé
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    /**
     * Montant en FCFA
     */
    @Column(name = "montant", nullable = false)
    private Integer montant;

    /**
     * Méthode de paiement
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "methode", nullable = false, length = 50)
    private PaymentMethod methode;

    /**
     * Statut du paiement
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * Numéro de téléphone pour paiement mobile
     */
    @Column(name = "telephone_paiement", length = 20)
    private String telephonePaiement;

    /**
     * Référence externe (de Orange Money/Wave)
     */
    @Column(name = "reference_externe", length = 100)
    private String referenceExterne;

    /**
     * ID de la session Wave Checkout (cos_...)
     */
    @Column(name = "wave_checkout_id", length = 100)
    private String waveCheckoutId;

    /**
     * URL de paiement Wave (wave_launch_url)
     */
    @Column(name = "wave_checkout_url", length = 500)
    private String waveCheckoutUrl;

    /**
     * Identifiant du plan d'abonnement choisi
     */
    @Column(name = "plan_id", length = 100)
    private String planId;

    /**
     * Référence interne (notre système)
     */
    @Column(name = "reference_interne", nullable = false, unique = true, length = 50)
    private String referenceInterne;

    /**
     * Message d'erreur (si échec)
     */
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /**
     * Date de paiement réussi
     */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

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
     * Vérifier si le paiement est réussi
     */
    public boolean isSuccess() {
        return status == PaymentStatus.SUCCESS;
    }

    /**
     * Vérifier si le paiement a échoué
     */
    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }

    /**
     * Vérifier si le paiement est en attente
     */
    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    /**
     * Marquer comme réussi
     */
    public void markAsSuccess(String referenceExterne) {
        this.status = PaymentStatus.SUCCESS;
        this.referenceExterne = referenceExterne;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * Marquer comme échoué
     */
    public void markAsFailed(String errorMessage) {
        this.status = PaymentStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    /**
     * Générer une référence interne unique
     */
    public static String generateReferenceInterne() {
        return "PAY-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", montant=" + montant +
                ", methode=" + methode +
                ", status=" + status +
                ", referenceInterne='" + referenceInterne + '\'' +
                '}';
    }
}