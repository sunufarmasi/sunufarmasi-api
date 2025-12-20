package sn.sunufarmasi.vente.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import sn.sunufarmasi.vente.enums.ModePaiement;
import sn.sunufarmasi.vente.enums.TypeVente;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO Request pour créer une vente
 *
 * @author WeCan
 * @since 1.0.0
 */
public record CreateVenteRequest(

        // Type de vente
        TypeVente type,

        // Client (optionnel)
        UUID clientId,

        @Size(max = 200, message = "Le nom du client ne doit pas dépasser 200 caractères")
        String clientNom,

        @Pattern(regexp = "^\\+221[0-9]{9}$", message = "Format téléphone invalide")
        String clientTelephone,

        // Ordonnance (si applicable)
        @Size(max = 50, message = "Le numéro d'ordonnance ne doit pas dépasser 50 caractères")
        String numeroOrdonnance,

        @Size(max = 200, message = "Le médecin prescripteur ne doit pas dépasser 200 caractères")
        String medecinPrescripteur,

        LocalDateTime dateOrdonnance,

        // Mutuelle (si applicable)
        UUID mutuelleId,

        @Size(max = 200, message = "Le nom de la mutuelle ne doit pas dépasser 200 caractères")
        String mutuelleNom,

        @Size(max = 50, message = "Le numéro d'adhérent ne doit pas dépasser 50 caractères")
        String numeroAdherent,

        @DecimalMin(value = "0", message = "Le taux de prise en charge doit être positif")
        @DecimalMax(value = "100", message = "Le taux de prise en charge ne peut pas dépasser 100%")
        BigDecimal tauxPriseEnCharge,

        // Lignes de vente
        @NotEmpty(message = "La vente doit contenir au moins une ligne")
        @Valid
        List<LigneVenteRequest> lignes,

        // Remise globale (optionnel)
        @DecimalMin(value = "0", message = "Le pourcentage de remise doit être positif")
        @DecimalMax(value = "100", message = "Le pourcentage de remise ne peut pas dépasser 100%")
        BigDecimal pourcentageRemise,

        @DecimalMin(value = "0", message = "Le montant de remise doit être positif")
        BigDecimal montantRemise,

        // Paiement (optionnel - peut être fait séparément)
        ModePaiement modePaiement,

        @DecimalMin(value = "0", message = "Le montant payé doit être positif")
        BigDecimal montantPaye,

        @Size(max = 100, message = "La référence de paiement ne doit pas dépasser 100 caractères")
        String referencePaiement,

        // Notes
        @Size(max = 1000, message = "Les notes ne doivent pas dépasser 1000 caractères")
        String notes

) {}
