package sn.sunufarmasi.stock.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO Response pour les alertes de stock
 *
 * @author WeCan
 * @since 1.0.0
 */
public record AlerteStockResponse(

        // Résumé
        ResumeAlertes resume,

        // Détails
        List<ProduitAlerte> ruptures,
        List<ProduitAlerte> stocksBas,
        List<ProduitAlerte> aReapprovisionner,
        List<ProduitAlerte> perimes,
        List<ProduitAlerte> bientotPerimes

) {
    /**
     * Résumé des alertes
     */
    public record ResumeAlertes(
            int totalAlertes,
            int nombreRuptures,
            int nombreStocksBas,
            int nombreAReapprovisionner,
            int nombrePerimes,
            int nombreBientotPerimes,
            BigDecimal valeurPerimes
    ) {}

    /**
     * Détail d'un produit en alerte
     */
    public record ProduitAlerte(
            UUID produitPharmacieId,
            UUID produitId,
            String code,
            String nom,
            String nomComplet,
            Integer quantiteStock,
            Integer seuilAlerte,
            Integer seuilReappro,
            LocalDate datePeremption,
            Integer joursRestants,
            BigDecimal prixAchatHT,
            BigDecimal valeurStock,
            String emplacement,
            String typeAlerte
    ) {}

    /**
     * Créer une réponse vide
     */
    public static AlerteStockResponse vide() {
        return new AlerteStockResponse(
                new ResumeAlertes(0, 0, 0, 0, 0, 0, BigDecimal.ZERO),
                List.of(), List.of(), List.of(), List.of(), List.of()
        );
    }
}
