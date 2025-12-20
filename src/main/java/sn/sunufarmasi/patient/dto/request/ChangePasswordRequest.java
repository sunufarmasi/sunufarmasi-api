package sn.sunufarmasi.patient.dto.request;

import jakarta.validation.constraints.*;

/**
 * DTO Request pour le changement de mot de passe
 *
 * @author WeCan
 * @since 1.0.0
 */
public record ChangePasswordRequest(

        @NotBlank(message = "L'ancien mot de passe est obligatoire")
        String oldPassword,

        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
                message = "Le mot de passe doit contenir au moins une majuscule, une minuscule et un chiffre"
        )
        String newPassword,

        @NotBlank(message = "La confirmation est obligatoire")
        String confirmPassword
) {
    public ChangePasswordRequest {
        // Vérifier que les mots de passe correspondent
        if (newPassword != null && confirmPassword != null && !newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
        }
    }
}