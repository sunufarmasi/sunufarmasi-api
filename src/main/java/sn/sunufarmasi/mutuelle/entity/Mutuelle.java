package sn.sunufarmasi.mutuelle.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sn.sunufarmasi.mutuelle.enums.TypeCouverture;
import sn.sunufarmasi.mutuelle.enums.TypeMutuelle;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant une mutuelle ou organisme payeur
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(name = "mutuelles", indexes = {
        @Index(name = "idx_mutuelle_code", columnList = "code"),
        @Index(name = "idx_mutuelle_nom", columnList = "nom"),
        @Index(name = "idx_mutuelle_type", columnList = "type"),
        @Index(name = "idx_mutuelle_actif", columnList = "est_actif")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Mutuelle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ═══════════════════════════════════════════════════════════
    // IDENTIFICATION
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, unique = true, length = 20)
    private String code;                          // MUT-XXXXX

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(name = "nom_court", length = 50)
    private String nomCourt;                      // Sigle/Abréviation

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeMutuelle type;

    // ═══════════════════════════════════════════════════════════
    // COORDONNÉES
    // ═══════════════════════════════════════════════════════════

    @Column(length = 500)
    private String adresse;

    @Column(length = 100)
    private String ville;

    @Column(length = 20)
    private String telephone;

    @Column(name = "telephone_urgence", length = 20)
    private String telephoneUrgence;

    @Column(length = 100)
    private String email;

    @Column(name = "site_web", length = 200)
    private String siteWeb;

    // ═══════════════════════════════════════════════════════════
    // IDENTIFICATION FISCALE
    // ═══════════════════════════════════════════════════════════

    @Column(length = 50)
    private String ninea;

    @Column(name = "numero_agrement", length = 50)
    private String numeroAgrement;

    // ═══════════════════════════════════════════════════════════
    // CONTACTS
    // ═══════════════════════════════════════════════════════════

    @Column(name = "contact_nom", length = 200)
    private String contactNom;

    @Column(name = "contact_telephone", length = 20)
    private String contactTelephone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "contact_poste", length = 100)
    private String contactPoste;

    // ═══════════════════════════════════════════════════════════
    // CONDITIONS DE REMBOURSEMENT
    // ═══════════════════════════════════════════════════════════

    @Enumerated(EnumType.STRING)
    @Column(name = "type_couverture", length = 25)
    private TypeCouverture typeCouverture;

    @Column(name = "taux_couverture_defaut", precision = 5, scale = 2)
    private BigDecimal tauxCouvertureDefaut;      // Taux par défaut (ex: 80.00)

    @Column(name = "plafond_annuel", precision = 12, scale = 2)
    private BigDecimal plafondAnnuel;             // Plafond annuel de remboursement

    @Column(name = "plafond_par_acte", precision = 12, scale = 2)
    private BigDecimal plafondParActe;            // Plafond par acte

    @Column(name = "franchise", precision = 12, scale = 2)
    private BigDecimal franchise;                 // Franchise (reste à charge minimum)

    @Column(name = "delai_carence_jours")
    private Integer delaiCarenceJours;            // Délai de carence

    // ═══════════════════════════════════════════════════════════
    // CONDITIONS DE PAIEMENT
    // ═══════════════════════════════════════════════════════════

    @Column(name = "delai_paiement_jours")
    private Integer delaiPaiementJours;           // Délai de paiement moyen

    @Column(name = "mode_paiement", length = 100)
    private String modePaiement;                  // Virement, chèque, etc.

    @Column(name = "jour_paiement")
    private Integer jourPaiement;                 // Jour du mois pour paiement

    // ═══════════════════════════════════════════════════════════
    // BANCAIRE
    // ═══════════════════════════════════════════════════════════

    @Column(length = 100)
    private String banque;

    @Column(length = 50)
    private String iban;

    // ═══════════════════════════════════════════════════════════
    // EXIGENCES DOCUMENTAIRES
    // ═══════════════════════════════════════════════════════════

    @Column(name = "exige_ordonnance")
    private Boolean exigeOrdonnance = true;

    @Column(name = "exige_carte_adherent")
    private Boolean exigeCarteAdherent = true;

    @Column(name = "exige_facture_detaillee")
    private Boolean exigeFactureDetaillee = true;

    @Column(name = "delai_soumission_jours")
    private Integer delaiSoumissionJours;         // Délai max pour soumettre les factures

    // ═══════════════════════════════════════════════════════════
    // PRODUITS COUVERTS
    // ═══════════════════════════════════════════════════════════

    @Column(name = "couvre_medicaments")
    private Boolean couvreMedicaments = true;

    @Column(name = "couvre_generiques")
    private Boolean couvreGeneriques = true;

    @Column(name = "couvre_paramedical")
    private Boolean couvreParamedical = false;

    @Column(name = "couvre_cosmetique")
    private Boolean couvreCosmetique = false;

    @Column(name = "liste_exclusions", length = 2000)
    private String listeExclusions;               // Produits/catégories exclus

    // ═══════════════════════════════════════════════════════════
    // STATUT
    // ═══════════════════════════════════════════════════════════

    @Column(name = "est_actif")
    private Boolean estActif = true;

    @Column(length = 1000)
    private String notes;

    // ═══════════════════════════════════════════════════════════
    // LOGO
    // ═══════════════════════════════════════════════════════════

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

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

    public String getNomAffiche() {
        return nomCourt != null ? nomCourt : nom;
    }

    /**
     * Calcule le montant pris en charge
     */
    public BigDecimal calculerPriseEnCharge(BigDecimal montant) {
        if (tauxCouvertureDefaut == null || montant == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal pec = montant.multiply(tauxCouvertureDefaut)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

        // Appliquer le plafond par acte si défini
        if (plafondParActe != null && pec.compareTo(plafondParActe) > 0) {
            pec = plafondParActe;
        }

        // Déduire la franchise si définie
        if (franchise != null && pec.compareTo(franchise) > 0) {
            pec = pec.subtract(franchise);
        }

        return pec.max(BigDecimal.ZERO);
    }

    @PrePersist
    protected void onCreate() {
        if (estActif == null) estActif = true;
        if (exigeOrdonnance == null) exigeOrdonnance = true;
        if (exigeCarteAdherent == null) exigeCarteAdherent = true;
        if (exigeFactureDetaillee == null) exigeFactureDetaillee = true;
        if (couvreMedicaments == null) couvreMedicaments = true;
        if (couvreGeneriques == null) couvreGeneriques = true;
        if (couvreParamedical == null) couvreParamedical = false;
        if (couvreCosmetique == null) couvreCosmetique = false;
    }
}
