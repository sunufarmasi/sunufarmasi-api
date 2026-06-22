package sn.sunufarmasi.pharmacie.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sn.sunufarmasi.pharmacie.enums.TypePharmacien;
import sn.sunufarmasi.pharmacie.enums.PlanAbonnementPharmacie;
import sn.sunufarmasi.pharmacie.enums.StatutAbonnement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entité représentant un pharmacien (propriétaire ou gérant)
 *
 * Le pharmacien souscrit un abonnement pour gérer sa/ses pharmacie(s).
 * Il paie l'abonnement mensuel selon son plan.
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(name = "pharmaciens", indexes = {
        @Index(name = "idx_pharmacien_telephone", columnList = "telephone"),
        @Index(name = "idx_pharmacien_email", columnList = "email"),
        @Index(name = "idx_pharmacien_numero_ordre", columnList = "numero_ordre_national")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Pharmacien {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS PERSONNELLES
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(unique = true, length = 100)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String telephone;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(length = 1)
    private String sexe;

    @Column(length = 500)
    private String photoUrl;

    // ═══════════════════════════════════════════════════════════
    // AUTHENTIFICATION
    // ═══════════════════════════════════════════════════════════

    @Column(name = "mot_de_passe_hash", nullable = false)
    private String motDePasseHash;

    // ═══════════════════════════════════════════════════════════
    // DIPLÔMES & AUTORISATIONS
    // ═══════════════════════════════════════════════════════════

    @Column(length = 50)
    private String numeroOrdreNational;

    @Column(length = 200)
    private String universiteFormation;

    @Column(name = "annee_diplome")
    private Integer anneeDiplome;

    @Column(length = 500)
    private String diplomeUrl;

    @Column(length = 500)
    private String carteOrdreUrl;

    @Column(length = 500)
    private String cinUrl;

    // ═══════════════════════════════════════════════════════════
    // TYPE PHARMACIEN
    // ═══════════════════════════════════════════════════════════

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypePharmacien type;

    // ═══════════════════════════════════════════════════════════
    // ABONNEMENT (Le pharmacien paie pour gérer sa/ses pharmacie(s))
    // ═══════════════════════════════════════════════════════════

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanAbonnementPharmacie plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutAbonnement statutAbonnement;

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
    // LIMITATIONS PLAN
    // ═══════════════════════════════════════════════════════════

    @Column(name = "nombre_employes_max")
    private Integer nombreEmployesMax;

    @Column(name = "nombre_pharmacies_max")
    private Integer nombrePharmaciesMax;

    @Column(name = "nombre_pharmacies_actuelles")
    private Integer nombrePharmaciesActuelles = 0;

    // ═══════════════════════════════════════════════════════════
    // RELATIONS
    // ═══════════════════════════════════════════════════════════

    @OneToMany(mappedBy = "pharmacienProprietaire", fetch = FetchType.LAZY)
    private List<Pharmacie> pharmacies;          // Liste de SES pharmacies

    // ═══════════════════════════════════════════════════════════
    // STATUT
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false)
    private Boolean actif = true;

    @Column(name = "valide_par_ordre", nullable = false)
    private Boolean valideParOrdre = false;

    @Column(name = "compte_verifie", nullable = false)
    private Boolean compteVerifie = false;

    // ═══════════════════════════════════════════════════════════
    // PRÉFÉRENCES NOTIFICATIONS
    // ═══════════════════════════════════════════════════════════

    @Column(name = "notifications_email")
    private Boolean notificationsEmail = true;

    @Column(name = "notifications_sms")
    private Boolean notificationsSms = true;

    @Column(name = "notifications_push")
    private Boolean notificationsPush = true;

    // ═══════════════════════════════════════════════════════════
    // MÉTADONNÉES
    // ═══════════════════════════════════════════════════════════

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    @Column(name = "date_verification_compte")
    private LocalDateTime dateVerificationCompte;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    public String getNomComplet() {
        return this.prenom + " " + this.nom;
    }

    public boolean abonnementActif() {
        if (this.essaiGratuit && this.dateFinEssai != null && this.dateFinEssai.isAfter(LocalDate.now())) {
            return true;
        }
        return this.statutAbonnement == StatutAbonnement.ACTIF
                && this.dateFinAbonnement != null
                && this.dateFinAbonnement.isAfter(LocalDate.now());
    }

    public boolean peutCreerPharmacie() {
        if (plan == PlanAbonnementPharmacie.ENTERPRISE) {
            return true;
        }
        int actuelles = this.nombrePharmaciesActuelles == null ? 0 : this.nombrePharmaciesActuelles;
        int max = this.nombrePharmaciesMax == null ? 1 : this.nombrePharmaciesMax;
        return actuelles < max;
    }

    public void ajouterPharmacie() {
        this.nombrePharmaciesActuelles = (this.nombrePharmaciesActuelles == null ? 0 : this.nombrePharmaciesActuelles) + 1;
    }

    public void retirerPharmacie() {
        if (this.nombrePharmaciesActuelles != null && this.nombrePharmaciesActuelles > 0) {
            this.nombrePharmaciesActuelles--;
        }
    }

    public boolean peutGerer() {
        return this.actif
                && this.valideParOrdre
                && this.compteVerifie
                && abonnementActif();
    }

    public boolean estProprietaire() {
        return this.type == TypePharmacien.PROPRIETAIRE;
    }

    public void enregistrerConnexion() {
        this.derniereConnexion = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.actif == null) this.actif = true;
        if (this.valideParOrdre == null) this.valideParOrdre = false;
        if (this.compteVerifie == null) this.compteVerifie = false;
        if (this.type == null) this.type = TypePharmacien.PROPRIETAIRE;
        if (this.notificationsEmail == null) this.notificationsEmail = true;
        if (this.notificationsSms == null) this.notificationsSms = true;
        if (this.notificationsPush == null) this.notificationsPush = true;
        if (this.nombrePharmaciesActuelles == null) this.nombrePharmaciesActuelles = 0;

        if (this.plan == null) {
            this.plan = PlanAbonnementPharmacie.TRIAL;
            this.montantMensuel = this.plan.getMontantMensuel();
            this.nombreEmployesMax = this.plan.getNombreEmployesMax();
            this.nombrePharmaciesMax = this.plan.getNombrePharmaciesMax();
        }

        if (this.statutAbonnement == null) {
            this.statutAbonnement = StatutAbonnement.EN_ATTENTE;
        }

        if (this.essaiGratuit == null) {
            this.essaiGratuit = true;
        }

        if (this.essaiGratuit && this.dateDebutEssai == null) {
            this.dateDebutEssai = LocalDate.now();
            this.dateFinEssai = LocalDate.now().plusMonths(1);
            this.statutAbonnement = StatutAbonnement.ACTIF;
        }
    }
}