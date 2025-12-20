package sn.sunufarmasi.stock.dto.request;

import jakarta.validation.constraints.*;
import sn.sunufarmasi.stock.enums.CategorieProduit;
import sn.sunufarmasi.stock.enums.FormeProduit;
import sn.sunufarmasi.stock.enums.StatutProduit;
import sn.sunufarmasi.stock.enums.UniteVente;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO Request pour la modification d'un produit
 *
 * @author WeCan
 * @since 1.0.0
 */
public record UpdateProduitRequest(

        @Size(max = 50, message = "Le code barre ne doit pas dépasser 50 caractères")
        String codeBarre,

        @Size(max = 255, message = "Le nom ne doit pas dépasser 255 caractères")
        String nom,

        @Size(max = 255, message = "La DCI ne doit pas dépasser 255 caractères")
        String dci,

        @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
        String description,

        CategorieProduit categorie,

        FormeProduit forme,

        @Size(max = 100, message = "Le dosage ne doit pas dépasser 100 caractères")
        String dosage,

        UniteVente uniteVente,

        Integer contenance,

        @Size(max = 200, message = "Le laboratoire ne doit pas dépasser 200 caractères")
        String laboratoire,

        @DecimalMin(value = "0", message = "Le prix public TTC doit être positif")
        BigDecimal prixPublicTTC,

        @DecimalMin(value = "0", message = "Le prix d'achat HT doit être positif")
        BigDecimal prixAchatHT,

        BigDecimal tauxTVA,

        BigDecimal tauxRemboursement,

        Boolean surOrdonnance,

        String listeMedicament,

        Boolean estRemboursable,

        String temperatureConservation,

        Boolean chaineFroid,

        Integer dureeConservationMois,

        String imageUrl,

        String noticeUrl,

        StatutProduit statut

) {}
