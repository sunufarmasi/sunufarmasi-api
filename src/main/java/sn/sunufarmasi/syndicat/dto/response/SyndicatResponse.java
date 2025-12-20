package sn.sunufarmasi.syndicat.dto.response;

import sn.sunufarmasi.syndicat.enums.PlanAbonnementSyndicat;
import sn.sunufarmasi.syndicat.enums.StatutSyndicat;
import sn.sunufarmasi.syndicat.enums.TypeSyndicat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO Response pour un syndicat (version simple)
 *
 * @author WeCan
 * @since 1.0.0
 */
public record SyndicatResponse(

        UUID id,
        String code,
        String nom,
        String description,

        // Type et zone
        TypeSyndicat type,
        UUID zoneId,           // communeId ou departementId selon le type
        String nomZone,        // Nom de la commune ou du département
        UUID regionId,
        String nomRegion,

        // Contact
        String telephone,
        String email,

        // Responsable
        UUID responsableId,
        String nomResponsable,

        // Abonnement
        PlanAbonnementSyndicat plan,
        Boolean essaiGratuit,
        LocalDate dateFinEssai,
        LocalDate dateFinAbonnement,

        // Statistiques
        Integer nombrePharmacies,

        // Statut
        StatutSyndicat statut,

        // Métadonnées
        LocalDateTime createdAt,
        LocalDateTime derniereConnexion

) {}
