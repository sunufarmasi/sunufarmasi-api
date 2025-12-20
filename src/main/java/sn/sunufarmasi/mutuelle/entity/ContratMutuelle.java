package sn.sunufarmasi.mutuelle.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sn.sunufarmasi.pharmacie.entity.Pharmacie;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant un contrat/convention entre une pharmacie et une mutuelle
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(name = "contrats_mutuelle", indexes = {
        @Index(name = "idx_contrat_pharmacie", columnList = "pharmacie_id"),
        @Index(name = "idx_contrat_mutuelle", columnList = "mutuelle_id"),
        @Index(name = "idx_contrat_actif", columnList = "est_actif"),
        @Index(name = "idx_contrat_dates", columnList = "date_debut, date_fin")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_contrat_pharmacie_mutuelle",
                columnNames = {"pharmacie_id", "mutuelle_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ContratMutuelle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ═══════════════════════════════════════════════════════════
    // RELATIONS
    // ═══════════════════════════════════════════════════════════

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacie_id", nullable = false)
    private Pharmacie pharmacie;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mutuelle_id", nullable = false)
    private Mutuelle mutuelle;

    // ═══════════════════════════════════════════════════════════
    // IDENTIFICATION CONTRAT
    // ═══════════════════════════════════════════════════════════

    @Column(name = "numero_contrat", length = 50)
    private String numeroContrat;

    @Column(name = "numero_conventionnement", length = 50)
    private String numeroConventionnement;        // Numéro attribué par la mutuelle

    // ═══════════════════════════════════════════════════════════
    // VALIDITÉ
    // ═══════════════════════════════════════════════════════════

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "renouvellement_auto")
    private Boolean renouvellementAuto = true;

    // ═══════════════════════════════════════════════════════════
    // CONDITIONS SPÉCIFIQUES
    // ═══════════════════════════════════════════════════════════

    @Column(name = "taux_couverture", precision = 5, scale = 2)
    private BigDecimal tauxCouverture;            // Taux négocié (peut différer du défaut)

    @Column(name = "plafond_mensuel", precision = 12, scale = 2)
    private BigDecimal plafondMensuel;

    @Column(name = "plafond_annuel", precision = 12, scale = 2)
    private BigDecimal plafondAnnuel;

    @Column(name = "delai_paiement_jours")
    private Integer delaiPaiementJours;

    // ═══════════════════════════════════════════════════════════
    // MODALITÉS DE FACTURATION
    // ═══════════════════════════════════════════════════════════

    @Column(name = "frequence_facturation", length = 20)
    private String frequenceFacturation;          // QUOTIDIEN, HEBDOMADAIRE, MENSUEL

    @Column(name = "jour_facturation")
    private Integer jourFacturation;              // Jour du mois pour envoyer les factures

    @Column(name = "email_facturation", length = 100)
    private String emailFacturation;

    // ═══════════════════════════════════════════════════════════
    // TIERS PAYANT
    // ═══════════════════════════════════════════════════════════

    @Column(name = "tiers_payant_autorise")
    private Boolean tiersPayantAutorise = true;   // Dispense d'avance de frais

    @Column(name = "montant_avance_max", precision = 12, scale = 2)
    private BigDecimal montantAvanceMax;          // Montant max en tiers payant

    // ═══════════════════════════════════════════════════════════
    // STATUT
    // ═══════════════════════════════════════════════════════════

    @Column(name = "est_actif")
    private Boolean estActif = true;

    @Column(name = "motif_suspension", length = 500)
    private String motifSuspension;

    @Column(name = "date_suspension")
    private LocalDate dateSuspension;

    // ═══════════════════════════════════════════════════════════
    // NOTES
    // ═══════════════════════════════════════════════════════════

    @Column(length = 1000)
    private String notes;

    // ═══════════════════════════════════════════════════════════
    // MÉTADONNÉES
    // ═══════════════════════════════════════════════════════════

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Vérifie si le contrat est valide à une date donnée
     */
    public boolean estValide(LocalDate date) {
        if (!estActif) return false;
        if (date.isBefore(dateDebut)) return false;
        if (dateFin != null && date.isAfter(dateFin)) return false;
        return true;
    }

    /**
     * Vérifie si le contrat est valide aujourd'hui
     */
    public boolean estValide() {
        return estValide(LocalDate.now());
    }

    /**
     * Retourne le taux effectif (négocié ou par défaut)
     */
    public BigDecimal getTauxEffectif() {
        if (tauxCouverture != null) {
            return tauxCouverture;
        }
        return mutuelle != null ? mutuelle.getTauxCouvertureDefaut() : BigDecimal.ZERO;
    }

    /**
     * Vérifie si le tiers payant est possible
     */
    public boolean tiersPayantPossible(BigDecimal montant) {
        if (!tiersPayantAutorise) return false;
        if (montantAvanceMax != null && montant.compareTo(montantAvanceMax) > 0) return false;
        return true;
    }

    @PrePersist
    protected void onCreate() {
        if (estActif == null) estActif = true;
        if (tiersPayantAutorise == null) tiersPayantAutorise = true;
        if (renouvellementAuto == null) renouvellementAuto = true;
    }
}
