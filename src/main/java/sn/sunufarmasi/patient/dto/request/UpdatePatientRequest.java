package sn.sunufarmasi.patient.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * DTO Request pour la mise à jour d'un patient
 *
 * @author WeCan
 * @since 1.0.0
 */
public record UpdatePatientRequest(

        @Size(min = 3, max = 255, message = "Le nom doit contenir entre 3 et 255 caractères")
        String nomComplet,

        @Pattern(
                regexp = "^\\+221[0-9]{9}$",
                message = "Format de téléphone invalide. Format attendu: +221XXXXXXXXX"
        )
        String telephone,

        @Email(message = "Format d'email invalide")
        String email,

        String communeId,

        LocalDate dateNaissance,

        @Pattern(regexp = "M|F", message = "Sexe invalide. Valeurs acceptées: M ou F")
        String sexe,

        String adresse,

        String photoUrl
) {
    /**
     * Constructeur avec normalisation
     */
    public UpdatePatientRequest {
        // Normaliser le téléphone si fourni
        if (telephone != null && !telephone.isEmpty()) {
            telephone = telephone.trim();

            if (telephone.startsWith("7") && telephone.length() == 9) {
                telephone = "+221" + telephone;
            } else if (telephone.startsWith("221") && telephone.length() == 12) {
                telephone = "+" + telephone;
            }
        }

        // Normaliser l'email si fourni
        if (email != null && !email.isEmpty()) {
            email = email.trim().toLowerCase();
        }
    }
}