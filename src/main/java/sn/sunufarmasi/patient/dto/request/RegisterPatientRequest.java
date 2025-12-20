package sn.sunufarmasi.patient.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * DTO Request pour l'inscription d'un patient
 *
 * WORKFLOW SIMPLIFIÉ :
 * 1. Patient s'inscrit avec : Téléphone + Nom + Commune
 * 2. Vérifie son téléphone avec OTP
 * 3. Reçoit 15 jours d'essai gratuit automatiquement
 * 4. PIN de sécurité créé LOCALEMENT dans l'app mobile (pas en base)
 * 5. Après 15 jours → Doit payer pour continuer
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

        @NotNull(message = "La commune est obligatoire")
        String communeId,

        LocalDate dateNaissance,

        @Pattern(regexp = "M|F", message = "Sexe invalide. Valeurs acceptées: M ou F")
        String sexe,

        String adresse
) {
    /**
     * Constructeur avec validation
     */
    public RegisterPatientRequest {
        // Normaliser le téléphone
        if (telephone != null) {
            telephone = telephone.trim();

            // Ajouter +221 si manquant
            if (telephone.startsWith("7") && telephone.length() == 9) {
                telephone = "+221" + telephone;
            } else if (telephone.startsWith("221") && telephone.length() == 12) {
                telephone = "+" + telephone;
            }
        }
    }
}