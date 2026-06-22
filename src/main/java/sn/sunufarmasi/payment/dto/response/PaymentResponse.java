package sn.sunufarmasi.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import sn.sunufarmasi.payment.entity.PaymentMethod;
import sn.sunufarmasi.payment.entity.PaymentStatus;

import java.time.LocalDateTime;

/**
 * DTO Response pour Payment
 *
 * @author WeCan
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse(
        String id,
        Integer montant,
        String montantFormate,
        PaymentMethod methode,
        PaymentStatus status,
        String referenceInterne,
        String referenceExterne,
        String telephonePaiement,
        String errorMessage,

        /** URL Wave Checkout à ouvrir dans le navigateur (null si non-Wave) */
        String waveCheckoutUrl,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime paidAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        /** Nom du patient (pour l'affichage admin) */
        String patientNom,

        /** ID du patient */
        String patientId,

        /** Code du plan souscrit */
        String planCode
) {
    public static String formatMontant(Integer montant) {
        return montant + " FCFA";
    }
}
