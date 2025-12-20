package sn.sunufarmasi.employe.dto.response;

import sn.sunufarmasi.employe.enums.StatutEmploye;
import sn.sunufarmasi.employe.enums.TypePermission;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO Response pour un employé (version simple)
 *
 * @author WeCan
 * @since 1.0.0
 */
public record EmployeResponse(

        UUID id,
        String code,
        String username,

        // Informations personnelles
        String nom,
        String prenom,
        String nomComplet,
        String telephone,
        String email,
        String sexe,
        String photoUrl,

        // Emploi
        String poste,
        LocalDate dateEmbauche,

        // Pharmacie
        UUID pharmacieId,
        String pharmacieNom,
        String pharmacieCode,

        // Permissions
        Set<TypePermission> permissions,
        int nombrePermissions,

        // Statut
        StatutEmploye statut,
        Boolean doitChangerMotDePasse,

        // Métadonnées
        LocalDateTime createdAt,
        LocalDateTime derniereConnexion

) {}
