package sn.sunufarmasi.auth.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * DTO Request pour compléter l'inscription après vérification OTP
 *
 * @author WeCan
 * @since 1.0.0
 */
public record CompleteRegistrationRequest(

        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(
                regexp = "^\\+221[0-9]{9}$",
                message = "Format de téléphone invalide"
        )
        String telephone,

        @NotBlank(message = "Le nom complet est obligatoire")
        @Size(min = 3, max = 255, message = "Le nom doit contenir entre 3 et 255 caractères")
        String nomComplet,

        @Email(message = "Format d'email invalide")
        String email,

        @NotNull(message = "La commune est obligatoire")
        String communeId,

        LocalDate dateNaissance,

        @Pattern(regexp = "M|F", message = "Sexe invalide. Valeurs acceptées: M ou F")
        String sexe,

        String adresse
) {
    /**
     * Constructeur avec normalisation
     */
    public CompleteRegistrationRequest {
        // Normaliser le téléphone
        if (telephone != null) {
            telephone = telephone.trim();

            if (telephone.startsWith("7") && telephone.length() == 9) {
                telephone = "+221" + telephone;
            } else if (telephone.startsWith("221") && telephone.length() == 12) {
                telephone = "+" + telephone;
            }
        }

        // Normaliser l'email
        if (email != null && !email.isEmpty()) {
            email = email.trim().toLowerCase();
        }
    }
}