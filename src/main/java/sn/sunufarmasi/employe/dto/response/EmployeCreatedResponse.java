package sn.sunufarmasi.employe.dto.response;

import sn.sunufarmasi.employe.enums.TypePermission;

import java.util.Set;
import java.util.UUID;

/**
 * DTO Response retourné lors de la création d'un employé
 * Inclut le mot de passe initial généré (à communiquer à l'employé)
 *
 * @author WeCan
 * @since 1.0.0
 */
public record EmployeCreatedResponse(

        UUID id,
        String code,
        String username,

        // Informations personnelles
        String nom,
        String prenom,
        String nomComplet,
        String telephone,

        // Pharmacie
        UUID pharmacieId,
        String pharmacieNom,

        // Permissions
        Set<TypePermission> permissions,

        // Credentials (à communiquer à l'employé)
        String motDePasseInitial,

        String message

) {
    public static EmployeCreatedResponse of(
            UUID id, String code, String username,
            String nom, String prenom, String telephone,
            UUID pharmacieId, String pharmacieNom,
            Set<TypePermission> permissions,
            String motDePasseInitial
    ) {
        return new EmployeCreatedResponse(
                id, code, username,
                nom, prenom, prenom + " " + nom, telephone,
                pharmacieId, pharmacieNom,
                permissions,
                motDePasseInitial,
                "Employé créé avec succès. Communiquez le username et le mot de passe initial à l'employé."
        );
    }
}
