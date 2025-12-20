package sn.sunufarmasi.syndicat.dto.response;

import sn.sunufarmasi.pharmacie.enums.StatutAbonnement;
import sn.sunufarmasi.syndicat.enums.PlanAbonnementSyndicat;
import sn.sunufarmasi.syndicat.enums.StatutSyndicat;
import sn.sunufarmasi.syndicat.enums.TypeSyndicat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO Response pour un syndicat (version complète avec tous les détails)
 *
 * @author WeCan
 * @since 1.0.0
 */
public record SyndicatDetailResponse(

        UUID id,
        String code,
        String nom,
        String description,
        String username,

        // Zone géographique
        ZoneInfo zone,

        // Contact
        ContactInfo contact,

        // Responsable
        ResponsableInfo responsable,

        // Abonnement
        AbonnementInfo abonnement,

        // Statistiques
        StatistiquesInfo statistiques,

        // Statut
        StatutSyndicat statut,

        // Métadonnées
        MetadataInfo metadata

) {
    /**
     * Informations sur la zone géographique
     */
    public record ZoneInfo(
            TypeSyndicat type,
            UUID communeId,
            String nomCommune,
            UUID departementId,
            String nomDepartement,
            UUID regionId,
            String nomRegion
    ) {}

    /**
     * Informations de contact
     */
    public record ContactInfo(
            String telephone,
            String telephoneSecondaire,
            String email,
            String adresse
    ) {}

    /**
     * Informations sur le responsable
     */
    public record ResponsableInfo(
            UUID pharmacienId,
            String nom,
            String telephone,
            String email
    ) {}

    /**
     * Informations sur l'abonnement
     */
    public record AbonnementInfo(
            PlanAbonnementSyndicat plan,
            StatutAbonnement statutAbonnement,
            BigDecimal montantMensuel,
            Boolean essaiGratuit,
            LocalDate dateDebutEssai,
            LocalDate dateFinEssai,
            LocalDate dateDebutAbonnement,
            LocalDate dateFinAbonnement,
            LocalDate dateDernierPaiement,
            LocalDate prochainPaiement,
            Boolean renouvellementAutomatique,
            Integer nombrePharmaciesMax
    ) {}

    /**
     * Statistiques du syndicat
     */
    public record StatistiquesInfo(
            Integer nombrePharmaciesActuelles,
            Integer nombrePharmaciesMax,
            Integer nombrePharmaciesValidees,
            Integer nombrePharmaciesEnAttente
    ) {}

    /**
     * Métadonnées
     */
    public record MetadataInfo(
            UUID creeParId,
            String creeParNom,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime derniereConnexion
    ) {}
}
