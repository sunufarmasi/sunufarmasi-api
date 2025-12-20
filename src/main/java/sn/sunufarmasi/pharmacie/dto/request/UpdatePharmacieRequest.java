package sn.sunufarmasi.pharmacie.dto.request;

import jakarta.validation.constraints.*;

/**
 * DTO Request pour modifier une pharmacie
 *
 * @author WeCan
 * @since 1.0.0
 */
public record UpdatePharmacieRequest(

        @Size(max = 200, message = "Le nom ne doit pas dépasser 200 caractères")
        String nom,

        @Size(max = 500, message = "L'adresse ne doit pas dépasser 500 caractères")
        String adresseComplete,

        @Size(max = 100, message = "Le quartier ne doit pas dépasser 100 caractères")
        String quartier,

        @DecimalMin(value = "-90.0", message = "La latitude doit être >= -90")
        @DecimalMax(value = "90.0", message = "La latitude doit être <= 90")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "La longitude doit être >= -180")
        @DecimalMax(value = "180.0", message = "La longitude doit être <= 180")
        Double longitude,

        @Pattern(regexp = "^\\+221[0-9]{9}$", message = "Format téléphone invalide")
        String telephone,

        String telephoneSecondaire,

        @Email(message = "Format email invalide")
        String email,

        String siteWeb,

        String horaires,

        Boolean accepteCommandes,

        Boolean proposeLivraison,

        @Min(value = 1, message = "Le rayon de livraison doit être >= 1 km")
        @Max(value = 100, message = "Le rayon de livraison doit être <= 100 km")
        Integer rayonLivraisonKm

) {}