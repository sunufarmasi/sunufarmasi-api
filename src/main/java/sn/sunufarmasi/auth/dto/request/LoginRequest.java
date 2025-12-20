package sn.sunufarmasi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO Request pour demander un OTP de connexion
 *
 * @author WeCan
 * @since 1.0.0
 */
public record LoginRequest(

        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(
                regexp = "^\\+221[0-9]{9}$",
                message = "Format de téléphone invalide. Format attendu: +221XXXXXXXXX"
        )
        String telephone
) {
    /**
     * Constructeur avec normalisation
     */
    public LoginRequest {
        // Normaliser le téléphone
        if (telephone != null) {
            telephone = telephone.trim();

            if (telephone.startsWith("7") && telephone.length() == 9) {
                telephone = "+221" + telephone;
            } else if (telephone.startsWith("221") && telephone.length() == 12) {
                telephone = "+" + telephone;
            }
        }
    }
}