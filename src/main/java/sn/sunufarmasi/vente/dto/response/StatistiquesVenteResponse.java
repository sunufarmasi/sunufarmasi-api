package sn.sunufarmasi.vente.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO Response pour les statistiques de ventes
 *
 * @author WeCan
 * @since 1.0.0
 */
public record StatistiquesVenteResponse(

        // Période
        LocalDate dateDebut,
        LocalDate dateFin,

        // KPIs principaux
        BigDecimal chiffreAffaires,
        Long nombreVentes,
        BigDecimal panierMoyen,
        Integer nombreArticlesVendus,

        // Remises
        BigDecimal totalRemises,
        BigDecimal pourcentageRemiseMoyen,

        // Mutuelles
        BigDecimal montantMutuelle,
        Long nombreVentesMutuelle,

        // Paiements
        Map<String, BigDecimal> ventesParModePaiement,
        Map<String, Long> nombreParModePaiement,

        // Types
        Map<String, BigDecimal> ventesParType,
        Map<String, Long> nombreParType,

        // Vendeurs
        List<StatVendeur> statsVendeurs,

        // Évolution journalière
        List<StatJour> evolutionJournaliere,

        // Top produits
        List<TopProduit> topProduitsQuantite,
        List<TopProduit> topProduitsCA

) {
    public record StatVendeur(
            UUID vendeurId,
            String vendeurNom,
            Long nombreVentes,
            BigDecimal chiffreAffaires,
            BigDecimal panierMoyen
    ) {}

    public record StatJour(
            LocalDate date,
            Long nombreVentes,
            BigDecimal chiffreAffaires
    ) {}

    public record TopProduit(
            UUID produitId,
            String produitNom,
            Integer quantiteVendue,
            BigDecimal chiffreAffaires
    ) {}
}
