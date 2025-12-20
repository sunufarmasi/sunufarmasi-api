package sn.sunufarmasi.stock.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO Request pour modifier les paramètres d'un produit en stock
 *
 * @author WeCan
 * @since 1.0.0
 */
public record UpdateStockParamsRequest(

        // Prix
        @DecimalMin(value = "0", message = "Le prix de vente TTC doit être positif")
        BigDecimal prixVenteTTC,

        @DecimalMin(value = "0", message = "Le prix d'achat HT doit être positif")
        BigDecimal prixAchatHT,

        @DecimalMin(value = "0", message = "La marge doit être positive")
        @DecimalMax(value = "100", message = "La marge ne peut pas dépasser 100%")
        BigDecimal margePourcentage,

        // Seuils
        @Min(value = 0, message = "Le seuil d'alerte doit être positif")
        Integer seuilAlerte,

        @Min(value = 0, message = "Le seuil de réappro doit être positif")
        Integer seuilReappro,

        @Min(value = 0, message = "La quantité optimale doit être positive")
        Integer quantiteOptimale,

        // Péremption
        LocalDate datePeremptionProche,

        @Min(value = 1, message = "L'alerte péremption doit être positive")
        Integer alertePeremptionJours,

        // Emplacement
        @Size(max = 50, message = "L'emplacement ne doit pas dépasser 50 caractères")
        String emplacement,

        @Size(max = 20, message = "Le rayon ne doit pas dépasser 20 caractères")
        String rayon,

        @Size(max = 20, message = "L'étagère ne doit pas dépasser 20 caractères")
        String etagere,

        // Paramètres
        Boolean estActif,
        Boolean venteAutorisee,
        Boolean commandeAuto

) {}
