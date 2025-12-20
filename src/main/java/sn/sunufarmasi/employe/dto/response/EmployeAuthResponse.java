package sn.sunufarmasi.employe.dto.response;

import sn.sunufarmasi.employe.enums.StatutEmploye;
import sn.sunufarmasi.employe.enums.TypePermission;

import java.util.Set;
import java.util.UUID;

/**
 * DTO Response pour l'authentification d'un employé
 *
 * @author WeCan
 * @since 1.0.0
 */
public record EmployeAuthResponse(

        UUID id,
        String code,
        String username,
        String nomComplet,

        // Pharmacie
        UUID pharmacieId,
        String pharmacieNom,
        String pharmacieCode,

        // Permissions
        Set<TypePermission> permissions,

        // Statut
        StatutEmploye statut,
        Boolean doitChangerMotDePasse,

        // Token JWT
        String accessToken,
        String refreshToken,
        Long expiresIn,

        String message

) {
    /**
     * Créer une réponse de succès
     */
    public static EmployeAuthResponse success(
            UUID id, String code, String username, String nomComplet,
            UUID pharmacieId, String pharmacieNom, String pharmacieCode,
            Set<TypePermission> permissions,
            StatutEmploye statut, Boolean doitChangerMotDePasse,
            String accessToken, String refreshToken, Long expiresIn
    ) {
        return new EmployeAuthResponse(
                id, code, username, nomComplet,
                pharmacieId, pharmacieNom, pharmacieCode,
                permissions,
                statut, doitChangerMotDePasse,
                accessToken, refreshToken, expiresIn,
                doitChangerMotDePasse ? "Connexion réussie - Veuillez changer votre mot de passe" : "Connexion réussie"
        );
    }

    /**
     * Créer une réponse d'échec
     */
    public static EmployeAuthResponse failure(String message) {
        return new EmployeAuthResponse(
                null, null, null, null,
                null, null, null,
                null,
                null, null,
                null, null, null,
                message
        );
    }
}
