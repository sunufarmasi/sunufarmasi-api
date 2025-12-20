package sn.sunufarmasi.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity représentant un code OTP (One-Time Password)
 * Utilisé pour vérifier le téléphone lors de l'inscription/login
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(
        name = "otp_codes",
        indexes = {
                @Index(name = "idx_otp_telephone", columnList = "telephone"),
                @Index(name = "idx_otp_code", columnList = "code"),
                @Index(name = "idx_otp_expires_at", columnList = "expires_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Numéro de téléphone destinataire
     */
    @Column(name = "telephone", nullable = false, length = 20)
    private String telephone;

    /**
     * Code OTP (6 chiffres)
     */
    @Column(name = "code", nullable = false, length = 6)
    private String code;

    /**
     * Type d'OTP (REGISTRATION, LOGIN, PASSWORD_RESET)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private OtpType type;

    /**
     * Nombre de tentatives de vérification
     */
    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private int attempts = 0;

    /**
     * Code vérifié avec succès
     */
    @Column(name = "verified", nullable = false)
    @Builder.Default
    private boolean verified = false;

    /**
     * Date d'expiration (5 minutes après création)
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Date de création
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Vérifier si le code est expiré
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Vérifier si le nombre maximum de tentatives est atteint
     */
    public boolean isMaxAttemptsReached() {
        return attempts >= 3;
    }

    /**
     * Incrémenter le nombre de tentatives
     */
    public void incrementAttempts() {
        this.attempts++;
    }

    /**
     * Marquer comme vérifié
     */
    public void markAsVerified() {
        this.verified = true;
    }

    /**
     * Vérifier si le code peut être utilisé
     */
    public boolean canBeUsed() {
        return !isExpired() && !verified && !isMaxAttemptsReached();
    }

    @Override
    public String toString() {
        return "OtpCode{" +
                "id=" + id +
                ", telephone='" + telephone + '\'' +
                ", type=" + type +
                ", verified=" + verified +
                ", expired=" + isExpired() +
                '}';
    }
}