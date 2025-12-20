package sn.sunufarmasi.stock.dto.response;


import sn.sunufarmasi.stock.enums.CategorieProduit;
import sn.sunufarmasi.stock.enums.FormeProduit;
import sn.sunufarmasi.stock.enums.StatutProduit;
import sn.sunufarmasi.stock.enums.UniteVente;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO Response pour un produit du catalogue
 *
 * @author WeCan
 * @since 1.0.0
 */
public record ProduitResponse(

        UUID id,
        String code,
        String codeBarre,

        // Dénomination
        String nom,
        String nomComplet,
        String dci,
        String description,

        // Classification
        CategorieProduit categorie,
        String categorieLibelle,
        FormeProduit forme,
        String formeLibelle,
        String dosage,
        UniteVente uniteVente,
        Integer contenance,

        // Fabricant
        String laboratoire,
        String paysOrigine,

        // Prix
        BigDecimal prixPublicTTC,
        BigDecimal prixAchatHT,
        BigDecimal tauxTVA,
        BigDecimal tauxRemboursement,

        // Réglementation
        Boolean surOrdonnance,
        String listeMedicament,
        Boolean estGenerique,
        Boolean estRemboursable,

        // Conservation
        String temperatureConservation,
        Boolean chaineFroid,

        // Images
        String imageUrl,

        // Statut
        StatutProduit statut

) {}
