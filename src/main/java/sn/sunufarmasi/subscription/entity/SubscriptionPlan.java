package sn.sunufarmasi.subscription.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity représentant un plan d'abonnement (plan tarifaire)
 *
 * Plans disponibles:
 * - FREE_TRIAL: Période d'essai 15 jours (gratuit)
 * - MONTHLY: Abonnement mensuel 750 FCFA (avec publicités)
 * - ANNUAL: Abonnement annuel 8000 FCFA (sans publicités)
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Code unique du plan (FREE_TRIAL, MONTHLY, ANNUAL)
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Nom du plan
     */
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    /**
     * Description du plan
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Prix en FCFA
     */
    @Column(name = "prix", nullable = false)
    private Integer prix;

    /**
     * Durée en jours
     */
    @Column(name = "duree_jours", nullable = false)
    private Integer dureeJours;

    /**
     * Afficher des publicités
     */
    @Column(name = "avec_publicite", nullable = false)
    @Builder.Default
    private boolean avecPublicite = false;

    /**
     * Plan actif (peut être souscrit)
     */
    @Column(name = "actif", nullable = false)
    @Builder.Default
    private boolean actif = true;

    /**
     * Ordre d'affichage
     */
    @Column(name = "ordre")
    private Integer ordre;

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
     * Vérifier si c'est un essai gratuit
     */
    public boolean isFreeTrial() {
        return "FREE_TRIAL".equals(code);
    }

    /**
     * Vérifier si c'est un plan mensuel
     */
    public boolean isMonthly() {
        return "MONTHLY".equals(code);
    }

    /**
     * Vérifier si c'est un plan annuel
     */
    public boolean isAnnual() {
        return "ANNUAL".equals(code);
    }

    /**
     * Calculer le prix par jour
     */
    public double getPrixParJour() {
        if (dureeJours == null || dureeJours == 0) {
            return 0;
        }
        return (double) prix / dureeJours;
    }

    @Override
    public String toString() {
        return "SubscriptionPlan{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", nom='" + nom + '\'' +
                ", prix=" + prix +
                ", dureeJours=" + dureeJours +
                '}';
    }
}