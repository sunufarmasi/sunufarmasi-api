package sn.sunufarmasi.stock.dto.request;

import jakarta.validation.constraints.*;
import sn.sunufarmasi.stock.enums.CategorieProduit;
import sn.sunufarmasi.stock.enums.FormeProduit;
import sn.sunufarmasi.stock.enums.UniteVente;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO Request pour la création d'un produit dans le catalogue
 *
 * @author WeCan
 * @since 1.0.0
 */
public record CreateProduitRequest(

        // Identification
        @Size(max = 50, message = "Le code barre ne doit pas dépasser 50 caractères")
        String codeBarre,

        @Size(max = 20, message = "Le code CIP ne doit pas dépasser 20 caractères")
        String codeCip,

        @Size(max = 50, message = "Le numéro AMM ne doit pas dépasser 50 caractères")
        String numeroAmm,

        // Dénomination
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 255, message = "Le nom ne doit pas dépasser 255 caractères")
        String nom,

        @Size(max = 255, message = "La DCI ne doit pas dépasser 255 caractères")
        String dci,

        @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
        String description,

        // Classification
        @NotNull(message = "La catégorie est obligatoire")
        CategorieProduit categorie,

        FormeProduit forme,

        @Size(max = 100, message = "Le dosage ne doit pas dépasser 100 caractères")
        String dosage,

        UniteVente uniteVente,

        @Min(value = 1, message = "La contenance doit être positive")
        Integer contenance,

        // Fabricant
        @Size(max = 200, message = "Le laboratoire ne doit pas dépasser 200 caractères")
        String laboratoire,

        @Size(max = 100, message = "Le pays d'origine ne doit pas dépasser 100 caractères")
        String paysOrigine,

        // Prix
        @DecimalMin(value = "0", message = "Le prix public TTC doit être positif")
        BigDecimal prixPublicTTC,

        @DecimalMin(value = "0", message = "Le prix d'achat HT doit être positif")
        BigDecimal prixAchatHT,

        @DecimalMin(value = "0", message = "Le taux TVA doit être positif")
        @DecimalMax(value = "100", message = "Le taux TVA ne peut pas dépasser 100%")
        BigDecimal tauxTVA,

        @DecimalMin(value = "0", message = "Le taux de remboursement doit être positif")
        @DecimalMax(value = "100", message = "Le taux de remboursement ne peut pas dépasser 100%")
        BigDecimal tauxRemboursement,

        // Réglementation
        Boolean surOrdonnance,

        @Size(max = 10, message = "La liste médicament ne doit pas dépasser 10 caractères")
        String listeMedicament,

        Boolean estGenerique,

        UUID produitReferenceId,

        Boolean estRemboursable,

        // Conservation
        @Size(max = 50, message = "La température de conservation ne doit pas dépasser 50 caractères")
        String temperatureConservation,

        Boolean chaineFroid,

        @Min(value = 1, message = "La durée de conservation doit être positive")
        Integer dureeConservationMois,

        // Images
        @Size(max = 500, message = "L'URL de l'image ne doit pas dépasser 500 caractères")
        String imageUrl,

        @Size(max = 500, message = "L'URL de la notice ne doit pas dépasser 500 caractères")
        String noticeUrl

) {}
