package sn.sunufarmasi.stock.dto.request;

import jakarta.validation.constraints.*;
import sn.sunufarmasi.stock.enums.TypeMouvement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO Request pour une entrée de stock
 *
 * @author WeCan
 * @since 1.0.0
 */
public record EntreeStockRequest(

        @NotNull(message = "L'ID du produit est obligatoire")
        UUID produitId,

        @NotNull(message = "La quantité est obligatoire")
        @Min(value = 1, message = "La quantité doit être au moins 1")
        Integer quantite,

        @NotNull(message = "Le type de mouvement est obligatoire")
        sn.sunufarmasi.stock.enums.TypeMouvement type,

        // Prix
        @DecimalMin(value = "0", message = "Le prix unitaire doit être positif")
        BigDecimal prixUnitaire,

        // Lot et péremption
        @Size(max = 50, message = "Le numéro de lot ne doit pas dépasser 50 caractères")
        String numeroLot,

        LocalDate datePeremption,

        // Référence externe
        @Size(max = 100, message = "La référence ne doit pas dépasser 100 caractères")
        String referenceExterne,

        @Size(max = 200, message = "Le fournisseur ne doit pas dépasser 200 caractères")
        String fournisseur,

        // Notes
        @Size(max = 500, message = "Le motif ne doit pas dépasser 500 caractères")
        String motif,

        @Size(max = 1000, message = "Les notes ne doivent pas dépasser 1000 caractères")
        String notes

) {
    /**
     * Valide que le type est bien une entrée
     */
    public boolean isTypeValide() {
        return type != null && type.isEntree();
    }
}
