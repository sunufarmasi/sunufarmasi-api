package sn.sunufarmasi.syndicat.dto.response;

import sn.sunufarmasi.syndicat.enums.StatutSyndicat;
import sn.sunufarmasi.syndicat.enums.TypeSyndicat;

import java.util.UUID;

/**
 * DTO Response pour l'authentification d'un syndicat
 *
 * @author WeCan
 * @since 1.0.0
 */
public record SyndicatAuthResponse(

        UUID id,
        String code,
        String nom,
        String username,

        // Zone
        TypeSyndicat type,
        String nomZone,
        String nomRegion,

        // Statut
        StatutSyndicat statut,
        Boolean abonnementActif,

        // Token JWT
        String accessToken,
        String refreshToken,
        Long expiresIn,

        String message

) {
    /**
     * Créer une réponse de succès
     */
    public static SyndicatAuthResponse success(
            UUID id, String code, String nom, String username,
            TypeSyndicat type, String nomZone, String nomRegion,
            StatutSyndicat statut, Boolean abonnementActif,
            String accessToken, String refreshToken, Long expiresIn
    ) {
        return new SyndicatAuthResponse(
                id, code, nom, username,
                type, nomZone, nomRegion,
                statut, abonnementActif,
                accessToken, refreshToken, expiresIn,
                "Connexion réussie"
        );
    }

    /**
     * Créer une réponse d'échec
     */
    public static SyndicatAuthResponse failure(String message) {
        return new SyndicatAuthResponse(
                null, null, null, null,
                null, null, null,
                null, null,
                null, null, null,
                message
        );
    }
}
