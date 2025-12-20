package sn.sunufarmasi.employe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sn.sunufarmasi.employe.enums.StatutEmploye;
import sn.sunufarmasi.employe.enums.TypePermission;
import sn.sunufarmasi.pharmacie.entity.Pharmacie;
import sn.sunufarmasi.pharmacie.entity.Pharmacien;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entité représentant un employé (vendeur) d'une pharmacie
 *
 * Un employé est créé par un pharmacien pour travailler dans sa pharmacie.
 * Il dispose de permissions granulaires et s'authentifie par username/password.
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(name = "employes", indexes = {
        @Index(name = "idx_employe_code", columnList = "code"),
        @Index(name = "idx_employe_username", columnList = "username"),
        @Index(name = "idx_employe_pharmacie", columnList = "pharmacie_id"),
        @Index(name = "idx_employe_pharmacien", columnList = "pharmacien_id"),
        @Index(name = "idx_employe_statut", columnList = "statut"),
        @Index(name = "idx_employe_telephone", columnList = "telephone")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ═══════════════════════════════════════════════════════════
    // IDENTIFIANTS
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, unique = true, length = 30)
    private String code;                         // EMP-PHAR001-001 (auto-généré)

    @Column(nullable = false, unique = true, length = 50)
    private String username;                     // Auto-généré ou défini par pharmacien

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS PERSONNELLES
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, length = 20)
    private String telephone;

    @Column(length = 100)
    private String email;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(length = 1)
    private String sexe;                         // M ou F

    @Column(length = 500)
    private String photoUrl;

    @Column(length = 500)
    private String adresse;

    // ═══════════════════════════════════════════════════════════
    // AUTHENTIFICATION
    // ═══════════════════════════════════════════════════════════

    @Column(name = "mot_de_passe_hash", nullable = false)
    private String motDePasseHash;

    @Column(name = "doit_changer_mdp")
    private Boolean doitChangerMotDePasse = true; // Force le changement au 1er login

    // ═══════════════════════════════════════════════════════════
    // RELATIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Pharmacie où travaille l'employé
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacie_id", nullable = false)
    private Pharmacie pharmacie;

    /**
     * Pharmacien qui a créé/gère cet employé
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacien_id", nullable = false)
    private Pharmacien pharmacien;

    // ═══════════════════════════════════════════════════════════
    // EMPLOI
    // ═══════════════════════════════════════════════════════════

    @Column(length = 100)
    private String poste;                        // Vendeur, Caissier, Assistant, etc.

    @Column(name = "date_embauche")
    private LocalDate dateEmbauche;

    @Column(name = "date_fin_contrat")
    private LocalDate dateFinContrat;            // Si CDD

    // ═══════════════════════════════════════════════════════════
    // PERMISSIONS (stockées en JSON)
    // ═══════════════════════════════════════════════════════════

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "employe_permissions",
            joinColumns = @JoinColumn(name = "employe_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    private Set<TypePermission> permissions = new HashSet<>();

    // ═══════════════════════════════════════════════════════════
    // STATUT
    // ═══════════════════════════════════════════════════════════

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutEmploye statut;

    @Column(length = 500)
    private String motifSuspension;              // Motif si suspendu/inactif

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

    @Column(name = "nombre_connexions")
    private Integer nombreConnexions = 0;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Retourne le nom complet de l'employé
     */
    public String getNomComplet() {
        return this.prenom + " " + this.nom;
    }

    /**
     * Vérifie si l'employé est actif et peut se connecter
     */
    public boolean peutSeConnecter() {
        return this.statut == StatutEmploye.ACTIF;
    }

    /**
     * Vérifie si l'employé a une permission spécifique
     */
    public boolean aPermission(TypePermission permission) {
        return this.permissions != null && this.permissions.contains(permission);
    }

    /**
     * Vérifie si l'employé a toutes les permissions spécifiées
     */
    public boolean aToutesPermissions(TypePermission... permissionsRequises) {
        if (this.permissions == null) {
            return false;
        }
        for (TypePermission p : permissionsRequises) {
            if (!this.permissions.contains(p)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Vérifie si l'employé a au moins une des permissions spécifiées
     */
    public boolean aUnePermission(TypePermission... permissionsRequises) {
        if (this.permissions == null) {
            return false;
        }
        for (TypePermission p : permissionsRequises) {
            if (this.permissions.contains(p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ajoute une permission
     */
    public void ajouterPermission(TypePermission permission) {
        if (this.permissions == null) {
            this.permissions = new HashSet<>();
        }
        this.permissions.add(permission);
    }

    /**
     * Retire une permission
     */
    public void retirerPermission(TypePermission permission) {
        if (this.permissions != null) {
            this.permissions.remove(permission);
        }
    }

    /**
     * Définit toutes les permissions d'un coup
     */
    public void setToutesPermissions(Set<TypePermission> nouvellesPermissions) {
        this.permissions = nouvellesPermissions != null ? new HashSet<>(nouvellesPermissions) : new HashSet<>();
    }

    /**
     * Applique les permissions par défaut
     */
    public void appliquerPermissionsDefaut() {
        this.permissions = new HashSet<>();
        for (TypePermission p : TypePermission.getPermissionsDefaut()) {
            this.permissions.add(p);
        }
    }

    /**
     * Enregistre une connexion
     */
    public void enregistrerConnexion() {
        this.derniereConnexion = LocalDateTime.now();
        this.nombreConnexions = (this.nombreConnexions == null ? 0 : this.nombreConnexions) + 1;
    }

    /**
     * Vérifie si l'employé peut effectuer des ventes
     */
    public boolean peutVendre() {
        return peutSeConnecter() && aPermission(TypePermission.VENDRE);
    }

    /**
     * Vérifie si l'employé peut gérer le stock
     */
    public boolean peutGererStock() {
        return peutSeConnecter() && aPermission(TypePermission.MODIFIER_STOCK);
    }

    @PrePersist
    protected void onCreate() {
        if (this.statut == null) {
            this.statut = StatutEmploye.ACTIF;
        }
        if (this.doitChangerMotDePasse == null) {
            this.doitChangerMotDePasse = true;
        }
        if (this.permissions == null || this.permissions.isEmpty()) {
            appliquerPermissionsDefaut();
        }
        if (this.nombreConnexions == null) {
            this.nombreConnexions = 0;
        }
        if (this.dateEmbauche == null) {
            this.dateEmbauche = LocalDate.now();
        }
    }
}
