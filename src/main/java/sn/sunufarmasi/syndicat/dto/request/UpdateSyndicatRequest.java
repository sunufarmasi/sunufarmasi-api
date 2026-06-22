package sn.sunufarmasi.syndicat.dto.request;

import jakarta.validation.constraints.*;
import sn.sunufarmasi.syndicat.enums.TypeSyndicat;

import java.util.List;
import java.util.UUID;

/**
 * DTO Request pour la modification d'un syndicat
 */
public record UpdateSyndicatRequest(

        @Size(max = 200)
        String nom,

        @Size(max = 500)
        String description,

        // ── Contact ──────────────────────────────────────────────
        @Pattern(regexp = "^(\\+221[0-9]{9})?$")
        String telephone,

        @Pattern(regexp = "^(\\+221[0-9]{9})?$")
        String telephoneSecondaire,

        @Email
        @Size(max = 100)
        String email,

        @Size(max = 500)
        String adresse,

        // ── Responsable ───────────────────────────────────────────
        UUID responsableId,

        @Size(max = 200)
        String nomResponsable,

        @Pattern(regexp = "^(\\+221[0-9]{9})?$")
        String telephoneResponsable,

        // ── Zone géographique (optionnel — si on veut changer la zone) ──
        TypeSyndicat type,        // nouveau type (COMMUNE, ZONE, DEPARTEMENT, REGION)
        UUID communeId,           // pour COMMUNE
        UUID departementId,       // pour DEPARTEMENT
        UUID regionId,            // pour REGION
        List<UUID> communeIds     // pour ZONE (plusieurs communes)

) {}
