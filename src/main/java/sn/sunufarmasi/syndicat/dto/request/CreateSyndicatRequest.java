package sn.sunufarmasi.syndicat.dto.request;

import jakarta.validation.constraints.*;
import sn.sunufarmasi.syndicat.enums.TypeSyndicat;

import java.util.UUID;

/**
 * DTO Request pour la création d'un syndicat (par Admin)
 *
 * @author WeCan
 * @since 1.0.0
 */
public record CreateSyndicatRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 200, message = "Le nom ne doit pas dépasser 200 caractères")
        String nom,

        @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
        String description,

        // ═══════════════════════════════════════════════════════════
        // AUTHENTIFICATION
        // ═══════════════════════════════════════════════════════════

        @NotBlank(message = "Le username est obligatoire")
        @Size(min = 4, max = 50, message = "Le username doit contenir entre 4 et 50 caractères")
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Le username ne peut contenir que des lettres, chiffres, tirets et underscores")
        String username,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
        String motDePasse,

        // ═══════════════════════════════════════════════════════════
        // TYPE ET ZONE GÉOGRAPHIQUE
        // ═══════════════════════════════════════════════════════════

        @NotNull(message = "Le type de syndicat est obligatoire")
        TypeSyndicat type,

        /**
         * ID de la commune (obligatoire si type = COMMUNE)
         */
        UUID communeId,

        /**
         * ID du département (obligatoire si type = DEPARTEMENT)
         */
        UUID departementId,

        // ═══════════════════════════════════════════════════════════
        // CONTACT
        // ═══════════════════════════════════════════════════════════

        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(regexp = "^\\+221[0-9]{9}$", message = "Format téléphone invalide (ex: +221771234567)")
        String telephone,

        @Pattern(regexp = "^(\\+221[0-9]{9})?$", message = "Format téléphone secondaire invalide")
        String telephoneSecondaire,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format email invalide")
        @Size(max = 100, message = "L'email ne doit pas dépasser 100 caractères")
        String email,

        @Size(max = 500, message = "L'adresse ne doit pas dépasser 500 caractères")
        String adresse,

        // ═══════════════════════════════════════════════════════════
        // RESPONSABLE (optionnel)
        // ═══════════════════════════════════════════════════════════

        /**
         * ID du pharmacien responsable (optionnel)
         */
        UUID responsableId,

        /**
         * Nom du responsable si pas de compte pharmacien
         */
        @Size(max = 200, message = "Le nom du responsable ne doit pas dépasser 200 caractères")
        String nomResponsable

) {
    /**
     * Validation personnalisée : soit communeId soit departementId selon le type
     */
    public boolean isZoneValid() {
        if (type == TypeSyndicat.COMMUNE) {
            return communeId != null;
        }
        if (type == TypeSyndicat.DEPARTEMENT) {
            return departementId != null;
        }
        return false;
    }
}
