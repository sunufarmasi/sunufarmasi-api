package sn.sunufarmasi.stock.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sn.sunufarmasi.stock.enums.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant un produit dans le catalogue national
 *
 * Le catalogue est partagé entre toutes les pharmacies.
 * Chaque pharmacie a ensuite son propre stock (ProduitPharmacie).
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(name = "produits", indexes = {
        @Index(name = "idx_produit_code", columnList = "code"),
        @Index(name = "idx_produit_code_barre", columnList = "code_barre"),
        @Index(name = "idx_produit_dci", columnList = "dci"),
        @Index(name = "idx_produit_nom", columnList = "nom"),
        @Index(name = "idx_produit_categorie", columnList = "categorie"),
        @Index(name = "idx_produit_forme", columnList = "forme"),
        @Index(name = "idx_produit_laboratoire", columnList = "laboratoire"),
        @Index(name = "idx_produit_statut", columnList = "statut")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ═══════════════════════════════════════════════════════════
    // IDENTIFICATION
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, unique = true, length = 50)
    private String code;                         // Code interne unique (PRD-XXXXX)

    @Column(name = "code_barre", unique = true, length = 50)
    private String codeBarre;                    // Code EAN/GTIN

    @Column(name = "code_cip", length = 20)
    private String codeCip;                      // Code CIP (France)

    @Column(name = "numero_amm", length = 50)
    private String numeroAmm;                    // Numéro d'autorisation de mise sur le marché

    // ═══════════════════════════════════════════════════════════
    // DÉNOMINATION
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, length = 255)
    private String nom;                          // Nom commercial

    @Column(length = 255)
    private String dci;                          // Dénomination Commune Internationale

    @Column(length = 500)
    private String description;

    // ═══════════════════════════════════════════════════════════
    // CLASSIFICATION
    // ═══════════════════════════════════════════════════════════

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategorieProduit categorie;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private FormeProduit forme;                  // Forme galénique

    @Column(length = 100)
    private String dosage;                       // Ex: "500mg", "1g/5ml"

    @Enumerated(EnumType.STRING)
    @Column(name = "unite_vente", length = 20)
    private UniteVente uniteVente;

    @Column(name = "contenance")
    private Integer contenance;                  // Nombre d'unités par boîte

    // ═══════════════════════════════════════════════════════════
    // FABRICANT
    // ═══════════════════════════════════════════════════════════

    @Column(length = 200)
    private String laboratoire;                  // Fabricant/Laboratoire

    @Column(name = "pays_origine", length = 100)
    private String paysOrigine;

    // ═══════════════════════════════════════════════════════════
    // PRIX DE RÉFÉRENCE
    // ═══════════════════════════════════════════════════════════

    @Column(name = "prix_public_ttc", precision = 12, scale = 2)
    private BigDecimal prixPublicTTC;            // Prix public conseillé TTC

    @Column(name = "prix_achat_ht", precision = 12, scale = 2)
    private BigDecimal prixAchatHT;              // Prix d'achat de référence HT

    @Column(name = "taux_tva", precision = 5, scale = 2)
    private BigDecimal tauxTVA;                  // Taux de TVA (0, 18, etc.)

    @Column(name = "taux_remboursement", precision = 5, scale = 2)
    private BigDecimal tauxRemboursement;        // Taux de remboursement mutuelle

    // ═══════════════════════════════════════════════════════════
    // RÉGLEMENTATION
    // ═══════════════════════════════════════════════════════════

    @Column(name = "sur_ordonnance")
    private Boolean surOrdonnance = false;

    @Column(name = "liste_medicament", length = 10)
    private String listeMedicament;              // Liste I, II, stupéfiants

    @Column(name = "est_generique")
    private Boolean estGenerique = false;

    @Column(name = "produit_reference_id")
    private UUID produitReferenceId;             // Si générique, référence au princeps

    @Column(name = "est_remboursable")
    private Boolean estRemboursable = false;

    // ═══════════════════════════════════════════════════════════
    // CONSERVATION
    // ═══════════════════════════════════════════════════════════

    @Column(name = "temperature_conservation", length = 50)
    private String temperatureConservation;      // Ex: "2-8°C", "< 25°C"

    @Column(name = "chaine_froid")
    private Boolean chaineFroid = false;         // Nécessite chaîne du froid

    @Column(name = "duree_conservation_mois")
    private Integer dureeConservationMois;       // Durée de conservation en mois

    // ═══════════════════════════════════════════════════════════
    // IMAGES ET DOCUMENTS
    // ═══════════════════════════════════════════════════════════

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "notice_url", length = 500)
    private String noticeUrl;

    // ═══════════════════════════════════════════════════════════
    // STATUT
    // ═══════════════════════════════════════════════════════════

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutProduit statut;


//    ================================

    // Dans Produit.java, ajouter ces champs :

    /**
     * Pharmacie qui a créé ce produit (null = catalogue national)
     * Si non null, ce produit n'est visible que par cette pharmacie
     */
    @Column(name = "pharmacie_creatrice_id")
    private UUID pharmacieCreatrice;

    /**
     * Produit visible par toutes les pharmacies
     * true = catalogue national
     * false = produit privé de la pharmacie créatrice
     */
    @Column(name = "est_public")
    @Builder.Default
    private Boolean estPublic = true;

    /**
     * Type de produit personnalisé
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_creation", length = 30)
    private TypeCreationProduit typeCreation;


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
     * Retourne le nom complet avec dosage et forme
     * Ex: "Doliprane 500mg Comprimé"
     */
    public String getNomComplet() {
        StringBuilder sb = new StringBuilder(this.nom);
        if (this.dosage != null && !this.dosage.isEmpty()) {
            sb.append(" ").append(this.dosage);
        }
        if (this.forme != null) {
            sb.append(" ").append(this.forme.getLibelle());
        }
        return sb.toString();
    }

    /**
     * Vérifie si le produit est actif et disponible
     */
    public boolean estDisponible() {
        return this.statut == StatutProduit.ACTIF;
    }

    /**
     * Vérifie si c'est un médicament (nécessite ordonnance ou liste)
     */
    public boolean estMedicament() {
        return this.categorie == CategorieProduit.MEDICAMENT ||
                this.categorie == CategorieProduit.GENERIQUE ||
                this.categorie == CategorieProduit.ORDONNANCE;
    }

    /**
     * Calcule le prix TTC à partir du prix HT
     */
    public BigDecimal calculerPrixTTC(BigDecimal prixHT) {
        if (prixHT == null || this.tauxTVA == null) {
            return prixHT;
        }
        BigDecimal tva = prixHT.multiply(this.tauxTVA).divide(BigDecimal.valueOf(100));
        return prixHT.add(tva);
    }

    @PrePersist
    protected void onCreate() {
        if (this.statut == null) {
            this.statut = StatutProduit.ACTIF;
        }
        if (this.surOrdonnance == null) {
            this.surOrdonnance = false;
        }
        if (this.estGenerique == null) {
            this.estGenerique = false;
        }
        if (this.estRemboursable == null) {
            this.estRemboursable = false;
        }
        if (this.chaineFroid == null) {
            this.chaineFroid = false;
        }
    }
}
