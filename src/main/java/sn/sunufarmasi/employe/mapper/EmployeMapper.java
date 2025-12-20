package sn.sunufarmasi.employe.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sn.sunufarmasi.employe.dto.response.EmployeDetailResponse;
import sn.sunufarmasi.employe.dto.response.EmployeResponse;
import sn.sunufarmasi.employe.entity.Employe;
import sn.sunufarmasi.employe.enums.TypePermission;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Mapper pour convertir Employe entity vers DTOs
 *
 * @author WeCan
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class EmployeMapper {

    /**
     * Convertir Employe → EmployeResponse (version simple)
     */
    public EmployeResponse toResponse(Employe employe) {
        if (employe == null) {
            return null;
        }

        return new EmployeResponse(
                employe.getId(),
                employe.getCode(),
                employe.getUsername(),

                // Informations personnelles
                employe.getNom(),
                employe.getPrenom(),
                employe.getNomComplet(),
                employe.getTelephone(),
                employe.getEmail(),
                employe.getSexe(),
                employe.getPhotoUrl(),

                // Emploi
                employe.getPoste(),
                employe.getDateEmbauche(),

                // Pharmacie
                employe.getPharmacie() != null ? employe.getPharmacie().getId() : null,
                employe.getPharmacie() != null ? employe.getPharmacie().getNom() : null,
                employe.getPharmacie() != null ? employe.getPharmacie().getCode() : null,

                // Permissions
                employe.getPermissions(),
                employe.getPermissions() != null ? employe.getPermissions().size() : 0,

                // Statut
                employe.getStatut(),
                employe.getDoitChangerMotDePasse(),

                // Métadonnées
                employe.getCreatedAt(),
                employe.getDerniereConnexion()
        );
    }

    /**
     * Convertir Employe → EmployeDetailResponse (version complète)
     */
    public EmployeDetailResponse toDetailResponse(Employe employe) {
        if (employe == null) {
            return null;
        }

        return new EmployeDetailResponse(
                employe.getId(),
                employe.getCode(),
                employe.getUsername(),

                // Informations personnelles
                new EmployeDetailResponse.PersonnelInfo(
                        employe.getNom(),
                        employe.getPrenom(),
                        employe.getNomComplet(),
                        employe.getTelephone(),
                        employe.getEmail(),
                        employe.getDateNaissance(),
                        employe.getSexe(),
                        employe.getAdresse(),
                        employe.getPhotoUrl()
                ),

                // Emploi
                new EmployeDetailResponse.EmploiInfo(
                        employe.getPoste(),
                        employe.getDateEmbauche(),
                        employe.getDateFinContrat(),
                        calculerJoursAnciennete(employe.getDateEmbauche())
                ),

                // Pharmacie
                buildPharmacieInfo(employe),

                // Pharmacien
                buildPharmacienInfo(employe),

                // Permissions
                buildPermissionsInfo(employe),

                // Statut
                employe.getStatut(),
                employe.getMotifSuspension(),
                employe.getDoitChangerMotDePasse(),

                // Métadonnées
                new EmployeDetailResponse.MetadataInfo(
                        employe.getCreatedAt(),
                        employe.getUpdatedAt(),
                        employe.getDerniereConnexion(),
                        employe.getNombreConnexions()
                )
        );
    }

    /**
     * Construire les informations de la pharmacie
     */
    private EmployeDetailResponse.PharmacieInfo buildPharmacieInfo(Employe employe) {
        if (employe.getPharmacie() == null) {
            return null;
        }

        return new EmployeDetailResponse.PharmacieInfo(
                employe.getPharmacie().getId(),
                employe.getPharmacie().getCode(),
                employe.getPharmacie().getNom(),
                employe.getPharmacie().getCommune() != null
                        ? employe.getPharmacie().getCommune().getNom()
                        : null,
                employe.getPharmacie().getTelephone()
        );
    }

    /**
     * Construire les informations du pharmacien créateur
     */
    private EmployeDetailResponse.PharmacienInfo buildPharmacienInfo(Employe employe) {
        if (employe.getPharmacien() == null) {
            return null;
        }

        return new EmployeDetailResponse.PharmacienInfo(
                employe.getPharmacien().getId(),
                employe.getPharmacien().getNomComplet(),
                employe.getPharmacien().getTelephone()
        );
    }

    /**
     * Construire les informations des permissions
     */
    private EmployeDetailResponse.PermissionsInfo buildPermissionsInfo(Employe employe) {
        var permissions = employe.getPermissions();
        int nombre = permissions != null ? permissions.size() : 0;

        return new EmployeDetailResponse.PermissionsInfo(
                permissions,
                nombre,
                employe.aPermission(TypePermission.VENDRE),
                employe.aPermission(TypePermission.MODIFIER_STOCK),
                employe.aUnePermission(TypePermission.VOIR_RAPPORTS_BASIQUES, TypePermission.VOIR_RAPPORTS_AVANCES),
                employe.aUnePermission(TypePermission.CREER_COMMANDE, TypePermission.VALIDER_RECEPTION)
        );
    }

    /**
     * Calculer le nombre de jours d'ancienneté
     */
    private Long calculerJoursAnciennete(LocalDate dateEmbauche) {
        if (dateEmbauche == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(dateEmbauche, LocalDate.now());
    }
}
