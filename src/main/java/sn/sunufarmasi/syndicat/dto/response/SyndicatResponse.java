package sn.sunufarmasi.syndicat.dto.response;

import sn.sunufarmasi.pharmacie.enums.StatutAbonnement;
import sn.sunufarmasi.syndicat.enums.PlanAbonnementSyndicat;
import sn.sunufarmasi.syndicat.enums.StatutSyndicat;
import sn.sunufarmasi.syndicat.enums.TypeSyndicat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SyndicatResponse(

        UUID id,
        String code,
        String nom,
        String description,

        // Type et zone
        TypeSyndicat type,
        UUID zoneId,           // communeId ou departementId selon le type
        String nomZone,        // Nom de la commune, département ou région
        UUID regionId,
        String nomRegion,

        // Pour type = ZONE : liste des communes couvertes
        List<UUID> communeIds,
        List<String> communeNoms,

        // Contact
        String telephone,
        String email,

        // Responsable
        UUID responsableId,
        String nomResponsable,
        String telephoneResponsable,

        // Abonnement
        PlanAbonnementSyndicat plan,
        StatutAbonnement statutAbonnement,
        BigDecimal montantMensuel,
        LocalDate dateDernierPaiement,
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
