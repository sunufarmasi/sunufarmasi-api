package sn.sunufarmasi.stock.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO Request pour un ajustement d'inventaire
 *
 * @author WeCan
 * @since 1.0.0
 */
public record AjustementStockRequest(

        @NotNull(message = "L'ID du produit est obligatoire")
        UUID produitId,

        @NotNull(message = "La nouvelle quantité est obligatoire")
        @Min(value = 0, message = "La quantité doit être positive ou nulle")
        Integer nouvelleQuantite,

        @NotBlank(message = "Le motif est obligatoire pour un ajustement")
        @Size(max = 500, message = "Le motif ne doit pas dépasser 500 caractères")
        String motif,

        // Mise à jour optionnelle de la péremption
        LocalDate datePeremption,

        @Size(max = 1000, message = "Les notes ne doivent pas dépasser 1000 caractères")
        String notes

) {}
