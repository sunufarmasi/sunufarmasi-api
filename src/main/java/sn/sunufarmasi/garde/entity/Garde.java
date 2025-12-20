package sn.sunufarmasi.garde.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sn.sunufarmasi.garde.enums.StatutGarde;
import sn.sunufarmasi.garde.enums.TypeGarde;
import sn.sunufarmasi.localisation.entity.Commune;
import sn.sunufarmasi.localisation.entity.Departement;
import sn.sunufarmasi.pharmacie.entity.Pharmacie;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.UUID;

/**
 * Garde - Une pharmacie de garde pour une SEMAINE donnée dans une ZONE
 *
 * Au Sénégal, les gardes fonctionnent par semaine (samedi au vendredi)
 * et par zone géographique (commune ou département).
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(
        name = "gardes",
        indexes = {
                @Index(name = "idx_garde_planning", columnList = "planning_id"),
                @Index(name = "idx_garde_pharmacie", columnList = "pharmacie_id"),
                @Index(name = "idx_garde_dates", columnList = "date_debut, date_fin"),
                @Index(name = "idx_garde_commune", columnList = "commune_id"),
                @Index(name = "idx_garde_departement", columnList = "departement_id"),
                @Index(name = "idx_garde_type", columnList = "type_garde"),
                @Index(name = "idx_garde_statut", columnList = "statut")
        },
        uniqueConstraints = {
                // Une seule pharmacie de garde par semaine, par zone, par type
                @UniqueConstraint(
                        name = "uk_garde_semaine_zone_type",
                        columnNames = {"date_debut", "date_fin", "commune_id", "departement_id", "type_garde"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Garde {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ═══════════════════════════════════════════════════════════
    // PLANNING PARENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Planning auquel appartient cette garde
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planning_id", nullable = false)
    private PlanningGarde planning;

    // ═══════════════════════════════════════════════════════════
    // PHARMACIE DE GARDE
    // ═══════════════════════════════════════════════════════════

    /**
     * Pharmacie de garde
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacie_id", nullable = false)
    private Pharmacie pharmacie;

    // ═══════════════════════════════════════════════════════════
    // PÉRIODE (SEMAINE)
    // ═══════════════════════════════════════════════════════════

    /**
     * Date de début de la garde (samedi)
     */
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    /**
     * Date de fin de la garde (vendredi suivant)
     */
    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    /**
     * Numéro de semaine dans l'année
     */
    @Column(name = "numero_semaine")
    private Integer numeroSemaine;

    // ═══════════════════════════════════════════════════════════
    // ZONE GÉOGRAPHIQUE
    // ═══════════════════════════════════════════════════════════

    /**
     * Commune de la garde (si garde au niveau commune)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_id")
    private Commune commune;

    /**
     * Département de la garde (si garde au niveau département)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id")
    private Departement departement;

    /**
     * Nom de la zone (pour affichage rapide)
     */
    @Column(name = "zone_nom", length = 100)
    private String zoneNom;

    // ═══════════════════════════════════════════════════════════
    // TYPE ET STATUT
    // ═══════════════════════════════════════════════════════════

    /**
     * Type de garde (JOUR, NUIT, JOUR_ET_NUIT)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_garde", nullable = false, length = 20)
    @Builder.Default
    private TypeGarde typeGarde = TypeGarde.JOUR_ET_NUIT;

    /**
     * Statut de la garde
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    @Builder.Default
    private StatutGarde statut = StatutGarde.PLANIFIEE;

    /**
     * Motif d'annulation (si annulée)
     */
    @Column(name = "motif_annulation", length = 500)
    private String motifAnnulation;

    // ═══════════════════════════════════════════════════════════
    // HORAIRES (optionnel, si différent des horaires par défaut)
    // ═══════════════════════════════════════════════════════════

    /**
     * Heure de début personnalisée
     */
    @Column(name = "heure_debut")
    private LocalTime heureDebut;

    /**
     * Heure de fin personnalisée
     */
    @Column(name = "heure_fin")
    private LocalTime heureFin;

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS COMPLÉMENTAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Notes / Instructions spéciales
     */
    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * La pharmacie a confirmé sa disponibilité
     */
    @Column(name = "confirme_par_pharmacie")
    @Builder.Default
    private Boolean confirmeParPharmacie = false;

    /**
     * Date de confirmation par la pharmacie
     */
    @Column(name = "date_confirmation")
    private LocalDateTime dateConfirmation;

    /**
     * Notification envoyée à la pharmacie
     */
    @Column(name = "notification_envoyee")
    @Builder.Default
    private Boolean notificationEnvoyee = false;

    /**
     * Date d'envoi de la notification
     */
    @Column(name = "date_notification")
    private LocalDateTime dateNotification;

    /**
     * Rappel envoyé (J-1 ou J-7 selon la durée)
     */
    @Column(name = "rappel_envoye")
    @Builder.Default
    private Boolean rappelEnvoye = false;

    /**
     * Date d'envoi du rappel
     */
    @Column(name = "date_rappel")
    private LocalDateTime dateRappel;

    // ═══════════════════════════════════════════════════════════
    // REMPLACEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Est-ce une garde de remplacement?
     */
    @Column(name = "est_remplacement")
    @Builder.Default
    private Boolean estRemplacement = false;

    /**
     * ID de la garde remplacée (si remplacement)
     */
    @Column(name = "garde_remplacee_id")
    private UUID gardeRemplaceeId;

    // ═══════════════════════════════════════════════════════════
    // COORDONNÉES (copie de la pharmacie pour recherche rapide)
    // ═══════════════════════════════════════════════════════════

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // ═══════════════════════════════════════════════════════════
    // AUDIT
    // ═══════════════════════════════════════════════════════════

    /**
     * ID de l'utilisateur qui a créé la garde
     */
    @Column(name = "cree_par_id")
    private UUID creeParId;

    /**
     * Nom de l'utilisateur qui a créé la garde
     */
    @Column(name = "cree_par_nom", length = 100)
    private String creeParNom;

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
     * Vérifier si c'est la semaine en cours
     */
    public boolean estSemaineEnCours() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(dateDebut) && !today.isAfter(dateFin);
    }

    /**
     * Vérifier si c'est la semaine prochaine
     */
    public boolean estSemaineProchaine() {
        LocalDate debutSemaineProchaine = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
        return dateDebut.equals(debutSemaineProchaine);
    }

    /**
     * Vérifier si la garde est passée
     */
    public boolean estPassee() {
        return dateFin.isBefore(LocalDate.now());
    }

    /**
     * Vérifier si la garde est à venir
     */
    public boolean estAVenir() {
        return dateDebut.isAfter(LocalDate.now());
    }

    /**
     * Nombre de jours de garde
     */
    public long getNombreJours() {
        return ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;
    }

    /**
     * Obtenir le libellé de la période
     */
    public String getPeriodeLibelle() {
        return "Du " + dateDebut.toString() + " au " + dateFin.toString();
    }

    /**
     * Obtenir le nom de la zone
     */
    public String getNomZone() {
        if (zoneNom != null) return zoneNom;
        if (commune != null) return commune.getNom();
        if (departement != null) return departement.getNom();
        return "Zone non définie";
    }

    /**
     * Vérifier si une date donnée est dans la période de garde
     */
    public boolean contientDate(LocalDate date) {
        return !date.isBefore(dateDebut) && !date.isAfter(dateFin);
    }

    /**
     * Obtenir les horaires formatés
     */
    public String getHorairesFormates() {
        if (heureDebut != null && heureFin != null) {
            return heureDebut.toString() + " - " + heureFin.toString();
        }
        return typeGarde.getHorairesDefaut();
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES DE TRANSITION D'ÉTAT
    // ═══════════════════════════════════════════════════════════

    /**
     * Confirmer la garde par la pharmacie
     */
    public void confirmer() {
        this.confirmeParPharmacie = true;
        this.dateConfirmation = LocalDateTime.now();
        this.statut = StatutGarde.CONFIRMEE;
    }

    /**
     * Marquer comme notifiée
     */
    public void marquerNotifiee() {
        this.notificationEnvoyee = true;
        this.dateNotification = LocalDateTime.now();
    }

    /**
     * Marquer le rappel comme envoyé
     */
    public void marquerRappelEnvoye() {
        this.rappelEnvoye = true;
        this.dateRappel = LocalDateTime.now();
    }

    /**
     * Démarrer la garde
     */
    public void demarrer() {
        if (statut == StatutGarde.PLANIFIEE || statut == StatutGarde.CONFIRMEE) {
            this.statut = StatutGarde.EN_COURS;
        }
    }

    /**
     * Terminer la garde
     */
    public void terminer() {
        if (statut == StatutGarde.EN_COURS) {
            this.statut = StatutGarde.TERMINEE;
        }
    }

    /**
     * Annuler la garde
     */
    public void annuler(String motif) {
        this.statut = StatutGarde.ANNULEE;
        this.motifAnnulation = motif;
    }

    // ═══════════════════════════════════════════════════════════
    // HOOKS JPA
    // ═══════════════════════════════════════════════════════════

    @PrePersist
    private void prePersist() {
        // Copier les coordonnées de la pharmacie
        if (pharmacie != null && latitude == null) {
            this.latitude = pharmacie.getLatitude();
            this.longitude = pharmacie.getLongitude();
        }

        // Calculer le numéro de semaine
        if (dateDebut != null && numeroSemaine == null) {
            this.numeroSemaine = dateDebut.get(WeekFields.ISO.weekOfWeekBasedYear());
        }

        // Définir le nom de la zone
        if (zoneNom == null) {
            this.zoneNom = getNomZone();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES STATIQUES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Calculer le début de semaine (samedi) pour une date donnée
     * Semaine sénégalaise : samedi à vendredi
     */
    public static LocalDate getDebutSemaine(LocalDate date) {
        DayOfWeek jour = date.getDayOfWeek();
        if (jour == DayOfWeek.SATURDAY) {
            return date;
        } else if (jour == DayOfWeek.SUNDAY) {
            return date.minusDays(1);
        } else {
            // Lundi à Vendredi : retourner le samedi précédent
            return date.with(TemporalAdjusters.previous(DayOfWeek.SATURDAY));
        }
    }

    /**
     * Calculer la fin de semaine (vendredi) pour une date donnée
     */
    public static LocalDate getFinSemaine(LocalDate date) {
        LocalDate debutSemaine = getDebutSemaine(date);
        return debutSemaine.plusDays(6); // Samedi + 6 jours = Vendredi
    }

    /**
     * Créer une garde pour la semaine contenant une date donnée
     */
    public static Garde.GardeBuilder pourSemaineDe(LocalDate date) {
        LocalDate debut = getDebutSemaine(date);
        LocalDate fin = getFinSemaine(date);
        return Garde.builder()
                .dateDebut(debut)
                .dateFin(fin)
                .numeroSemaine(debut.get(WeekFields.ISO.weekOfWeekBasedYear()));
    }
}