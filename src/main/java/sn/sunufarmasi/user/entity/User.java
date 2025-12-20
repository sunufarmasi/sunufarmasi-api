package sn.sunufarmasi.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité User - Utilisateurs avec login/password (Pharmaciens, Syndicats, Admins, Employés)
 * Les Patients utilisent le système OTP séparé
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_telephone", columnList = "telephone"),
    @Index(name = "idx_user_role", columnList = "role"),
    @Index(name = "idx_user_statut", columnList = "statut")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false, length = 50)
    private String prenom;

    @Column(length = 20)
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RoleUser role = RoleUser.CLIENT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutUser statut = StatutUser.EN_ATTENTE;

    @Column(name = "email_verifie")
    @Builder.Default
    private Boolean emailVerifie = false;

    @Column(name = "telephone_verifie")
    @Builder.Default
    private Boolean telephoneVerifie = false;

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    @Column(name = "tentatives_connexion")
    @Builder.Default
    private Integer tentativesConnexion = 0;

    @Column(name = "compte_verrouille_jusqu")
    private LocalDateTime compteVerrouilleJusqu;

    @Column(length = 255)
    private String avatar;

    @Column(length = 200)
    private String adresse;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════════════════════════════════════════
    // ENUMS
    // ═══════════════════════════════════════════════════════════════════════════

    public enum RoleUser {
        ADMIN("Administrateur système"),
        ADMIN_SYNDICAT("Administrateur syndicat"),
        PHARMACIEN("Pharmacien titulaire"),
        EMPLOYE("Employé de pharmacie"),
        CLIENT("Client"),
        LIVREUR("Livreur");

        private final String libelle;

        RoleUser(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    public enum StatutUser {
        EN_ATTENTE("En attente de validation"),
        ACTIF("Actif"),
        INACTIF("Inactif"),
        SUSPENDU("Suspendu"),
        BLOQUE("Bloqué");

        private final String libelle;

        StatutUser(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════════════════════

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public boolean isAdmin() {
        return role == RoleUser.ADMIN;
    }

    public boolean isPharmacien() {
        return role == RoleUser.PHARMACIEN;
    }

    public boolean isActif() {
        return statut == StatutUser.ACTIF;
    }

    public boolean isAccountNonLocked() {
        if (compteVerrouilleJusqu == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(compteVerrouilleJusqu);
    }

    public void incrementerTentativesConnexion() {
        this.tentativesConnexion++;
        if (this.tentativesConnexion >= 5) {
            this.compteVerrouilleJusqu = LocalDateTime.now().plusMinutes(30);
        }
    }

    public void connexionReussie() {
        this.tentativesConnexion = 0;
        this.compteVerrouilleJusqu = null;
        this.derniereConnexion = LocalDateTime.now();
    }

    public boolean canLogin() {
        return isActif() && isAccountNonLocked();
    }
}
