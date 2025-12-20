package sn.sunufarmasi.mutuelle.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant un adhérent/bénéficiaire d'une mutuelle
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(name = "adherents", indexes = {
        @Index(name = "idx_adherent_mutuelle", columnList = "mutuelle_id"),
        @Index(name = "idx_adherent_numero", columnList = "numero_adherent"),
        @Index(name = "idx_adherent_nom", columnList = "nom, prenom"),
        @Index(name = "idx_adherent_actif", columnList = "est_actif")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Adherent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ═══════════════════════════════════════════════════════════
    // MUTUELLE
    // ═══════════════════════════════════════════════════════════

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mutuelle_id", nullable = false)
    private Mutuelle mutuelle;

    // ═══════════════════════════════════════════════════════════
    // IDENTIFICATION ADHÉRENT
    // ═══════════════════════════════════════════════════════════

    @Column(name = "numero_adherent", nullable = false, length = 50)
    private String numeroAdherent;

    @Column(name = "numero_carte", length = 50)
    private String numeroCarte;

    @Column(name = "type_beneficiaire", length = 20)
    private String typeBeneficiaire;              // TITULAIRE, CONJOINT, ENFANT, ASCENDANT

    @Column(name = "adherent_principal_id")
    private UUID adherentPrincipalId;             // Si ayant droit, référence au titulaire

    // ═══════════════════════════════════════════════════════════
    // IDENTITÉ
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(length = 10)
    private String sexe;                          // M, F

    @Column(name = "numero_cni", length = 20)
    private String numeroCni;

    // ═══════════════════════════════════════════════════════════
    // COORDONNÉES
    // ═══════════════════════════════════════════════════════════

    @Column(length = 20)
    private String telephone;

    @Column(length = 100)
    private String email;

    @Column(length = 500)
    private String adresse;

    // ═══════════════════════════════════════════════════════════
    // EMPLOYEUR (si IPM)
    // ═══════════════════════════════════════════════════════════

    @Column(length = 200)
    private String employeur;

    @Column(name = "matricule_employe", length = 50)
    private String matriculeEmploye;

    // ═══════════════════════════════════════════════════════════
    // COUVERTURE
    // ═══════════════════════════════════════════════════════════

    @Column(name = "taux_couverture", precision = 5, scale = 2)
    private BigDecimal tauxCouverture;            // Taux spécifique si différent du contrat

    @Column(name = "plafond_annuel", precision = 12, scale = 2)
    private BigDecimal plafondAnnuel;

    @Column(name = "consommation_annuelle", precision = 12, scale = 2)
    private BigDecimal consommationAnnuelle = BigDecimal.ZERO;

    // ═══════════════════════════════════════════════════════════
    // VALIDITÉ
    // ═══════════════════════════════════════════════════════════

    @Column(name = "date_adhesion")
    private LocalDate dateAdhesion;

    @Column(name = "date_fin_droits")
    private LocalDate dateFinDroits;

    @Column(name = "est_actif")
    private Boolean estActif = true;

    @Column(name = "motif_radiation", length = 500)
    private String motifRadiation;

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

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    /**
     * Vérifie si les droits sont ouverts
     */
    public boolean droitsOuverts() {
        if (!estActif) return false;
        LocalDate aujourdhui = LocalDate.now();
        if (dateAdhesion != null && aujourdhui.isBefore(dateAdhesion)) return false;
        if (dateFinDroits != null && aujourdhui.isAfter(dateFinDroits)) return false;
        return true;
    }

    /**
     * Vérifie si le plafond est atteint
     */
    public boolean plafondAtteint() {
        if (plafondAnnuel == null) return false;
        return consommationAnnuelle != null && consommationAnnuelle.compareTo(plafondAnnuel) >= 0;
    }

    /**
     * Calcule le reste à consommer
     */
    public BigDecimal getResteAConsommer() {
        if (plafondAnnuel == null) return null;
        BigDecimal conso = consommationAnnuelle != null ? consommationAnnuelle : BigDecimal.ZERO;
        return plafondAnnuel.subtract(conso).max(BigDecimal.ZERO);
    }

    /**
     * Retourne le taux effectif
     */
    public BigDecimal getTauxEffectif() {
        if (tauxCouverture != null) return tauxCouverture;
        return mutuelle != null ? mutuelle.getTauxCouvertureDefaut() : BigDecimal.ZERO;
    }

    /**
     * Ajouter une consommation
     */
    public void ajouterConsommation(BigDecimal montant) {
        if (consommationAnnuelle == null) {
            consommationAnnuelle = BigDecimal.ZERO;
        }
        consommationAnnuelle = consommationAnnuelle.add(montant);
    }

    @PrePersist
    protected void onCreate() {
        if (estActif == null) estActif = true;
        if (consommationAnnuelle == null) consommationAnnuelle = BigDecimal.ZERO;
        if (typeBeneficiaire == null) typeBeneficiaire = "TITULAIRE";
    }
}
