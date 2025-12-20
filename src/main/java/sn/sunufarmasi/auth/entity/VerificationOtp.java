package sn.sunufarmasi.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity pour stocker les codes OTP (One-Time Password) de vérification
 * Utilisé pour vérifier les téléphones lors de l'inscription
 * 
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(
    name = "verification_otp",
    indexes = {
        @Index(name = "idx_otp_email", columnList = "email"),
        @Index(name = "idx_otp_telephone", columnList = "telephone"),
        @Index(name = "idx_otp_expiration", columnList = "expiration_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationOtp {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    /**
     * Email de l'utilisateur
     */
    @Column(name = "email", nullable = false, length = 255)
    private String email;
    
    /**
     * Numéro de téléphone (format international)
     */
    @Column(name = "telephone", nullable = false, length = 20)
    private String telephone;
    
    /**
     * Code OTP (6 chiffres)
     */
    @Column(name = "code_otp", nullable = false, length = 10)
    private String codeOtp;
    
    /**
     * Type d'utilisateur (PATIENT, PHARMACIEN, etc.)
     */
    @Column(name = "user_type", nullable = false, length = 20)
    private String userType;
    
    /**
     * Nombre de tentatives de vérification
     */
    @Column(name = "tentatives", nullable = false)
    @Builder.Default
    private int tentatives = 0;
    
    /**
     * OTP vérifié avec succès
     */
    @Column(name = "verifie", nullable = false)
    @Builder.Default
    private boolean verifie = false;
    
    /**
     * Date d'expiration du code OTP
     */
    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;
    
    /**
     * Date de création
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Date de la dernière tentative
     */
    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;
    
    /**
     * Date de vérification
     */
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Vérifier si le code OTP est expiré
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationDate);
    }
    
    /**
     * Vérifier si le nombre maximum de tentatives est atteint
     */
    public boolean hasReachedMaxAttempts() {
        return tentatives >= 3; // Max 3 tentatives
    }
    
    /**
     * Incrémenter le nombre de tentatives
     */
    public void incrementTentatives() {
        this.tentatives++;
        this.lastAttemptAt = LocalDateTime.now();
    }
    
    /**
     * Marquer comme vérifié
     */
    public void markAsVerified() {
        this.verifie = true;
        this.verifiedAt = LocalDateTime.now();
    }
    
    /**
     * Vérifier si l'OTP est utilisable (pas expiré, pas max tentatives)
     */
    public boolean isUsable() {
        return !isExpired() && !hasReachedMaxAttempts() && !verifie;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VerificationOtp that)) return false;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "VerificationOtp{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", userType='" + userType + '\'' +
                ", verifie=" + verifie +
                ", tentatives=" + tentatives +
                ", expired=" + isExpired() +
                '}';
    }
}
