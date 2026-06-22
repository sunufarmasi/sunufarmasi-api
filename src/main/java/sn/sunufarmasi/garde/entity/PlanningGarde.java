package sn.sunufarmasi.garde.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sn.sunufarmasi.syndicat.entity.Syndicat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Planning de garde - Gestion globale par période
 * Créé et géré par le syndicat
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(
        name = "plannings_garde",
        indexes = {
                @Index(name = "idx_planning_syndicat", columnList = "syndicat_id"),
                @Index(name = "idx_planning_dates", columnList = "date_debut, date_fin"),
                @Index(name = "idx_planning_statut", columnList = "statut")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_planning_syndicat_periode", columnNames = {"syndicat_id", "date_debut", "date_fin"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningGarde {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Syndicat qui gère ce planning
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syndicat_id", nullable = false)
    private Syndicat syndicat;

    /**
     * Titre du planning (ex: "Gardes Décembre 2024")
     */
    @Column(name = "titre", nullable = false, length = 200)
    private String titre;

    /**
     * Description / Notes
     */
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * Date de début de la période
     */
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    /**
     * Date de fin de la période
     */
    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    /**
     * Statut du planning
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    @Builder.Default
    private StatutPlanning statut = StatutPlanning.BROUILLON;

    /**
     * Date de publication prévue (pour publication automatique)
     */
    @Column(name = "date_publication_prevue")
    private LocalDateTime datePublicationPrevue;

    /**
     * Date de publication effective
     */
    @Column(name = "date_publication")
    private LocalDateTime datePublication;

    /**
     * Publication automatique activée
     */
    @Column(name = "publication_auto")
    @Builder.Default
    private Boolean publicationAuto = false;

    /**
     * Notifier les pharmacies à la publication
     */
    @Column(name = "notifier_pharmacies")
    @Builder.Default
    private Boolean notifierPharmacies = true;

    /**
     * Notifier par SMS
     */
    @Column(name = "notifier_sms")
    @Builder.Default
    private Boolean notifierSms = false;

    /**
     * Notifier par Email
     */
    @Column(name = "notifier_email")
    @Builder.Default
    private Boolean notifierEmail = true;

    /**
     * Créé par (ID utilisateur)
     */
    @Column(name = "cree_par_id")
    private UUID creeParId;

    /**
     * Nom du créateur (pour affichage)
     */
    @Column(name = "cree_par_nom", length = 100)
    private String creeParNom;

    /**
     * Publié par (ID utilisateur)
     */
    @Column(name = "publie_par_id")
    private UUID publieParId;

    /**
     * Liste des gardes du planning
     */
    @OneToMany(mappedBy = "planning", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Garde> gardes = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Vérifier si le planning est modifiable
     */
    public boolean isModifiable() {
        return statut == StatutPlanning.BROUILLON
                || statut == StatutPlanning.EN_VALIDATION
                || statut == StatutPlanning.PUBLIE;
    }

    /**
     * Vérifier si le planning est publié
     */
    public boolean isPublie() {
        return statut == StatutPlanning.PUBLIE;
    }

    /**
     * Vérifier si la publication automatique est due
     */
    public boolean isPublicationAutoDue() {
        if (!publicationAuto || datePublicationPrevue == null) return false;
        return LocalDateTime.now().isAfter(datePublicationPrevue) && statut == StatutPlanning.VALIDE;
    }

    /**
     * Nombre de jours dans la période
     */
    public long getNombreJours() {
        return java.time.temporal.ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;
    }

    /**
     * Nombre de gardes planifiées
     */
    public int getNombreGardes() {
        return gardes.size();
    }

    /**
     * Publier le planning
     */
    public void publier(UUID publieParId) {
        this.statut = StatutPlanning.PUBLIE;
        this.datePublication = LocalDateTime.now();
        this.publieParId = publieParId;
    }

    /**
     * Passer en validation
     */
    public void soumettrePourValidation() {
        if (statut == StatutPlanning.BROUILLON) {
            this.statut = StatutPlanning.EN_VALIDATION;
        }
    }

    /**
     * Valider le planning
     */
    public void valider() {
        if (statut == StatutPlanning.EN_VALIDATION) {
            this.statut = StatutPlanning.VALIDE;
        }
    }

    /**
     * Rejeter le planning
     */
    public void rejeter() {
        if (statut == StatutPlanning.EN_VALIDATION) {
            this.statut = StatutPlanning.BROUILLON;
        }
    }

    /**
     * Archiver le planning
     */
    public void archiver() {
        this.statut = StatutPlanning.ARCHIVE;
    }

    /**
     * Ajouter une garde au planning
     */
    public void addGarde(Garde garde) {
        gardes.add(garde);
        garde.setPlanning(this);
    }

    /**
     * Retirer une garde du planning
     */
    public void removeGarde(Garde garde) {
        gardes.remove(garde);
        garde.setPlanning(null);
    }
}
