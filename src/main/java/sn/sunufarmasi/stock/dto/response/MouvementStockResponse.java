package sn.sunufarmasi.stock.dto.response;


import sn.sunufarmasi.stock.enums.TypeMouvement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO Response pour un mouvement de stock
 *
 * @author WeCan
 * @since 1.0.0
 */
public record MouvementStockResponse(

        UUID id,

        // Type et quantité
        TypeMouvement type,
        String typeLibelle,
        boolean isEntree,
        Integer quantite,
        Integer quantiteAvant,
        Integer quantiteApres,
        int impact,

        // Produit
        UUID produitId,
        String produitCode,
        String produitNom,

        // Lot et péremption
        String numeroLot,
        LocalDate datePeremption,

        // Prix
        BigDecimal prixUnitaire,
        BigDecimal montantTotal,

        // Références
        String referenceExterne,
        String fournisseur,
        String client,

        // Informations
        String motif,
        String notes,

        // Traçabilité
        LocalDateTime dateMouvement,
        UUID effectueParId,
        String effectueParNom,
        String effectueParRole

) {}
