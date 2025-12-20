package sn.sunufarmasi.stock.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sn.sunufarmasi.pharmacie.entity.Pharmacie;
import sn.sunufarmasi.stock.enums.TypeMouvement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant un mouvement de stock (entrée ou sortie)
 *
 * Chaque mouvement est tracé pour garantir la traçabilité complète
 * du stock de la pharmacie.
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(name = "mouvements_stock", indexes = {
        @Index(name = "idx_mvt_pharmacie", columnList = "pharmacie_id"),
        @Index(name = "idx_mvt_produit", columnList = "produit_id"),
        @Index(name = "idx_mvt_produit_pharmacie", columnList = "produit_pharmacie_id"),
        @Index(name = "idx_mvt_type", columnList = "type"),
        @Index(name = "idx_mvt_date", columnList = "date_mouvement"),
        @Index(name = "idx_mvt_lot", columnList = "numero_lot"),
        @Index(name = "idx_mvt_reference", columnList = "reference_externe")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class MouvementStock {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_pharmacie_id")
    private ProduitPharmacie produitPharmacie;

    // ═══════════════════════════════════════════════════════════
    // TYPE ET QUANTITÉ
    // ═══════════════════════════════════════════════════════════

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeMouvement type;

    @Column(nullable = false)
    private Integer quantite;                     // Toujours positif

    @Column(name = "quantite_avant")
    private Integer quantiteAvant;                // Stock avant mouvement

    @Column(name = "quantite_apres")
    private Integer quantiteApres;                // Stock après mouvement

    // ═══════════════════════════════════════════════════════════
    // LOT ET PÉREMPTION
    // ═══════════════════════════════════════════════════════════

    @Column(name = "numero_lot", length = 50)
    private String numeroLot;

    @Column(name = "date_peremption")
    private LocalDate datePeremption;

    // ═══════════════════════════════════════════════════════════
    // PRIX
    // ═══════════════════════════════════════════════════════════

    @Column(name = "prix_unitaire", precision = 12, scale = 2)
    private BigDecimal prixUnitaire;              // Prix unitaire au moment du mouvement

    @Column(name = "montant_total", precision = 12, scale = 2)
    private BigDecimal montantTotal;              // Quantité × Prix unitaire

    // ═══════════════════════════════════════════════════════════
    // RÉFÉRENCES EXTERNES
    // ═══════════════════════════════════════════════════════════

    @Column(name = "reference_externe", length = 100)
    private String referenceExterne;              // N° facture, N° BL, N° vente, etc.

    @Column(name = "fournisseur", length = 200)
    private String fournisseur;                   // Nom du fournisseur (si achat)

    @Column(name = "client", length = 200)
    private String client;                        // Nom du client (si vente)

    @Column(name = "pharmacie_transfert_id")
    private UUID pharmacieTransfertId;            // Pharmacie origine/destination (si transfert)

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS COMPLÉMENTAIRES
    // ═══════════════════════════════════════════════════════════

    @Column(length = 500)
    private String motif;                         // Motif du mouvement

    @Column(length = 1000)
    private String notes;

    // ═══════════════════════════════════════════════════════════
    // TRAÇABILITÉ
    // ═══════════════════════════════════════════════════════════

    @Column(name = "date_mouvement", nullable = false)
    private LocalDateTime dateMouvement;

    @Column(name = "effectue_par_id")
    private UUID effectueParId;                   // ID de l'utilisateur

    @Column(name = "effectue_par_nom", length = 200)
    private String effectueParNom;                // Nom de l'utilisateur

    @Column(name = "effectue_par_role", length = 50)
    private String effectueParRole;               // PHARMACIEN, VENDEUR, etc.

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Vérifie si c'est une entrée de stock
     */
    public boolean isEntree() {
        return this.type != null && this.type.isEntree();
    }

    /**
     * Vérifie si c'est une sortie de stock
     */
    public boolean isSortie() {
        return this.type != null && this.type.isSortie();
    }

    /**
     * Calcule l'impact sur le stock
     */
    public int getImpact() {
        if (this.type == null || this.quantite == null) {
            return 0;
        }
        return this.type.calculerImpact(this.quantite);
    }

    /**
     * Calcule le montant total si non défini
     */
    public BigDecimal calculerMontantTotal() {
        if (this.prixUnitaire == null || this.quantite == null) {
            return BigDecimal.ZERO;
        }
        return this.prixUnitaire.multiply(BigDecimal.valueOf(this.quantite));
    }

    @PrePersist
    protected void onCreate() {
        if (this.dateMouvement == null) {
            this.dateMouvement = LocalDateTime.now();
        }
        if (this.montantTotal == null) {
            this.montantTotal = calculerMontantTotal();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BUILDER HELPERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Créer un mouvement d'entrée
     */
    public static MouvementStock creerEntree(
            Pharmacie pharmacie, Produit produit, ProduitPharmacie produitPharmacie,
            TypeMouvement type, int quantite, int stockAvant) {

        return MouvementStock.builder()
                .pharmacie(pharmacie)
                .produit(produit)
                .produitPharmacie(produitPharmacie)
                .type(type)
                .quantite(quantite)
                .quantiteAvant(stockAvant)
                .quantiteApres(stockAvant + quantite)
                .dateMouvement(LocalDateTime.now())
                .build();
    }

    /**
     * Créer un mouvement de sortie
     */
    public static MouvementStock creerSortie(
            Pharmacie pharmacie, Produit produit, ProduitPharmacie produitPharmacie,
            TypeMouvement type, int quantite, int stockAvant) {

        return MouvementStock.builder()
                .pharmacie(pharmacie)
                .produit(produit)
                .produitPharmacie(produitPharmacie)
                .type(type)
                .quantite(quantite)
                .quantiteAvant(stockAvant)
                .quantiteApres(stockAvant - quantite)
                .dateMouvement(LocalDateTime.now())
                .build();
    }
}
