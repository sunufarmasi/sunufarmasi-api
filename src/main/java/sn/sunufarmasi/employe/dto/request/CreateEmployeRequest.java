package sn.sunufarmasi.employe.dto.request;

import jakarta.validation.constraints.*;
import sn.sunufarmasi.employe.enums.TypePermission;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * DTO Request pour la création d'un employé (par Pharmacien)
 *
 * @author WeCan
 * @since 1.0.0
 */
public record CreateEmployeRequest(

        // ═══════════════════════════════════════════════════════════
        // INFORMATIONS PERSONNELLES
        // ═══════════════════════════════════════════════════════════

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
        String prenom,

        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(regexp = "^\\+221[0-9]{9}$", message = "Format téléphone invalide (ex: +221771234567)")
        String telephone,

        @Email(message = "Format email invalide")
        @Size(max = 100, message = "L'email ne doit pas dépasser 100 caractères")
        String email,

        @Past(message = "La date de naissance doit être dans le passé")
        LocalDate dateNaissance,

        @Pattern(regexp = "^[MF]$", message = "Le sexe doit être M ou F")
        String sexe,

        @Size(max = 500, message = "L'adresse ne doit pas dépasser 500 caractères")
        String adresse,

        // ═══════════════════════════════════════════════════════════
        // EMPLOI
        // ═══════════════════════════════════════════════════════════

        @NotNull(message = "L'ID de la pharmacie est obligatoire")
        UUID pharmacieId,

        @Size(max = 100, message = "Le poste ne doit pas dépasser 100 caractères")
        String poste,

        LocalDate dateEmbauche,

        LocalDate dateFinContrat,

        // ═══════════════════════════════════════════════════════════
        // AUTHENTIFICATION
        // ═══════════════════════════════════════════════════════════

        /**
         * Mot de passe initial (optionnel - sera généré si non fourni)
         */
        @Size(min = 6, max = 100, message = "Le mot de passe doit contenir entre 6 et 100 caractères")
        String motDePasse,

        // ═══════════════════════════════════════════════════════════
        // PERMISSIONS
        // ═══════════════════════════════════════════════════════════

        /**
         * Permissions à attribuer (optionnel - permissions par défaut si non fourni)
         */
        Set<TypePermission> permissions

) {}
