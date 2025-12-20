package sn.sunufarmasi.stock.dto.response;



import sn.sunufarmasi.stock.enums.CategorieProduit;
import sn.sunufarmasi.stock.enums.FormeProduit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO Response pour le stock d'un produit dans une pharmacie
 *
 * @author WeCan
 * @since 1.0.0
 */
public record StockResponse(

        UUID id,

        // Produit
        ProduitInfo produit,

        // Stock
        Integer quantiteStock,
        Integer quantiteReservee,
        Integer quantiteDisponible,

        // Alertes
        Integer seuilAlerte,
        Integer seuilReappro,
        boolean stockBas,
        boolean enRupture,
        boolean aReapprovisionner,

        // Péremption
        LocalDate datePeremptionProche,
        Integer alertePeremptionJours,
        boolean bientotPerime,
        boolean estPerime,

        // Prix pharmacie
        BigDecimal prixVenteTTC,
        BigDecimal prixAchatHT,
        BigDecimal margePourcentage,
        BigDecimal margeUnitaire,
        BigDecimal valeurStock,

        // Emplacement
        String emplacement,

        // Paramètres
        Boolean estActif,
        Boolean venteAutorisee,
        boolean peutEtreVendu,

        // Stats
        Integer totalVendu,
        LocalDateTime derniereVente,
        LocalDateTime derniereEntree

) {
    /**
     * Info produit simplifiée
     */
    public record ProduitInfo(
            UUID id,
            String code,
            String codeBarre,
            String nom,
            String nomComplet,
            String dci,
            CategorieProduit categorie,
            FormeProduit forme,
            String dosage,
            String laboratoire,
            Boolean surOrdonnance,
            String imageUrl
    ) {}
}
