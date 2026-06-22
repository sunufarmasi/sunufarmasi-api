package sn.sunufarmasi.pharmacie.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO Request simplifié pour créer une pharmacie (Admin)
 * Le Super Admin crée la pharmacie et l'assigne à un syndicat.
 * Champs obligatoires réduits.
 *
 * @author WeCan
 * @since 1.0.0
 */
public record AdminCreatePharmacieRequest(

        @NotBlank(message = "Le nom de la pharmacie est obligatoire")
        @Size(max = 200)
        String nom,

        @NotNull(message = "La commune est obligatoire")
        UUID communeId,

        @NotBlank(message = "Le téléphone est obligatoire")
        String telephone,

        String adresseComplete,
        String quartier,
        Double latitude,
        Double longitude,
        String email,
        String siteWeb,
        String numeroAgrementMinistere,
        String numeroOrdre,
        LocalDate dateOuverture,
        String horaires,
        Boolean accepteCommandes,
        Boolean proposeLivraison,
        Integer rayonLivraisonKm,

        // Optionnel : assigner directement à un syndicat
        UUID syndicatId,

        // Optionnel : pharmacien propriétaire (sinon on en crée un générique)
        UUID pharmacienId

) {}
