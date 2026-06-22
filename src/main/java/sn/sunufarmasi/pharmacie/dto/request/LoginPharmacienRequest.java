package sn.sunufarmasi.pharmacie.dto.request;

/**
 * DTO Request pour la connexion d'un pharmacien
 *
 * @author WeCan
 * @since 1.0.0
 */
public record LoginPharmacienRequest(

        // Téléphone OU email — l'un des deux suffit
        String telephone,

        String email,

        // Optionnel — si absent, aucune vérification de mot de passe (phase transition)
        String motDePasse

) {}