package sn.sunufarmasi.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import sn.sunufarmasi.payment.entity.PaymentMethod;

/**
 * DTO Request pour initier un paiement
 *
 * @author WeCan
 * @since 1.0.0
 */
public record InitiatePaymentRequest(

        @NotNull(message = "Le plan d'abonnement est obligatoire")
        String planId,

        @NotNull(message = "La méthode de paiement est obligatoire")
        PaymentMethod methode,

        @NotBlank(message = "Le numéro de téléphone de paiement est obligatoire")
        @Pattern(
                regexp = "^\\+221[0-9]{9}$",
                message = "Format de téléphone invalide. Format attendu: +221XXXXXXXXX"
        )
        String telephonePaiement
) {
    /**
     * Constructeur avec normalisation
     */
    public InitiatePaymentRequest {
        // Normaliser le téléphone
        if (telephonePaiement != null) {
            telephonePaiement = telephonePaiement.trim();

            if (telephonePaiement.startsWith("7") && telephonePaiement.length() == 9) {
                telephonePaiement = "+221" + telephonePaiement;
            } else if (telephonePaiement.startsWith("221") && telephonePaiement.length() == 12) {
                telephonePaiement = "+" + telephonePaiement;
            }
        }
    }
}