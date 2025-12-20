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
 * Entité Région
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(
    name = "regions",
    indexes = {
        @Index(name = "idx_region_code", columnList = "code"),
        @Index(name = "idx_region_pays", columnList = "pays_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_region_code_pays", columnNames = {"code", "pays_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Pays
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pays_id", nullable = false)
    private Pays pays;

    /**
     * Code région (ex: DK pour Dakar)
     */
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    /**
     * Nom de la région
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
     * Active
     */
    @Column(name = "actif")
    @Builder.Default
    private Boolean actif = true;

    /**
     * Départements de la région
     */
    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("nom ASC")
    private List<Departement> departements = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    public void addDepartement(Departement departement) {
        departements.add(departement);
        departement.setRegion(this);
    }

    public void removeDepartement(Departement departement) {
        departements.remove(departement);
        departement.setRegion(null);
    }

    public int getNombreDepartements() {
        return departements != null ? departements.size() : 0;
    }

    public int getNombreCommunes() {
        if (departements == null) return 0;
        return departements.stream()
                .mapToInt(Departement::getNombreCommunes)
                .sum();
    }

    public String getNomComplet() {
        return nom + (pays != null ? ", " + pays.getNom() : "");
    }
}
