package sn.sunufarmasi.vente.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO Request pour une ligne de vente
 *
 * @author WeCan
 * @since 1.0.0
 */
public record LigneVenteRequest(

        @NotNull(message = "L'ID du produit est obligatoire")
        UUID produitId,

        @NotNull(message = "La quantité est obligatoire")
        @Min(value = 1, message = "La quantité doit être au moins 1")
        Integer quantite,

        @Min(value = 0, message = "La quantité gratuite doit être positive")
        Integer quantiteGratuite,

        // Prix (optionnel - utilise le prix du stock si non fourni)
        @DecimalMin(value = "0", message = "Le prix unitaire doit être positif")
        BigDecimal prixUnitaireTTC,

        // Remise ligne (optionnel)
        @DecimalMin(value = "0", message = "Le pourcentage de remise doit être positif")
        @DecimalMax(value = "100", message = "Le pourcentage de remise ne peut pas dépasser 100%")
        BigDecimal remisePourcentage,

        // Lot et péremption (optionnel)
        @Size(max = 50, message = "Le numéro de lot ne doit pas dépasser 50 caractères")
        String numeroLot,

        LocalDate datePeremption,

        // Posologie (optionnel)
        @Size(max = 500, message = "La posologie ne doit pas dépasser 500 caractères")
        String posologie

) {}
