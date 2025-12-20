package sn.sunufarmasi.stock.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO Request pour ajouter un produit au stock d'une pharmacie
 *
 * @author WeCan
 * @since 1.0.0
 */
public record AjouterStockRequest(

        @NotNull(message = "L'ID du produit est obligatoire")
        UUID produitId,

        @NotNull(message = "La quantité initiale est obligatoire")
        @Min(value = 0, message = "La quantité doit être positive ou nulle")
        Integer quantiteInitiale,

        // Prix
        @DecimalMin(value = "0", message = "Le prix de vente TTC doit être positif")
        BigDecimal prixVenteTTC,

        @DecimalMin(value = "0", message = "Le prix d'achat HT doit être positif")
        BigDecimal prixAchatHT,

        // Seuils
        @Min(value = 0, message = "Le seuil d'alerte doit être positif")
        Integer seuilAlerte,

        @Min(value = 0, message = "Le seuil de réappro doit être positif")
        Integer seuilReappro,

        @Min(value = 0, message = "La quantité optimale doit être positive")
        Integer quantiteOptimale,

        // Péremption
        LocalDate datePeremption,

        @Min(value = 1, message = "L'alerte péremption doit être positive")
        Integer alertePeremptionJours,

        // Emplacement
        @Size(max = 50, message = "L'emplacement ne doit pas dépasser 50 caractères")
        String emplacement,

        @Size(max = 20, message = "Le rayon ne doit pas dépasser 20 caractères")
        String rayon,

        @Size(max = 20, message = "L'étagère ne doit pas dépasser 20 caractères")
        String etagere,

        // Lot (optionnel pour l'initialisation)
        @Size(max = 50, message = "Le numéro de lot ne doit pas dépasser 50 caractères")
        String numeroLot

) {}
