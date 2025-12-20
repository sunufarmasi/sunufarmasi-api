package sn.sunufarmasi.pharmacie.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO Request pour créer une pharmacie
 *
 * Le pharmacien propriétaire est automatiquement celui connecté
 *
 * @author WeCan
 * @since 1.0.0
 */
public record CreatePharmacieRequest(

        @NotBlank(message = "Le nom de la pharmacie est obligatoire")
        @Size(max = 200, message = "Le nom ne doit pas dépasser 200 caractères")
        String nom,

        @Size(max = 200, message = "La raison sociale ne doit pas dépasser 200 caractères")
        String raisonSociale,

        @NotNull(message = "La commune est obligatoire")
        UUID communeId,

        @NotBlank(message = "L'adresse complète est obligatoire")
        @Size(max = 500, message = "L'adresse ne doit pas dépasser 500 caractères")
        String adresseComplete,

        @Size(max = 100, message = "Le quartier ne doit pas dépasser 100 caractères")
        String quartier,

        @NotNull(message = "La latitude est obligatoire")
        @DecimalMin(value = "-90.0", message = "La latitude doit être >= -90")
        @DecimalMax(value = "90.0", message = "La latitude doit être <= 90")
        Double latitude,

        @NotNull(message = "La longitude est obligatoire")
        @DecimalMin(value = "-180.0", message = "La longitude doit être >= -180")
        @DecimalMax(value = "180.0", message = "La longitude doit être <= 180")
        Double longitude,

        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(regexp = "^\\+221[0-9]{9}$", message = "Format téléphone invalide (ex: +221338234567)")
        String telephone,

        String telephoneSecondaire,

        @Email(message = "Format email invalide")
        @Size(max = 100, message = "L'email ne doit pas dépasser 100 caractères")
        String email,

        @Size(max = 200, message = "Le site web ne doit pas dépasser 200 caractères")
        String siteWeb,

        @NotBlank(message = "Le numéro d'agrément ministère est obligatoire")
        @Size(max = 50, message = "Le numéro d'agrément ne doit pas dépasser 50 caractères")
        String numeroAgrementMinistere,

        @NotBlank(message = "Le numéro d'ordre est obligatoire")
        @Size(max = 50, message = "Le numéro d'ordre ne doit pas dépasser 50 caractères")
        String numeroOrdre,

        @NotNull(message = "La date d'ouverture est obligatoire")
        @PastOrPresent(message = "La date d'ouverture ne peut pas être dans le futur")
        LocalDate dateOuverture,

        String horaires,

        Boolean accepteCommandes,

        Boolean proposeLivraison,

        @Min(value = 1, message = "Le rayon de livraison doit être >= 1 km")
        @Max(value = 100, message = "Le rayon de livraison doit être <= 100 km")
        Integer rayonLivraisonKm

) {
    /**
     * Constructeur avec valeurs par défaut
     */
    public CreatePharmacieRequest {
        if (accepteCommandes == null) {
            accepteCommandes = true;
        }
        if (proposeLivraison == null) {
            proposeLivraison = false;
        }
    }
}