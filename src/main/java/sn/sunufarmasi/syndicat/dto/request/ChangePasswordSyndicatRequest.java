package sn.sunufarmasi.syndicat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO Request pour le changement de mot de passe d'un syndicat
 *
 * @author WeCan
 * @since 1.0.0
 */
public record ChangePasswordSyndicatRequest(

        @NotBlank(message = "L'ancien mot de passe est obligatoire")
        String ancienMotDePasse,

        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 8, max = 100, message = "Le nouveau mot de passe doit contenir entre 8 et 100 caractères")
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
