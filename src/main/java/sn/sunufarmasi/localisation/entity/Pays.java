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
 * Entité Pays
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(
    name = "pays",
    indexes = {
        @Index(name = "idx_pays_code", columnList = "code"),
        @Index(name = "idx_pays_code_iso", columnList = "code_iso2")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pays {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Code pays (ex: SN, FR, US)
     */
    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    /**
     * Code ISO 2 lettres
     */
    @Column(name = "code_iso2", nullable = false, unique = true, length = 2)
    private String codeIso2;

    /**
     * Code ISO 3 lettres
     */
    @Column(name = "code_iso3", unique = true, length = 3)
    private String codeIso3;

    /**
     * Code téléphonique (ex: +221)
     */
    @Column(name = "indicatif_telephonique", length = 10)
    private String indicatifTelephonique;

    /**
     * Nom du pays
     */
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    /**
     * Nom en anglais
     */
    @Column(name = "nom_en", length = 100)
    private String nomEn;

    /**
     * Capitale
     */
    @Column(name = "capitale", length = 100)
    private String capitale;

    /**
     * Devise (code ISO)
     */
    @Column(name = "devise", length = 3)
    private String devise;

    /**
     * Fuseau horaire
     */
    @Column(name = "fuseau_horaire", length = 50)
    private String fuseauHoraire;

    /**
     * Drapeau (emoji ou URL)
     */
    @Column(name = "drapeau", length = 255)
    private String drapeau;

    /**
     * Actif
     */
    @Column(name = "actif")
    @Builder.Default
    private Boolean actif = true;

    /**
     * Régions du pays
     */
    @OneToMany(mappedBy = "pays", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Region> regions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    public void addRegion(Region region) {
        regions.add(region);
        region.setPays(this);
    }

    public void removeRegion(Region region) {
        regions.remove(region);
        region.setPays(null);
    }

    public int getNombreRegions() {
        return regions != null ? regions.size() : 0;
    }
}
