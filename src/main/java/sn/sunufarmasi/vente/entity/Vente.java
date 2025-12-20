package sn.sunufarmasi.vente.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sn.sunufarmasi.pharmacie.entity.Pharmacie;
import sn.sunufarmasi.vente.enums.ModePaiement;
import sn.sunufarmasi.vente.enums.StatutVente;
import sn.sunufarmasi.vente.enums.TypeVente;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entité représentant une vente (en-tête)
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(name = "ventes", indexes = {
        @Index(name = "idx_vente_numero", columnList = "numero"),
        @Index(name = "idx_vente_pharmacie", columnList = "pharmacie_id"),
        @Index(name = "idx_vente_date", columnList = "date_vente"),
        @Index(name = "idx_vente_statut", columnList = "statut"),
        @Index(name = "idx_vente_type", columnList = "type"),
        @Index(name = "idx_vente_client", columnList = "client_id"),
        @Index(name = "idx_vente_vendeur", columnList = "vendeur_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Vente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ═══════════════════════════════════════════════════════════
    // IDENTIFICATION
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, unique = true, length = 30)
    private String numero;                        // VTE-PHAR001-20231125-0001

    @Column(name = "numero_ticket", length = 20)
    private String numeroTicket;                  // Numéro de ticket de caisse

    // ═══════════════════════════════════════════════════════════
    // PHARMACIE
    // ═══════════════════════════════════════════════════════════

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacie_id", nullable = false)
    private Pharmacie pharmacie;

    // ═══════════════════════════════════════════════════════════
    // TYPE ET STATUT
    // ═══════════════════════════════════════════════════════════

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeVente type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private StatutVente statut;

    // ═══════════════════════════════════════════════════════════
    // CLIENT (optionnel)
    // ═══════════════════════════════════════════════════════════

    @Column(name = "client_id")
    private UUID clientId;                        // Référence au patient/client

    @Column(name = "client_nom", length = 200)
    private String clientNom;

    @Column(name = "client_telephone", length = 20)
    private String clientTelephone;

    // ═══════════════════════════════════════════════════════════
    // ORDONNANCE (si applicable)
    // ═══════════════════════════════════════════════════════════

    @Column(name = "numero_ordonnance", length = 50)
    private String numeroOrdonnance;

    @Column(name = "medecin_prescripteur", length = 200)
    private String medecinPrescripteur;

    @Column(name = "date_ordonnance")
    private LocalDateTime dateOrdonnance;

    // ═══════════════════════════════════════════════════════════
    // MUTUELLE / TIERS PAYANT
    // ═══════════════════════════════════════════════════════════

    @Column(name = "mutuelle_id")
    private UUID mutuelleId;

    @Column(name = "mutuelle_nom", length = 200)
    private String mutuelleNom;

    @Column(name = "numero_adherent", length = 50)
    private String numeroAdherent;

    @Column(name = "taux_prise_en_charge", precision = 5, scale = 2)
    private BigDecimal tauxPriseEnCharge;         // Ex: 80.00 pour 80%

    // ═══════════════════════════════════════════════════════════
    // MONTANTS
    // ═══════════════════════════════════════════════════════════

    @Column(name = "montant_brut_ht", precision = 12, scale = 2)
    private BigDecimal montantBrutHT = BigDecimal.ZERO;

    @Column(name = "montant_tva", precision = 12, scale = 2)
    private BigDecimal montantTVA = BigDecimal.ZERO;

    @Column(name = "montant_brut_ttc", precision = 12, scale = 2)
    private BigDecimal montantBrutTTC = BigDecimal.ZERO;

    @Column(name = "montant_remise", precision = 12, scale = 2)
    private BigDecimal montantRemise = BigDecimal.ZERO;

    @Column(name = "pourcentage_remise", precision = 5, scale = 2)
    private BigDecimal pourcentageRemise;

    @Column(name = "montant_net_ttc", precision = 12, scale = 2)
    private BigDecimal montantNetTTC = BigDecimal.ZERO;  // Après remise

    @Column(name = "montant_mutuelle", precision = 12, scale = 2)
    private BigDecimal montantMutuelle = BigDecimal.ZERO; // Part mutuelle

    @Column(name = "montant_client", precision = 12, scale = 2)
    private BigDecimal montantClient = BigDecimal.ZERO;   // Part client (reste à payer)

    // ═══════════════════════════════════════════════════════════
    // PAIEMENT
    // ═══════════════════════════════════════════════════════════

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement", length = 20)
    private ModePaiement modePaiement;

    @Column(name = "montant_paye", precision = 12, scale = 2)
    private BigDecimal montantPaye = BigDecimal.ZERO;

    @Column(name = "montant_rendu", precision = 12, scale = 2)
    private BigDecimal montantRendu = BigDecimal.ZERO;    // Monnaie rendue

    @Column(name = "reference_paiement", length = 100)
    private String referencePaiement;             // Réf transaction mobile money, CB, etc.

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    // ═══════════════════════════════════════════════════════════
    // LIGNES DE VENTE
    // ═══════════════════════════════════════════════════════════

    @OneToMany(mappedBy = "vente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneVente> lignes = new ArrayList<>();

    @Column(name = "nombre_articles")
    private Integer nombreArticles = 0;

    // ═══════════════════════════════════════════════════════════
    // VENDEUR
    // ═══════════════════════════════════════════════════════════

    @Column(name = "vendeur_id")
    private UUID vendeurId;

    @Column(name = "vendeur_nom", length = 200)
    private String vendeurNom;

    // ═══════════════════════════════════════════════════════════
    // ANNULATION
    // ═══════════════════════════════════════════════════════════

    @Column(name = "motif_annulation", length = 500)
    private String motifAnnulation;

    @Column(name = "annule_par_id")
    private UUID annuleParId;

    @Column(name = "annule_par_nom", length = 200)
    private String annuleParNom;

    @Column(name = "date_annulation")
    private LocalDateTime dateAnnulation;

    // ═══════════════════════════════════════════════════════════
    // NOTES
    // ═══════════════════════════════════════════════════════════

    @Column(length = 1000)
    private String notes;

    // ═══════════════════════════════════════════════════════════
    // MÉTADONNÉES
    // ═══════════════════════════════════════════════════════════

    @Column(name = "date_vente", nullable = false)
    private LocalDateTime dateVente;

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
     * Ajouter une ligne de vente
     */
    public void ajouterLigne(LigneVente ligne) {
        lignes.add(ligne);
        ligne.setVente(this);
        recalculerTotaux();
    }

    /**
     * Retirer une ligne de vente
     */
    public void retirerLigne(LigneVente ligne) {
        lignes.remove(ligne);
        ligne.setVente(null);
        recalculerTotaux();
    }

    /**
     * Recalculer tous les totaux
     */
    public void recalculerTotaux() {
        // Calculer les totaux depuis les lignes
        this.montantBrutHT = lignes.stream()
                .map(LigneVente::getMontantHT)
                .filter(m -> m != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.montantTVA = lignes.stream()
                .map(LigneVente::getMontantTVA)
                .filter(m -> m != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.montantBrutTTC = lignes.stream()
                .map(LigneVente::getMontantTTC)
                .filter(m -> m != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculer la remise
        if (this.pourcentageRemise != null && this.pourcentageRemise.compareTo(BigDecimal.ZERO) > 0) {
            this.montantRemise = this.montantBrutTTC
                    .multiply(this.pourcentageRemise)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        // Montant net après remise
        this.montantNetTTC = this.montantBrutTTC.subtract(
                this.montantRemise != null ? this.montantRemise : BigDecimal.ZERO);

        // Calculer la part mutuelle
        if (this.tauxPriseEnCharge != null && this.tauxPriseEnCharge.compareTo(BigDecimal.ZERO) > 0) {
            this.montantMutuelle = this.montantNetTTC
                    .multiply(this.tauxPriseEnCharge)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            this.montantMutuelle = BigDecimal.ZERO;
        }

        // Part client
        this.montantClient = this.montantNetTTC.subtract(this.montantMutuelle);

        // Nombre d'articles
        this.nombreArticles = lignes.stream()
                .mapToInt(l -> l.getQuantite() != null ? l.getQuantite() : 0)
                .sum();
    }

    /**
     * Appliquer une remise en pourcentage
     */
    public void appliquerRemise(BigDecimal pourcentage) {
        this.pourcentageRemise = pourcentage;
        recalculerTotaux();
    }

    /**
     * Appliquer une remise en montant
     */
    public void appliquerRemiseMontant(BigDecimal montant) {
        this.montantRemise = montant;
        if (this.montantBrutTTC.compareTo(BigDecimal.ZERO) > 0) {
            this.pourcentageRemise = montant
                    .multiply(BigDecimal.valueOf(100))
                    .divide(this.montantBrutTTC, 2, RoundingMode.HALF_UP);
        }
        this.montantNetTTC = this.montantBrutTTC.subtract(montant);
        // Recalculer mutuelle et part client
        if (this.tauxPriseEnCharge != null) {
            this.montantMutuelle = this.montantNetTTC
                    .multiply(this.tauxPriseEnCharge)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        this.montantClient = this.montantNetTTC.subtract(
                this.montantMutuelle != null ? this.montantMutuelle : BigDecimal.ZERO);
    }

    /**
     * Enregistrer un paiement
     */
    public void enregistrerPaiement(BigDecimal montant, ModePaiement mode, String reference) {
        this.montantPaye = (this.montantPaye != null ? this.montantPaye : BigDecimal.ZERO).add(montant);
        this.modePaiement = mode;
        this.referencePaiement = reference;
        this.datePaiement = LocalDateTime.now();

        // Calculer le rendu si paiement en espèces
        if (mode == ModePaiement.ESPECES && montant.compareTo(this.montantClient) > 0) {
            this.montantRendu = montant.subtract(this.montantClient);
        }

        // Mettre à jour le statut
        if (this.montantPaye.compareTo(this.montantClient) >= 0) {
            this.statut = StatutVente.VALIDEE;
        } else if (this.montantPaye.compareTo(BigDecimal.ZERO) > 0) {
            this.statut = StatutVente.PARTIELLEMENT_PAYEE;
        }
    }

    /**
     * Vérifie si la vente est payée
     */
    public boolean estPayee() {
        return this.montantPaye != null && this.montantClient != null
                && this.montantPaye.compareTo(this.montantClient) >= 0;
    }

    /**
     * Calcule le reste à payer
     */
    public BigDecimal getResteAPayer() {
        BigDecimal paye = this.montantPaye != null ? this.montantPaye : BigDecimal.ZERO;
        BigDecimal aPayer = this.montantClient != null ? this.montantClient : BigDecimal.ZERO;
        return aPayer.subtract(paye).max(BigDecimal.ZERO);
    }

    /**
     * Vérifie si la vente peut être modifiée
     */
    public boolean peutEtreModifiee() {
        return this.statut != null && this.statut.peutEtreModifiee();
    }

    /**
     * Vérifie si la vente peut être annulée
     */
    public boolean peutEtreAnnulee() {
        return this.statut != null && this.statut.peutEtreAnnulee();
    }

    @PrePersist
    protected void onCreate() {
        if (this.statut == null) {
            this.statut = StatutVente.EN_COURS;
        }
        if (this.type == null) {
            this.type = TypeVente.COMPTOIR;
        }
        if (this.dateVente == null) {
            this.dateVente = LocalDateTime.now();
        }
        if (this.montantBrutHT == null) this.montantBrutHT = BigDecimal.ZERO;
        if (this.montantTVA == null) this.montantTVA = BigDecimal.ZERO;
        if (this.montantBrutTTC == null) this.montantBrutTTC = BigDecimal.ZERO;
        if (this.montantRemise == null) this.montantRemise = BigDecimal.ZERO;
        if (this.montantNetTTC == null) this.montantNetTTC = BigDecimal.ZERO;
        if (this.montantMutuelle == null) this.montantMutuelle = BigDecimal.ZERO;
        if (this.montantClient == null) this.montantClient = BigDecimal.ZERO;
        if (this.montantPaye == null) this.montantPaye = BigDecimal.ZERO;
        if (this.montantRendu == null) this.montantRendu = BigDecimal.ZERO;
        if (this.nombreArticles == null) this.nombreArticles = 0;
    }
}
