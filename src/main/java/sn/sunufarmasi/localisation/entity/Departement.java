package sn.sunufarmasi.localisation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entité Département
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(
    name = "departements",
    indexes = {
        @Index(name = "idx_departement_code", columnList = "code"),
        @Index(name = "idx_departement_region", columnList = "region_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_departement_code_region", columnNames = {"code", "region_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Departement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Région
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    /**
     * Code département
     */
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    /**
     * Nom du département
     */
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    /**
     * Chef-lieu
     */
    @Column(name = "chef_lieu", length = 100)
    private String chefLieu;

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
     * Ordre d'affichage
     */
    @Column(name = "ordre")
    @Builder.Default
    private Integer ordre = 0;

    /**
     * Actif
     */
    @Column(name = "actif")
    @Builder.Default
    private Boolean actif = true;

    /**
     * Communes du département
     */
    @OneToMany(mappedBy = "departement", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("nom ASC")
    private List<Commune> communes = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    public void addCommune(Commune commune) {
        communes.add(commune);
        commune.setDepartement(this);
    }

    public void removeCommune(Commune commune) {
        communes.remove(commune);
        commune.setDepartement(null);
    }

    public int getNombreCommunes() {
        return communes != null ? communes.size() : 0;
    }

    public String getNomComplet() {
        StringBuilder sb = new StringBuilder(nom);
        if (region != null) {
            sb.append(", ").append(region.getNom());
            if (region.getPays() != null) {
                sb.append(", ").append(region.getPays().getNom());
            }
        }
        return sb.toString();
    }

    /**
     * Obtenir le pays via la région
     */
    public Pays getPays() {
        return region != null ? region.getPays() : null;
    }
}
