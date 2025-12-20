package sn.sunufarmasi.vente.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sn.sunufarmasi.stock.entity.Produit;
import sn.sunufarmasi.stock.entity.ProduitPharmacie;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant une ligne de vente (détail produit)
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(name = "lignes_vente", indexes = {
        @Index(name = "idx_lv_vente", columnList = "vente_id"),
        @Index(name = "idx_lv_produit", columnList = "produit_id"),
        @Index(name = "idx_lv_produit_pharmacie", columnList = "produit_pharmacie_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class LigneVente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ═══════════════════════════════════════════════════════════
    // VENTE PARENTE
    // ═══════════════════════════════════════════════════════════

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vente_id", nullable = false)
    private Vente vente;

    @Column(name = "numero_ligne")
    private Integer numeroLigne;

    // ═══════════════════════════════════════════════════════════
    // PRODUIT
    // ═══════════════════════════════════════════════════════════

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_pharmacie_id")
    private ProduitPharmacie produitPharmacie;

    // Snapshot des infos produit au moment de la vente
    @Column(name = "produit_code", length = 50)
    private String produitCode;

    @Column(name = "produit_nom", length = 255)
    private String produitNom;

    @Column(name = "produit_dci", length = 255)
    private String produitDci;

    // ═══════════════════════════════════════════════════════════
    // QUANTITÉ
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false)
    private Integer quantite;

    @Column(name = "quantite_gratuite")
    private Integer quantiteGratuite = 0;          // Échantillons, promotions

    // ═══════════════════════════════════════════════════════════
    // PRIX
    // ═══════════════════════════════════════════════════════════

    @Column(name = "prix_unitaire_ht", precision = 12, scale = 2)
    private BigDecimal prixUnitaireHT;

    @Column(name = "prix_unitaire_ttc", precision = 12, scale = 2, nullable = false)
    private BigDecimal prixUnitaireTTC;

    @Column(name = "taux_tva", precision = 5, scale = 2)
    private BigDecimal tauxTVA;

    // ═══════════════════════════════════════════════════════════
    // REMISE LIGNE
    // ═══════════════════════════════════════════════════════════

    @Column(name = "remise_pourcentage", precision = 5, scale = 2)
    private BigDecimal remisePourcentage;

    @Column(name = "remise_montant", precision = 12, scale = 2)
    private BigDecimal remiseMontant = BigDecimal.ZERO;

    // ═══════════════════════════════════════════════════════════
    // TOTAUX
    // ═══════════════════════════════════════════════════════════

    @Column(name = "montant_ht", precision = 12, scale = 2)
    private BigDecimal montantHT;

    @Column(name = "montant_tva", precision = 12, scale = 2)
    private BigDecimal montantTVA;

    @Column(name = "montant_ttc", precision = 12, scale = 2, nullable = false)
    private BigDecimal montantTTC;

    // ═══════════════════════════════════════════════════════════
    // LOT ET PÉREMPTION
    // ═══════════════════════════════════════════════════════════

    @Column(name = "numero_lot", length = 50)
    private String numeroLot;

    @Column(name = "date_peremption")
    private java.time.LocalDate datePeremption;

    // ═══════════════════════════════════════════════════════════
    // ORDONNANCE
    // ═══════════════════════════════════════════════════════════

    @Column(name = "sur_ordonnance")
    private Boolean surOrdonnance = false;

    @Column(name = "posologie", length = 500)
    private String posologie;

    // ═══════════════════════════════════════════════════════════
    // REMBOURSEMENT
    // ═══════════════════════════════════════════════════════════

    @Column(name = "est_remboursable")
    private Boolean estRemboursable = false;

    @Column(name = "taux_remboursement", precision = 5, scale = 2)
    private BigDecimal tauxRemboursement;

    @Column(name = "montant_remboursable", precision = 12, scale = 2)
    private BigDecimal montantRemboursable;

    // ═══════════════════════════════════════════════════════════
    // MÉTADONNÉES
    // ═══════════════════════════════════════════════════════════

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Calculer les montants de la ligne
     */
    public void calculerMontants() {
        if (this.prixUnitaireTTC == null || this.quantite == null) {
            return;
        }

        // Quantité facturée (hors gratuits)
        int qteFacturee = this.quantite - (this.quantiteGratuite != null ? this.quantiteGratuite : 0);

        // Montant brut TTC
        BigDecimal brut = this.prixUnitaireTTC.multiply(BigDecimal.valueOf(qteFacturee));

        // Appliquer la remise
        if (this.remisePourcentage != null && this.remisePourcentage.compareTo(BigDecimal.ZERO) > 0) {
            this.remiseMontant = brut
                    .multiply(this.remisePourcentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        // Montant TTC final
        this.montantTTC = brut.subtract(this.remiseMontant != null ? this.remiseMontant : BigDecimal.ZERO);

        // Calculer HT et TVA si taux TVA fourni
        if (this.tauxTVA != null && this.tauxTVA.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diviseur = BigDecimal.ONE.add(this.tauxTVA.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            this.montantHT = this.montantTTC.divide(diviseur, 2, RoundingMode.HALF_UP);
            this.montantTVA = this.montantTTC.subtract(this.montantHT);
        } else {
            this.montantHT = this.montantTTC;
            this.montantTVA = BigDecimal.ZERO;
        }

        // Calculer le montant remboursable
        if (this.estRemboursable && this.tauxRemboursement != null) {
            this.montantRemboursable = this.montantTTC
                    .multiply(this.tauxRemboursement)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Appliquer une remise en pourcentage
     */
    public void appliquerRemise(BigDecimal pourcentage) {
        this.remisePourcentage = pourcentage;
        calculerMontants();
    }

    /**
     * Initialiser depuis un produit pharmacie
     */
    public void initialiserDepuisProduitPharmacie(ProduitPharmacie pp) {
        this.produit = pp.getProduit();
        this.produitPharmacie = pp;
        this.produitCode = pp.getProduit().getCode();
        this.produitNom = pp.getProduit().getNomComplet();
        this.produitDci = pp.getProduit().getDci();
        this.prixUnitaireTTC = pp.getPrixVenteTTC();
        this.tauxTVA = pp.getProduit().getTauxTVA();
        this.surOrdonnance = pp.getProduit().getSurOrdonnance();
        this.estRemboursable = pp.getProduit().getEstRemboursable();
        this.tauxRemboursement = pp.getProduit().getTauxRemboursement();

        // Calculer prix HT
        if (this.tauxTVA != null && this.tauxTVA.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diviseur = BigDecimal.ONE.add(this.tauxTVA.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            this.prixUnitaireHT = this.prixUnitaireTTC.divide(diviseur, 2, RoundingMode.HALF_UP);
        } else {
            this.prixUnitaireHT = this.prixUnitaireTTC;
        }
    }

    /**
     * Quantité totale (vendue + gratuite)
     */
    public int getQuantiteTotale() {
        return (this.quantite != null ? this.quantite : 0) +
                (this.quantiteGratuite != null ? this.quantiteGratuite : 0);
    }

    @PrePersist
    protected void onCreate() {
        if (this.quantiteGratuite == null) this.quantiteGratuite = 0;
        if (this.remiseMontant == null) this.remiseMontant = BigDecimal.ZERO;
        if (this.surOrdonnance == null) this.surOrdonnance = false;
        if (this.estRemboursable == null) this.estRemboursable = false;
        calculerMontants();
    }

    @PreUpdate
    protected void onUpdate() {
        calculerMontants();
    }
}
