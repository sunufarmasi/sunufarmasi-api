package sn.sunufarmasi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import sn.sunufarmasi.auth.entity.OtpType;

/**
 * DTO Request pour vérifier un code OTP
 *
 * @author WeCan
 * @since 1.0.0
 */
public record VerifyOtpRequest(

        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(
                regexp = "^\\+221[0-9]{9}$",
                message = "Format de téléphone invalide. Format attendu: +221XXXXXXXXX"
        )
        String telephone,

        @NotBlank(message = "Le code OTP est obligatoire")
        @Size(min = 6, max = 6, message = "Le code OTP doit contenir 6 chiffres")
        @Pattern(regexp = "^[0-9]{6}$", message = "Le code OTP doit contenir uniquement des chiffres")
        String codeOtp,

        @NotNull(message = "Le type d'OTP est obligatoire")
        OtpType type
) {
    /**
     * Constructeur avec normalisation
     */
    public VerifyOtpRequest {
        // Normaliser le téléphone
        if (telephone != null) {
            telephone = telephone.trim();

            if (telephone.startsWith("7") && telephone.length() == 9) {
                telephone = "+221" + telephone;
            } else if (telephone.startsWith("221") && telephone.length() == 12) {
                telephone = "+" + telephone;
            }
        }

        // Normaliser le code
        if (codeOtp != null) {
            codeOtp = codeOtp.trim();
        }
    }
}