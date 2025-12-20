package sn.sunufarmasi.garde.dto.response;

import sn.sunufarmasi.garde.enums.StatutGarde;
import sn.sunufarmasi.garde.enums.TypeGarde;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO Response pour une garde (version complète)
 * NOUVEAU MODÈLE : par SEMAINE (samedi à vendredi) et par ZONE
 *
 * @author WeCan
 * @since 1.0.0
 */
public record GardeDetailResponse(

        UUID id,

        // Période (semaine)
        PeriodeInfo periode,

        // Pharmacie
        PharmacieInfo pharmacie,

        // Zone
        ZoneInfo zone,

        // Syndicat
        SyndicatInfo syndicat,

        // Statut
        StatutGarde statut,
        String motifAnnulation,
        boolean estSemaineEnCours,
        boolean estSemaineProchaine,
        boolean estPassee,

        // Remplacement
        boolean estRemplacement,
        UUID gardeRemplaceeId,

        // Informations complémentaires
        String notes,
        String telephoneGarde,

        // Confirmation
        Boolean confirmeParPharmacie,
        LocalDateTime dateConfirmation,
        Boolean notificationEnvoyee,
        Boolean rappelEnvoye,

        // Métadonnées
        MetadataInfo metadata

) {
    /**
     * Informations sur la période de garde (SEMAINE)
     */
    public record PeriodeInfo(
            LocalDate dateDebut,        // Samedi
            LocalDate dateFin,          // Vendredi
            LocalTime heureDebut,
            LocalTime heureFin,
            TypeGarde type,
            String typeLibelle,
            long dureeJours,            // 7 jours
            Integer numeroSemaine       // Numéro de semaine ISO
    ) {}

    /**
     * Informations sur la pharmacie de garde
     */
    public record PharmacieInfo(
            UUID id,
            String code,
            String nom,
            String adresse,
            String telephone,
            Double latitude,
            Double longitude
    ) {}

    /**
     * Informations sur la zone couverte (commune ou département)
     */
    public record ZoneInfo(
            UUID communeId,
            String communeNom,
            UUID departementId,
            String departementNom,
            UUID regionId,
            String regionNom
    ) {}

    /**
     * Informations sur le syndicat organisateur
     */
    public record SyndicatInfo(
            UUID id,
            String code,
            String nom,
            String telephone
    ) {}

    /**
     * Métadonnées
     */
    public record MetadataInfo(
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            UUID creeParId,
            String creeParNom
    ) {}
}