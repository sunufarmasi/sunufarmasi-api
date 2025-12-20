package sn.sunufarmasi.syndicat.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

/**
 * DTO Request pour la modification d'un syndicat
 *
 * @author WeCan
 * @since 1.0.0
 */
public record UpdateSyndicatRequest(

        @Size(max = 200, message = "Le nom ne doit pas dépasser 200 caractères")
        String nom,

        @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
        String description,

        // ═══════════════════════════════════════════════════════════
        // CONTACT
        // ═══════════════════════════════════════════════════════════

        @Pattern(regexp = "^\\+221[0-9]{9}$", message = "Format téléphone invalide (ex: +221771234567)")
        String telephone,

        @Pattern(regexp = "^(\\+221[0-9]{9})?$", message = "Format téléphone secondaire invalide")
        String telephoneSecondaire,

        @Email(message = "Format email invalide")
        @Size(max = 100, message = "L'email ne doit pas dépasser 100 caractères")
        String email,

        @Size(max = 500, message = "L'adresse ne doit pas dépasser 500 caractères")
        String adresse,

        // ═══════════════════════════════════════════════════════════
        // RESPONSABLE
        // ═══════════════════════════════════════════════════════════

        UUID responsableId,

        @Size(max = 200, message = "Le nom du responsable ne doit pas dépasser 200 caractères")
        String nomResponsable

) {}
