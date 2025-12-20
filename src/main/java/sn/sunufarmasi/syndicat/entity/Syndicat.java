package sn.sunufarmasi.syndicat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sn.sunufarmasi.localisation.entity.Commune;
import sn.sunufarmasi.localisation.entity.Departement;
import sn.sunufarmasi.localisation.entity.Region;
import sn.sunufarmasi.pharmacie.entity.Pharmacien;
import sn.sunufarmasi.syndicat.enums.PlanAbonnementSyndicat;
import sn.sunufarmasi.syndicat.enums.StatutSyndicat;
import sn.sunufarmasi.syndicat.enums.TypeSyndicat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant un syndicat de pharmaciens
 *
 * Un syndicat gère les pharmacies d'une commune ou d'un département.
 * Il est responsable de la validation des pharmacies et de la planification des gardes.
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(name = "syndicats", indexes = {
        @Index(name = "idx_syndicat_code", columnList = "code"),
        @Index(name = "idx_syndicat_username", columnList = "username"),
        @Index(name = "idx_syndicat_commune", columnList = "commune_id"),
        @Index(name = "idx_syndicat_departement", columnList = "departement_id"),
        @Index(name = "idx_syndicat_type", columnList = "type"),
        @Index(name = "idx_syndicat_statut", columnList = "statut")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Syndicat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS GÉNÉRALES
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, unique = true, length = 30)
    private String code;                         // SYN-DKR-001 (auto-généré)

    @Column(nullable = false, length = 200)
    private String nom;                          // Syndicat des Pharmaciens de Dakar

    @Column(length = 20)
    private String sigle;                        // SPD

    @Column(length = 500)
    private String description;

    // ═══════════════════════════════════════════════════════════
    // AUTHENTIFICATION
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, unique = true, length = 50)
    private String username;                     // Défini par l'admin, unique

    @Column(name = "mot_de_passe_hash", nullable = false)
    private String motDePasseHash;

    // ═══════════════════════════════════════════════════════════
    // TYPE ET PORTÉE GÉOGRAPHIQUE
    // ═══════════════════════════════════════════════════════════

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeSyndicat type;                   // COMMUNE ou DEPARTEMENT

    /**
     * Si type = COMMUNE : la commune gérée
     * Si type = DEPARTEMENT : null (on utilise departement)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_id")
    private Commune commune;

    /**
     * Si type = DEPARTEMENT : le département géré
     * Si type = COMMUNE : null (on utilise commune)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id")
    private Departement departement;

    /**
     * Région de référence (déduite de commune ou département)
     * Stockée pour faciliter les requêtes
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    // ═══════════════════════════════════════════════════════════
    // CONTACT
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, length = 20)
    private String telephone;

    @Column(length = 20)
    private String telephoneSecondaire;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 500)
    private String adresse;

    // ═══════════════════════════════════════════════════════════
    // DOCUMENTS LÉGAUX
    // ═══════════════════════════════════════════════════════════

    @Column(unique = true, length = 50)
    private String numeroAgrementNational;       // Agrément Ordre National des Pharmaciens

    @Column(length = 500)
    private String agrementUrl;                  // URL document agrément

    @Column(length = 500)
    private String registreCommerceUrl;

    @Column(length = 200)
    private String siteWeb;

    // ═══════════════════════════════════════════════════════════
    // RESPONSABLE (Pharmacien existant)
    // ═══════════════════════════════════════════════════════════

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Pharmacien responsable;              // Pharmacien qui dirige le syndicat (optionnel)

    @Column(name = "nom_responsable", length = 200)
    private String nomResponsable;               // Nom du responsable (si pas de compte pharmacien)

    @Column(length = 20)
    private String telephoneResponsable;

    // ═══════════════════════════════════════════════════════════
    // ABONNEMENT
    // ═══════════════════════════════════════════════════════════

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanAbonnementSyndicat plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_abonnement", nullable = false, length = 20)
    private sn.sunufarmasi.pharmacie.enums.StatutAbonnement statutAbonnement;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montantMensuel;

    private Boolean essaiGratuit = true;

    @Column(name = "date_debut_essai")
    private LocalDate dateDebutEssai;

    @Column(name = "date_fin_essai")
    private LocalDate dateFinEssai;

    @Column(name = "date_debut_abonnement")
    private LocalDate dateDebutAbonnement;

    @Column(name = "date_fin_abonnement")
    private LocalDate dateFinAbonnement;

    @Column(name = "date_dernier_paiement")
    private LocalDate dateDernierPaiement;

    @Column(name = "prochain_paiement")
    private LocalDate prochainPaiement;

    private Boolean renouvellementAutomatique = false;

    // ═══════════════════════════════════════════════════════════
    // LIMITATIONS
    // ═══════════════════════════════════════════════════════════

    @Column(name = "nombre_pharmacies_max")
    private Integer nombrePharmaciesMax;

    @Column(name = "nombre_pharmacies_actuelles")
    private Integer nombrePharmaciesActuelles = 0;

    // ═══════════════════════════════════════════════════════════
    // STATUT
    // ═══════════════════════════════════════════════════════════

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutSyndicat statut;

    @Column(length = 500)
    private String motifSuspension;              // Motif si suspendu

    // ═══════════════════════════════════════════════════════════
    // MÉTADONNÉES
    // ═══════════════════════════════════════════════════════════

    @Column(name = "cree_par_id")
    private UUID creeParId;                      // ID de l'admin qui a créé

    @Column(name = "cree_par_nom", length = 200)
    private String creeParNom;                   // Nom de l'admin pour historique

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Vérifie si le syndicat est opérationnel
     */
    public boolean estOperationnel() {
        return this.statut == StatutSyndicat.ACTIF && abonnementActif();
    }

    /**
     * Vérifie si l'abonnement est actif
     */
    public boolean abonnementActif() {
        // Pendant l'essai gratuit
        if (this.essaiGratuit && this.dateFinEssai != null && this.dateFinEssai.isAfter(LocalDate.now())) {
            return true;
        }
        // Abonnement payant actif
        return this.statutAbonnement == sn.sunufarmasi.pharmacie.enums.StatutAbonnement.ACTIF
                && this.dateFinAbonnement != null
                && this.dateFinAbonnement.isAfter(LocalDate.now());
    }

    /**
     * Vérifie si le syndicat peut ajouter une pharmacie
     */
    public boolean peutAjouterPharmacie() {
        if (!estOperationnel()) {
            return false;
        }
        int actuelles = this.nombrePharmaciesActuelles == null ? 0 : this.nombrePharmaciesActuelles;
        int max = this.nombrePharmaciesMax == null ? Integer.MAX_VALUE : this.nombrePharmaciesMax;
        return actuelles < max;
    }

    /**
     * Incrémente le nombre de pharmacies
     */
    public void ajouterPharmacie() {
        this.nombrePharmaciesActuelles = (this.nombrePharmaciesActuelles == null ? 0 : this.nombrePharmaciesActuelles) + 1;
    }

    /**
     * Décrémente le nombre de pharmacies
     */
    public void retirerPharmacie() {
        if (this.nombrePharmaciesActuelles != null && this.nombrePharmaciesActuelles > 0) {
            this.nombrePharmaciesActuelles--;
        }
    }

    /**
     * Retourne le nom de la zone gérée (commune ou département)
     */
    public String getNomZone() {
        if (this.type == TypeSyndicat.COMMUNE && this.commune != null) {
            return this.commune.getNom();
        }
        if (this.type == TypeSyndicat.DEPARTEMENT && this.departement != null) {
            return this.departement.getNom();
        }
        return null;
    }

    /**
     * Retourne le nom complet avec région
     */
    public String getNomCompletZone() {
        String zone = getNomZone();
        if (zone != null && this.region != null) {
            return zone + ", " + this.region.getNom();
        }
        return zone;
    }

    /**
     * Enregistre une connexion
     */
    public void enregistrerConnexion() {
        this.derniereConnexion = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.statut == null) {
            this.statut = StatutSyndicat.ACTIF;
        }

        if (this.plan == null) {
            // Déterminer le plan selon le type
            if (this.type == TypeSyndicat.DEPARTEMENT) {
                this.plan = PlanAbonnementSyndicat.DEPARTEMENT;
            } else {
                this.plan = PlanAbonnementSyndicat.COMMUNE;
            }
        }

        // Appliquer les paramètres du plan
        if (this.montantMensuel == null) {
            this.montantMensuel = this.plan.getMontantMensuel();
        }
        if (this.nombrePharmaciesMax == null) {
            this.nombrePharmaciesMax = this.plan.getNombrePharmaciesMax();
        }

        if (this.statutAbonnement == null) {
            this.statutAbonnement = sn.sunufarmasi.pharmacie.enums.StatutAbonnement.ACTIF;
        }

        if (this.essaiGratuit == null) {
            this.essaiGratuit = true;
        }

        // Activer l'essai gratuit automatiquement
        if (this.essaiGratuit && this.dateDebutEssai == null) {
            this.dateDebutEssai = LocalDate.now();
            this.dateFinEssai = LocalDate.now().plusMonths(1);
        }

        if (this.nombrePharmaciesActuelles == null) {
            this.nombrePharmaciesActuelles = 0;
        }

        if (this.renouvellementAutomatique == null) {
            this.renouvellementAutomatique = false;
        }
    }
}
