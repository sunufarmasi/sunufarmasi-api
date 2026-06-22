package sn.sunufarmasi.patient.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * DTO Request pour l'inscription d'un patient
 *
 * WORKFLOW :
 * 1. Patient s'inscrit avec : Nom + Téléphone + Email + deviceId
 * 2. Un code OTP est envoyé à l'email
 * 3. Patient saisit le code → compte activé
 * 4. 15 jours d'essai gratuit automatiquement
 * 5. Après 15 jours → payer pour continuer
 *
 * @author WeCan
 * @since 1.0.0
 */
public record RegisterPatientRequest(

        @NotBlank(message = "Le nom complet est obligatoire")
        @Size(min = 3, max = 255, message = "Le nom doit contenir entre 3 et 255 caractères")
        String nomComplet,

        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(
                regexp = "^\\+221[0-9]{9}$",
                message = "Format de téléphone invalide. Format attendu: +221XXXXXXXXX"
        )
        String telephone,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        @Size(max = 255)
        String email,

        @NotBlank(message = "L'identifiant de l'appareil est obligatoire")
        String deviceId,

        // Optionnel — peut être renseigné plus tard
        String communeId,

        LocalDate dateNaissance,

        @Pattern(regexp = "M|F|", message = "Sexe invalide. Valeurs acceptées: M ou F")
        String sexe,

        String adresse
) {
    public RegisterPatientRequest {
        if (telephone != null) {
            telephone = telephone.trim();
            if (telephone.startsWith("7") && telephone.length() == 9) {
                telephone = "+221" + telephone;
            } else if (telephone.startsWith("221") && telephone.length() == 12) {
                telephone = "+" + telephone;
            }
        }
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }
}
