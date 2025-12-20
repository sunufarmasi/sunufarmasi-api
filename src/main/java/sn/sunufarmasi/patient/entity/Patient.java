package sn.sunufarmasi.patient.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sn.sunufarmasi.localisation.entity.Commune;
import sn.sunufarmasi.shared.enums.UserType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity représentant un patient avec compte complet
 *
 * MODE PUBLIC (sans compte) :
 * - Nom + Localisation stockés localement dans l'app mobile
 * - Pas de Patient créé en base
 * - Accès direct aux endpoints publics
 *
 * MODE COMPTE (avec inscription) :
 * - Patient créé en base avec TOUS les champs obligatoires
 * - Email + Password + Téléphone requis
 * - Accès aux fonctionnalités premium (favoris, historique)
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(
        name = "patients",
        indexes = {
                @Index(name = "idx_patients_email", columnList = "email"),
                @Index(name = "idx_patients_telephone", columnList = "telephone"),
                @Index(name = "idx_patients_commune", columnList = "commune_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Email (OPTIONNEL - pas obligatoire pour l'inscription)
     */
    @Column(name = "email", unique = true, length = 255)
    private String email;

    /**
     * Numéro de téléphone (OBLIGATOIRE - format international)
     * Format: +221XXXXXXXXX
     * C'est l'identifiant principal pour l'authentification
     */
    @Column(name = "telephone", nullable = false, unique = true, length = 20)
    private String telephone;

    /**
     * Nom complet (OBLIGATOIRE)
     */
    @Column(name = "nom_complet", nullable = false, length = 255)
    private String nomComplet;

    /**
     * Date de naissance
     */
    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    /**
     * Sexe (M ou F)
     */
    @Column(name = "sexe", length = 1)
    private String sexe;

    /**
     * Adresse complète
     */
    @Column(name = "adresse", length = 500)
    private String adresse;

    /**
     * Photo de profil URL
     */
    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    /**
     * Type d'utilisateur (pour Security)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    @Builder.Default
    private UserType userType = UserType.PATIENT;

    /**
     * Email vérifié (via OTP)
     */
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    /**
     * Téléphone vérifié (via OTP)
     */
    @Column(name = "telephone_verified", nullable = false)
    @Builder.Default
    private boolean telephoneVerified = false;

    /**
     * Compte actif
     */
    @Column(name = "actif", nullable = false)
    @Builder.Default
    private boolean actif = true;

    /**
     * Date de dernière connexion
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * Date de création
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date de dernière mise à jour
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════════════════════════
    // RELATIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Commune de résidence
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_id")
    private Commune commune;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Vérifier si le patient peut se connecter
     * Tous les patients ont un compte complet par définition
     */
    public boolean canLogin() {
        return actif && emailVerified;
    }

    /**
     * Mettre à jour la dernière connexion
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * Obtenir le prénom (premier mot du nom complet)
     */
    public String getPrenom() {
        if (nomComplet == null || nomComplet.isEmpty()) {
            return null;
        }
        String[] parts = nomComplet.split(" ");
        return parts[0];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Patient patient)) return false;
        return id != null && id.equals(patient.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", nomComplet='" + nomComplet + '\'' +
                ", actif=" + actif +
                '}';
    }
}