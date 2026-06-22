package sn.sunufarmasi.syndicat.dto.request;

import jakarta.validation.constraints.*;
import sn.sunufarmasi.syndicat.enums.TypeSyndicat;

import java.util.List;
import java.util.UUID;

public record CreateSyndicatRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 200)
        String nom,

        @Size(max = 500)
        String description,

        // ── Authentification ──────────────────────────────────────
        @NotBlank(message = "Le username est obligatoire")
        @Size(min = 4, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Le username ne peut contenir que des lettres, chiffres, points, tirets et underscores")
        String username,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 100)
        String motDePasse,

        // ── Type et zone géographique ─────────────────────────────
        @NotNull(message = "Le type de syndicat est obligatoire")
        TypeSyndicat type,

        /** Pour type = COMMUNE */
        UUID communeId,

        /** Pour type = ZONE (plusieurs communes spécifiques) */
        List<UUID> communeIds,

        /** Pour type = DEPARTEMENT */
        UUID departementId,

        /** Pour type = REGION */
        UUID regionId,

        // ── Contact ───────────────────────────────────────────────
        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(regexp = "^\\+221[0-9]{9}$")
        String telephone,

        @Pattern(regexp = "^(\\+221[0-9]{9})?$")
        String telephoneSecondaire,

        @NotBlank(message = "L'email est obligatoire")
        @Email
        @Size(max = 100)
        String email,

        @Size(max = 500)
        String adresse,

        // ── Responsable ───────────────────────────────────────────
        UUID responsableId,

        @Size(max = 200)
        String nomResponsable

) {
    public boolean isZoneValid() {
        return switch (type) {
            case COMMUNE     -> communeId != null;
            case ZONE        -> communeIds != null && !communeIds.isEmpty();
            case DEPARTEMENT -> departementId != null;
            case REGION      -> regionId != null;
        };
    }
}
