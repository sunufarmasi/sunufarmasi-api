package sn.sunufarmasi.localisation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sn.sunufarmasi.localisation.enums.TypeCommune;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité Commune
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(
    name = "communes",
    indexes = {
        @Index(name = "idx_commune_code", columnList = "code"),
        @Index(name = "idx_commune_departement", columnList = "departement_id"),
        @Index(name = "idx_commune_type", columnList = "type_commune"),
        @Index(name = "idx_commune_coords", columnList = "latitude, longitude")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_commune_code_departement", columnNames = {"code", "departement_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commune {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Département
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id", nullable = false)
    private Departement departement;

    /**
     * Code commune
     */
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    /**
     * Nom de la commune
     */
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    /**
     * Type de commune
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_commune", length = 30)
    @Builder.Default
    private TypeCommune type = TypeCommune.COMMUNE;

    /**
     * Code postal
     */
    @Column(name = "code_postal", length = 10)
    private String codePostal;

    /**
     * Population
     */
    @Column(name = "population")
    private Long population;

    /**
     * Superficie en km²
     */
    @Column(name = "superficie")
    private Double superficie;

    /**
     * Latitude du centre
     */
    @Column(name = "latitude")
    private Double latitude;

    /**
     * Longitude du centre
     */
    @Column(name = "longitude")
    private Double longitude;

    /**
     * Altitude moyenne en mètres
     */
    @Column(name = "altitude")
    private Integer altitude;

    /**
     * Ordre d'affichage
     */
    @Column(name = "ordre")
    @Builder.Default
    private Integer ordre = 0;

    /**
     * Active
     */
    @Column(name = "actif")
    @Builder.Default
    private Boolean actif = true;

    /**
     * Zone urbaine
     */
    @Column(name = "zone_urbaine")
    @Builder.Default
    private Boolean zoneUrbaine = false;

    /**
     * Arrondissements (pour les grandes villes comme Dakar)
     */
    @Column(name = "arrondissement", length = 100)
    private String arrondissement;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    public String getNomComplet() {
        StringBuilder sb = new StringBuilder(nom);
        if (departement != null) {
            sb.append(", ").append(departement.getNom());
            if (departement.getRegion() != null) {
                sb.append(", ").append(departement.getRegion().getNom());
            }
        }
        return sb.toString();
    }

    /**
     * Obtenir la région via le département
     */
    public Region getRegion() {
        return departement != null ? departement.getRegion() : null;
    }

    /**
     * Obtenir le pays via le département et la région
     */
    public Pays getPays() {
        Region region = getRegion();
        return region != null ? region.getPays() : null;
    }

    /**
     * Vérifier si les coordonnées sont disponibles
     */
    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    /**
     * Calculer la distance avec un autre point (Haversine)
     */
    public Double distanceFrom(Double lat, Double lng) {
        if (!hasCoordinates() || lat == null || lng == null) {
            return null;
        }

        final int R = 6371; // Rayon de la Terre en km

        double latDistance = Math.toRadians(lat - this.latitude);
        double lngDistance = Math.toRadians(lng - this.longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(lat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round(R * c * 10.0) / 10.0;
    }
}
