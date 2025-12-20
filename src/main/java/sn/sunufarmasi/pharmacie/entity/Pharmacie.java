package sn.sunufarmasi.pharmacie.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sn.sunufarmasi.localisation.entity.Commune;
import sn.sunufarmasi.pharmacie.enums.StatutPharmacie;
import sn.sunufarmasi.syndicat.entity.Syndicat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant une pharmacie
 *
 * Une pharmacie est rattachée à un syndicat, gérée par un pharmacien propriétaire,
 * et dispose d'un abonnement pour accéder aux fonctionnalités de la plateforme.
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(name = "pharmacies", indexes = {
        @Index(name = "idx_pharmacie_code", columnList = "code"),
        @Index(name = "idx_pharmacie_commune", columnList = "commune_id"),
        @Index(name = "idx_pharmacie_syndicat", columnList = "syndicat_id"),
        @Index(name = "idx_pharmacie_proprietaire", columnList = "pharmacien_proprietaire_id"),
        @Index(name = "idx_pharmacie_statut", columnList = "statut"),
        @Index(name = "idx_pharmacie_coordinates", columnList = "latitude, longitude")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Pharmacie {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS GÉNÉRALES
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, unique = true, length = 30)
    private String code;                         // PHAR-DKR-001 (auto-généré)

    @Column(nullable = false, length = 200)
    private String nom;                          // Pharmacie du Plateau

    @Column(length = 200)
    private String raisonSociale;                // SARL Pharmacie du Plateau

    // ═══════════════════════════════════════════════════════════
    // LOCALISATION
    // ═══════════════════════════════════════════════════════════

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_id", nullable = false)
    private Commune commune;

    @Column(name = "adresse_complete", length = 500)
    private String adresseComplete;              // Avenue Pompidou, Immeuble Fahd

    @Column(length = 100)
    private String quartier;                     // Plateau

    @Column(precision = 10)
    private Double latitude;                     // 14.6928

    @Column(precision = 10)
    private Double longitude;                    // -17.4467

    @Column(length = 500)
    private String lienGoogleMaps;               // https://maps.google.com/?q=14.6928,-17.4467

    // ═══════════════════════════════════════════════════════════
    // CONTACT
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, unique = true, length = 20)
    private String telephone;                    // +221338234567

    @Column(length = 20)
    private String telephoneSecondaire;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 200)
    private String siteWeb;

    // ═══════════════════════════════════════════════════════════
    // DOCUMENTS LÉGAUX
    // ═══════════════════════════════════════════════════════════

    @Column(unique = true, length = 50)
    private String numeroAgrementMinistere;      // Agrément Ministère de la Santé

    @Column(unique = true, length = 50)
    private String numeroOrdre;                  // Inscription Ordre des Pharmaciens

    @Column(name = "date_ouverture")
    private LocalDate dateOuverture;

    @Column(length = 500)
    private String registreCommerceUrl;          // URL document RC scanné

    @Column(length = 500)
    private String agrementMinistereUrl;         // URL document agrément

    @Column(length = 500)
    private String assuranceUrl;                 // URL assurance professionnelle

    // ═══════════════════════════════════════════════════════════
    // HORAIRES (JSON)
    // ═══════════════════════════════════════════════════════════

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private String horaires;
    /* Format JSON:
    {
      "lundi": {"ouverture": "08:00", "fermeture": "20:00"},
      "mardi": {"ouverture": "08:00", "fermeture": "20:00"},
      "mercredi": {"ouverture": "08:00", "fermeture": "20:00"},
      "jeudi": {"ouverture": "08:00", "fermeture": "20:00"},
      "vendredi": {"ouverture": "08:00", "fermeture": "20:00"},
      "samedi": {"ouverture": "09:00", "fermeture": "18:00"},
      "dimanche": {"ferme": true}
    }
    */

    // ═══════════════════════════════════════════════════════════
    // IMAGES
    // ═══════════════════════════════════════════════════════════

    @Column(length = 500)
    private String logoUrl;

    @Column(length = 500)
    private String photoFacadeUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private String photosInterieuresUrls;        // Array JSON de photos

    // ═══════════════════════════════════════════════════════════
    // RELATIONS
    // ═══════════════════════════════════════════════════════════

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syndicat_id")
    private Syndicat syndicat;                   // Syndicat qui gère cette pharmacie (optionnel au début)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacien_proprietaire_id", nullable = false)
    private Pharmacien pharmacienProprietaire;   // Pharmacien qui possède cette pharmacie

    // ═══════════════════════════════════════════════════════════
    // STATUT PHARMACIE
    // ═══════════════════════════════════════════════════════════

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutPharmacie statut;              // EN_ATTENTE, VALIDEE, REJETEE, SUSPENDUE, FERMEE

    @Column(length = 500)
    private String motifRejet;                   // Si rejetée par syndicat

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Column(name = "valide_par_id")
    private UUID valideParId;                    // ID de l'admin/syndicat qui a validé

    @Column(name = "valide_par_nom")
    private String valideParNom;                 // Nom pour historique

    // ═══════════════════════════════════════════════════════════
    // CONFIGURATION
    // ═══════════════════════════════════════════════════════════

    private Boolean accepteCommandes = true;

    private Boolean proposeLivraison = false;

    @Column(name = "rayon_livraison_km")
    private Integer rayonLivraisonKm;

    private Boolean notificationsActives = true;

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
     * Vérifie si la pharmacie est validée et peut être utilisée
     */
    public boolean estOperationnelle() {
        return this.statut == StatutPharmacie.VALIDEE;
    }

    /**
     * Vérifie si la pharmacie peut accepter des commandes
     */
    public boolean peutAccepterCommandes() {
        return this.estOperationnelle() && this.accepteCommandes;
    }

    /**
     * Génère le lien Google Maps à partir des coordonnées
     */
    public String genererLienGoogleMaps() {
        if (this.latitude != null && this.longitude != null) {
            return String.format("https://maps.google.com/?q=%s,%s", this.latitude, this.longitude);
        }
        return null;
    }

    /**
     * Définit les coordonnées et génère automatiquement le lien Google Maps
     */
    public void setCoordonnees(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.lienGoogleMaps = genererLienGoogleMaps();
    }

    @PrePersist
    protected void onCreate() {
        // Initialiser le statut
        if (this.statut == null) {
            this.statut = StatutPharmacie.EN_ATTENTE;
        }
        if (this.accepteCommandes == null) {
            this.accepteCommandes = true;
        }
        if (this.notificationsActives == null) {
            this.notificationsActives = true;
        }
        if (this.proposeLivraison == null) {
            this.proposeLivraison = false;
        }

        // Générer automatiquement le lien Google Maps si coordonnées présentes
        if (this.latitude != null && this.longitude != null && this.lienGoogleMaps == null) {
            this.lienGoogleMaps = genererLienGoogleMaps();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Mettre à jour le lien Google Maps si coordonnées changent
        if (this.latitude != null && this.longitude != null) {
            this.lienGoogleMaps = genererLienGoogleMaps();
        }
    }
}