package sn.sunufarmasi.mutuelle.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sn.sunufarmasi.mutuelle.enums.StatutDemande;
import sn.sunufarmasi.pharmacie.entity.Pharmacie;
import sn.sunufarmasi.vente.entity.Vente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant une demande de remboursement à une mutuelle
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(name = "demandes_remboursement", indexes = {
        @Index(name = "idx_demande_numero", columnList = "numero"),
        @Index(name = "idx_demande_pharmacie", columnList = "pharmacie_id"),
        @Index(name = "idx_demande_mutuelle", columnList = "mutuelle_id"),
        @Index(name = "idx_demande_adherent", columnList = "adherent_id"),
        @Index(name = "idx_demande_vente", columnList = "vente_id"),
        @Index(name = "idx_demande_statut", columnList = "statut"),
        @Index(name = "idx_demande_date", columnList = "date_demande")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class DemandeRemboursement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ═══════════════════════════════════════════════════════════
    // IDENTIFICATION
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, unique = true, length = 30)
    private String numero;                        // RMB-PHAR001-20240115-0001

    // ═══════════════════════════════════════════════════════════
    // RELATIONS
    // ═══════════════════════════════════════════════════════════

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacie_id", nullable = false)
    private Pharmacie pharmacie;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mutuelle_id", nullable = false)
    private Mutuelle mutuelle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "adherent_id", nullable = false)
    private Adherent adherent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vente_id", nullable = false)
    private Vente vente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrat_id")
    private ContratMutuelle contrat;

    // ═══════════════════════════════════════════════════════════
    // STATUT
    // ═══════════════════════════════════════════════════════════

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private StatutDemande statut;

    // ═══════════════════════════════════════════════════════════
    // DATES
    // ═══════════════════════════════════════════════════════════

    @Column(name = "date_demande", nullable = false)
    private LocalDateTime dateDemande;

    @Column(name = "date_soumission")
    private LocalDateTime dateSoumission;

    @Column(name = "date_reponse")
    private LocalDateTime dateReponse;

    @Column(name = "date_paiement")
    private LocalDate datePaiement;

    // ═══════════════════════════════════════════════════════════
    // MONTANTS
    // ═══════════════════════════════════════════════════════════

    @Column(name = "montant_vente", precision = 12, scale = 2, nullable = false)
    private BigDecimal montantVente;              // Montant total de la vente

    @Column(name = "montant_demande", precision = 12, scale = 2, nullable = false)
    private BigDecimal montantDemande;            // Montant demandé au remboursement

    @Column(name = "taux_applique", precision = 5, scale = 2)
    private BigDecimal tauxApplique;              // Taux de prise en charge appliqué

    @Column(name = "montant_accepte", precision = 12, scale = 2)
    private BigDecimal montantAccepte;            // Montant accepté par la mutuelle

    @Column(name = "montant_rejete", precision = 12, scale = 2)
    private BigDecimal montantRejete;             // Montant rejeté

    @Column(name = "montant_paye", precision = 12, scale = 2)
    private BigDecimal montantPaye;               // Montant effectivement reçu

    // ═══════════════════════════════════════════════════════════
    // ORDONNANCE
    // ═══════════════════════════════════════════════════════════

    @Column(name = "numero_ordonnance", length = 50)
    private String numeroOrdonnance;

    @Column(name = "medecin_prescripteur", length = 200)
    private String medecinPrescripteur;

    @Column(name = "date_ordonnance")
    private LocalDate dateOrdonnance;

    // ═══════════════════════════════════════════════════════════
    // PAIEMENT
    // ═══════════════════════════════════════════════════════════

    @Column(name = "reference_paiement", length = 100)
    private String referencePaiement;             // Référence virement/chèque

    @Column(name = "mode_paiement", length = 50)
    private String modePaiement;

    // ═══════════════════════════════════════════════════════════
    // RÉPONSE MUTUELLE
    // ═══════════════════════════════════════════════════════════

    @Column(name = "reference_mutuelle", length = 100)
    private String referenceMutuelle;             // Référence dossier mutuelle

    @Column(name = "motif_rejet", length = 1000)
    private String motifRejet;

    @Column(name = "observations_mutuelle", length = 1000)
    private String observationsMutuelle;

    // ═══════════════════════════════════════════════════════════
    // DOCUMENTS
    // ═══════════════════════════════════════════════════════════

    @Column(name = "facture_url", length = 500)
    private String factureUrl;

    @Column(name = "ordonnance_url", length = 500)
    private String ordonnanceUrl;

    // ═══════════════════════════════════════════════════════════
    // NOTES
    // ═══════════════════════════════════════════════════════════

    @Column(length = 1000)
    private String notes;

    // ═══════════════════════════════════════════════════════════
    // CRÉATEUR
    // ═══════════════════════════════════════════════════════════

    @Column(name = "cree_par_id")
    private UUID creeParId;

    @Column(name = "cree_par_nom", length = 200)
    private String creeParNom;

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
     * Calcule le reste à payer
     */
    public BigDecimal getResteAPayer() {
        BigDecimal accepte = montantAccepte != null ? montantAccepte : montantDemande;
        BigDecimal paye = montantPaye != null ? montantPaye : BigDecimal.ZERO;
        return accepte.subtract(paye).max(BigDecimal.ZERO);
    }

    /**
     * Vérifie si la demande est entièrement payée
     */
    public boolean estPayee() {
        return montantPaye != null && montantAccepte != null
                && montantPaye.compareTo(montantAccepte) >= 0;
    }

    /**
     * Soumettre la demande
     */
    public void soumettre() {
        this.statut = StatutDemande.SOUMISE;
        this.dateSoumission = LocalDateTime.now();
    }

    /**
     * Accepter la demande
     */
    public void accepter(BigDecimal montant, String reference) {
        this.statut = StatutDemande.ACCEPTEE;
        this.montantAccepte = montant;
        this.referenceMutuelle = reference;
        this.dateReponse = LocalDateTime.now();

        if (montant.compareTo(montantDemande) < 0) {
            this.statut = StatutDemande.PARTIELLEMENT_ACCEPTEE;
            this.montantRejete = montantDemande.subtract(montant);
        }
    }

    /**
     * Rejeter la demande
     */
    public void rejeter(String motif) {
        this.statut = StatutDemande.REJETEE;
        this.motifRejet = motif;
        this.montantRejete = montantDemande;
        this.montantAccepte = BigDecimal.ZERO;
        this.dateReponse = LocalDateTime.now();
    }

    /**
     * Enregistrer le paiement
     */
    public void enregistrerPaiement(BigDecimal montant, String reference, String mode) {
        this.montantPaye = montant;
        this.referencePaiement = reference;
        this.modePaiement = mode;
        this.datePaiement = LocalDate.now();
        this.statut = StatutDemande.PAYEE;
    }

    @PrePersist
    protected void onCreate() {
        if (statut == null) {
            statut = StatutDemande.BROUILLON;
        }
        if (dateDemande == null) {
            dateDemande = LocalDateTime.now();
        }
    }
}
