package sn.sunufarmasi.stock.dto.response;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO Response pour les statistiques de stock
 *
 * @author WeCan
 * @since 1.0.0
 */
public record StatistiquesStockResponse(

        // Quantités
        Long totalProduits,
        Long produitsEnStock,
        Long produitsEnRupture,
        Long produitsStockBas,

        // Valeurs
        BigDecimal valeurTotaleStock,
        BigDecimal valeurAchats,
        BigDecimal valeurVentes,
        BigDecimal valeurPertes,

        // Mouvements
        Long totalEntrees,
        Long totalSorties,
        Map<String, Long> mouvementsParType,

        // Péremption
        Long produitsPerimes,
        Long produitsBientotPerimes,
        BigDecimal valeurPerimes,

        // Top produits
        java.util.List<TopProduit> topVendus

) {
    /**
     * Top produit vendu
     */
    public record TopProduit(
            String code,
            String nom,
            Integer totalVendu,
            BigDecimal chiffreAffaires
    ) {}
}
