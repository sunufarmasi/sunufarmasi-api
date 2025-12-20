package sn.sunufarmasi.stock.entity;

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
 * Entité représentant le stock d'un produit dans une pharmacie spécifique
 *
 * Chaque pharmacie peut avoir des prix, quantités et seuils différents
 * pour un même produit du catalogue national.
 *
 * @author AL Amine
 * @since 1.0.0
 */
@Entity
@Table(name = "produits_pharmacie", 
        uniqueConstraints = @UniqueConstraint(columnNames = {"pharmacie_id", "produit_id"}),
        indexes = {
                @Index(name = "idx_pp_pharmacie", columnList = "pharmacie_id"),
                @Index(name = "idx_pp_produit", columnList = "produit_id"),
                @Index(name = "idx_pp_quantite", columnList = "quantite_stock"),
                @Index(name = "idx_pp_peremption", columnList = "date_peremption_proche"),
                @Index(name = "idx_pp_emplacement", columnList = "emplacement")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ProduitPharmacie {

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
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    // ═══════════════════════════════════════════════════════════
    // STOCK
    // ═══════════════════════════════════════════════════════════

    @Column(name = "quantite_stock", nullable = false)
    private Integer quantiteStock = 0;

    @Column(name = "quantite_reservee")
    private Integer quantiteReservee = 0;         // Réservée pour commandes en cours

    @Column(name = "seuil_alerte")
    private Integer seuilAlerte = 10;             // Seuil bas pour alerte

    @Column(name = "seuil_reappro")
    private Integer seuilReappro = 20;            // Seuil de réapprovisionnement

    @Column(name = "quantite_optimale")
    private Integer quantiteOptimale;             // Stock optimal à maintenir

    // ═══════════════════════════════════════════════════════════
    // PÉREMPTION
    // ═══════════════════════════════════════════════════════════

    @Column(name = "date_peremption_proche")
    private LocalDate datePeremptionProche;       // Date de péremption la plus proche

    @Column(name = "alerte_peremption_jours")
    private Integer alertePeremptionJours = 90;   // Alerter X jours avant péremption

    // ═══════════════════════════════════════════════════════════
    // PRIX PHARMACIE (peuvent différer du catalogue)
    // ═══════════════════════════════════════════════════════════

    @Column(name = "prix_vente_ttc", precision = 12, scale = 2)
    private BigDecimal prixVenteTTC;              // Prix de vente TTC

    @Column(name = "prix_achat_ht", precision = 12, scale = 2)
    private BigDecimal prixAchatHT;               // Dernier prix d'achat HT

    @Column(name = "marge_pourcentage", precision = 5, scale = 2)
    private BigDecimal margePourcentage;          // Marge appliquée

    // ═══════════════════════════════════════════════════════════
    // EMPLACEMENT
    // ═══════════════════════════════════════════════════════════

    @Column(length = 50)
    private String emplacement;                   // Ex: "Rayon A - Étagère 3"

    @Column(length = 20)
    private String rayon;

    @Column(length = 20)
    private String etagere;

    // ═══════════════════════════════════════════════════════════
    // PARAMÈTRES
    // ═══════════════════════════════════════════════════════════

    @Column(name = "est_actif")
    private Boolean estActif = true;              // Produit actif dans cette pharmacie

    @Column(name = "vente_autorisee")
    private Boolean venteAutorisee = true;        // Autorise la vente

    @Column(name = "commande_auto")
    private Boolean commandeAuto = false;         // Réapprovisionnement automatique

    // ═══════════════════════════════════════════════════════════
    // STATISTIQUES
    // ═══════════════════════════════════════════════════════════

    @Column(name = "total_vendu")
    private Integer totalVendu = 0;               // Total vendu (historique)

    @Column(name = "derniere_vente")
    private LocalDateTime derniereVente;

    @Column(name = "derniere_entree")
    private LocalDateTime derniereEntree;

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
     * Calcule la quantité disponible (stock - réservé)
     */
    public Integer getQuantiteDisponible() {
        int reserve = this.quantiteReservee != null ? this.quantiteReservee : 0;
        return Math.max(0, this.quantiteStock - reserve);
    }

    /**
     * Vérifie si le stock est bas (sous le seuil d'alerte)
     */
    public boolean stockBas() {
        return this.quantiteStock != null && this.seuilAlerte != null
                && this.quantiteStock <= this.seuilAlerte;
    }

    /**
     * Vérifie si le stock nécessite réapprovisionnement
     */
    public boolean aReapprovisionner() {
        return this.quantiteStock != null && this.seuilReappro != null
                && this.quantiteStock <= this.seuilReappro;
    }

    /**
     * Vérifie si le produit est bientôt périmé
     */
    public boolean bientotPerime() {
        if (this.datePeremptionProche == null || this.alertePeremptionJours == null) {
            return false;
        }
        LocalDate dateAlerte = LocalDate.now().plusDays(this.alertePeremptionJours);
        return this.datePeremptionProche.isBefore(dateAlerte);
    }

    /**
     * Vérifie si le produit est périmé
     */
    public boolean estPerime() {
        return this.datePeremptionProche != null && this.datePeremptionProche.isBefore(LocalDate.now());
    }

    /**
     * Vérifie si le produit est en rupture
     */
    public boolean enRupture() {
        return this.quantiteStock == null || this.quantiteStock <= 0;
    }

    /**
     * Vérifie si le produit peut être vendu
     */
    public boolean peutEtreVendu() {
        return this.estActif && this.venteAutorisee && !enRupture() && !estPerime();
    }

    /**
     * Ajouter du stock
     */
    public void ajouterStock(int quantite) {
        this.quantiteStock = (this.quantiteStock != null ? this.quantiteStock : 0) + quantite;
        this.derniereEntree = LocalDateTime.now();
    }

    /**
     * Retirer du stock
     */
    public boolean retirerStock(int quantite) {
        if (this.quantiteStock == null || this.quantiteStock < quantite) {
            return false;
        }
        this.quantiteStock -= quantite;
        this.totalVendu = (this.totalVendu != null ? this.totalVendu : 0) + quantite;
        this.derniereVente = LocalDateTime.now();
        return true;
    }

    /**
     * Réserver du stock
     */
    public boolean reserverStock(int quantite) {
        if (getQuantiteDisponible() < quantite) {
            return false;
        }
        this.quantiteReservee = (this.quantiteReservee != null ? this.quantiteReservee : 0) + quantite;
        return true;
    }

    /**
     * Libérer une réservation
     */
    public void libererReservation(int quantite) {
        this.quantiteReservee = Math.max(0,
                (this.quantiteReservee != null ? this.quantiteReservee : 0) - quantite);
    }

    /**
     * Calcule le montant en stock
     */
    public BigDecimal getValeurStock() {
        if (this.prixAchatHT == null || this.quantiteStock == null) {
            return BigDecimal.ZERO;
        }
        return this.prixAchatHT.multiply(BigDecimal.valueOf(this.quantiteStock));
    }

    /**
     * Calcule la marge unitaire
     */
    public BigDecimal getMargeUnitaire() {
        if (this.prixVenteTTC == null || this.prixAchatHT == null) {
            return BigDecimal.ZERO;
        }
        return this.prixVenteTTC.subtract(this.prixAchatHT);
    }

    /**
     * Retourne l'emplacement complet
     */
    public String getEmplacementComplet() {
        if (this.emplacement != null) {
            return this.emplacement;
        }
        if (this.rayon != null || this.etagere != null) {
            return (this.rayon != null ? this.rayon : "") +
                    (this.etagere != null ? " - " + this.etagere : "");
        }
        return null;
    }

    @PrePersist
    protected void onCreate() {
        if (this.quantiteStock == null) {
            this.quantiteStock = 0;
        }
        if (this.quantiteReservee == null) {
            this.quantiteReservee = 0;
        }
        if (this.estActif == null) {
            this.estActif = true;
        }
        if (this.venteAutorisee == null) {
            this.venteAutorisee = true;
        }
        if (this.totalVendu == null) {
            this.totalVendu = 0;
        }
    }
}
