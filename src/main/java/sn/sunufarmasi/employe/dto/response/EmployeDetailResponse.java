package sn.sunufarmasi.employe.dto.response;

import sn.sunufarmasi.employe.enums.StatutEmploye;
import sn.sunufarmasi.employe.enums.TypePermission;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO Response pour un employé (version complète avec tous les détails)
 *
 * @author WeCan
 * @since 1.0.0
 */
public record EmployeDetailResponse(

        UUID id,
        String code,
        String username,

        // Informations personnelles
        PersonnelInfo personnel,

        // Emploi
        EmploiInfo emploi,

        // Pharmacie
        PharmacieInfo pharmacie,

        // Pharmacien (créateur)
        PharmacienInfo pharmacien,

        // Permissions
        PermissionsInfo permissions,

        // Statut
        StatutEmploye statut,
        String motifSuspension,
        Boolean doitChangerMotDePasse,

        // Métadonnées
        MetadataInfo metadata

) {
    /**
     * Informations personnelles
     */
    public record PersonnelInfo(
            String nom,
            String prenom,
            String nomComplet,
            String telephone,
            String email,
            LocalDate dateNaissance,
            String sexe,
            String adresse,
            String photoUrl
    ) {}

    /**
     * Informations sur l'emploi
     */
    public record EmploiInfo(
            String poste,
            LocalDate dateEmbauche,
            LocalDate dateFinContrat,
            Long joursAnciennete
    ) {}

    /**
     * Informations sur la pharmacie
     */
    public record PharmacieInfo(
            UUID id,
            String code,
            String nom,
            String commune,
            String telephone
    ) {}

    /**
     * Informations sur le pharmacien créateur
     */
    public record PharmacienInfo(
            UUID id,
            String nomComplet,
            String telephone
    ) {}

    /**
     * Informations sur les permissions
     */
    public record PermissionsInfo(
            Set<TypePermission> liste,
            int nombre,
            boolean peutVendre,
            boolean peutGererStock,
            boolean peutVoirRapports,
            boolean peutGererCommandes
    ) {}

    /**
     * Métadonnées
     */
    public record MetadataInfo(
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime derniereConnexion,
            Integer nombreConnexions
    ) {}
}
