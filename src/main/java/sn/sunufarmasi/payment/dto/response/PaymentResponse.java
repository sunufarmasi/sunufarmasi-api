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
        String montantFormate,  // "750 FCFA"
        PaymentMethod methode,
        PaymentStatus status,
        String referenceInterne,
        String referenceExterne,
        String telephonePaiement,
        String errorMessage,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime paidAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {
    /**
     * Formater le montant
     */
    public static String formatMontant(Integer montant) {
        return montant + " FCFA";
    }
}