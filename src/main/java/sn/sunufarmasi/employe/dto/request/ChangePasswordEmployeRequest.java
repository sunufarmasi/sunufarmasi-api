package sn.sunufarmasi.employe.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO Request pour le changement de mot de passe d'un employé
 *
 * @author WeCan
 * @since 1.0.0
 */
public record ChangePasswordEmployeRequest(

        @NotBlank(message = "L'ancien mot de passe est obligatoire")
        String ancienMotDePasse,

        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 6, max = 100, message = "Le nouveau mot de passe doit contenir entre 6 et 100 caractères")
        String nouveauMotDePasse,

        @NotBlank(message = "La confirmation du mot de passe est obligatoire")
        String confirmationMotDePasse

) {
    /**
     * Vérifie si les mots de passe correspondent
     */
    public boolean motDePasseCorrespond() {
        return nouveauMotDePasse != null && nouveauMotDePasse.equals(confirmationMotDePasse);
    }
}
