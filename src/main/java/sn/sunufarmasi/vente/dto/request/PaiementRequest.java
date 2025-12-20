package sn.sunufarmasi.vente.dto.request;

import jakarta.validation.constraints.*;
import sn.sunufarmasi.vente.enums.ModePaiement;

import java.math.BigDecimal;

/**
 * DTO Request pour enregistrer un paiement
 *
 * @author WeCan
 * @since 1.0.0
 */
public record PaiementRequest(

        @NotNull(message = "Le mode de paiement est obligatoire")
        ModePaiement modePaiement,

        @NotNull(message = "Le montant est obligatoire")
        @DecimalMin(value = "0.01", message = "Le montant doit être positif")
        BigDecimal montant,

        @Size(max = 100, message = "La référence ne doit pas dépasser 100 caractères")
        String reference

) {}
