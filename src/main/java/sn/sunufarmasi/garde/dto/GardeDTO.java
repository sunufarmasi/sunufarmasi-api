package sn.sunufarmasi.garde.dto;

import sn.sunufarmasi.garde.entity.StatutPlanning;
import sn.sunufarmasi.garde.enums.StatutGarde;
import sn.sunufarmasi.garde.enums.TypeGarde;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * DTOs pour le module Garde
 * NOUVEAU MODÈLE : Gardes par SEMAINE (samedi à vendredi) et par ZONE
 *
 * @author WeCan
 * @since 1.0.0
 */
public class GardeDTO {

    // ═══════════════════════════════════════════════════════════
    // PLANNING DTOs
    // ═══════════════════════════════════════════════════════════

    public record CreatePlanningRequest(
            String titre,
            String description,
            LocalDate dateDebut,
            LocalDate dateFin,
            Boolean publicationAuto,
            LocalDateTime datePublicationPrevue,
            Boolean notifierPharmacies,
            Boolean notifierSms,
            Boolean notifierEmail
    ) {}

    public record UpdatePlanningRequest(
            String titre,
            String description,
            LocalDate dateDebut,
            LocalDate dateFin,
            Boolean publicationAuto,
            LocalDateTime datePublicationPrevue,
            Boolean notifierPharmacies,
            Boolean notifierSms,
            Boolean notifierEmail
    ) {}

    public record PlanningResponse(
            UUID id,
            String titre,
            String description,
            LocalDate dateDebut,
            LocalDate dateFin,
            StatutPlanning statut,
            String statutLibelle,
            Boolean publicationAuto,
            LocalDateTime datePublicationPrevue,
            LocalDateTime datePublication,
            Boolean notifierPharmacies,
            Boolean notifierSms,
            Boolean notifierEmail,
            UUID creeParId,
            String creeParNom,
            UUID publieParId,
            int nombreGardes,
            int nombreGardesConfirmees,
            long nombreJours,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<GardeResponse> gardes
    ) {}

    public record PlanningResumeResponse(
            UUID id,
            String titre,
            LocalDate dateDebut,
            LocalDate dateFin,
            StatutPlanning statut,
            String statutLibelle,
            int nombreGardes,
            Boolean publicationAuto,
            LocalDateTime datePublication
    ) {}

    /**
     * Vue admin de tous les plannings (tous syndicats)
     */
    public record AdminPlanningResponse(
            UUID id,
            String titre,
            LocalDate dateDebut,
            LocalDate dateFin,
            StatutPlanning statut,
            String statutLibelle,
            UUID syndicatId,
            String syndicatNom,
            String syndicatRegion,
            int nombreGardes,
            List<GardeResumeResponse> gardes
    ) {}

    // ═══════════════════════════════════════════════════════════
    // GARDE DTOs (NOUVEAU MODÈLE PAR SEMAINE)
    // ═══════════════════════════════════════════════════════════

    /**
     * Request pour créer une garde
     * dateGarde : n'importe quelle date de la semaine (le système calcule samedi-vendredi)
     * communeId OU departementId : zone géographique de la garde
     */
    public record CreateGardeRequest(
            UUID pharmacieId,
            LocalDate dateGarde,          // Une date dans la semaine souhaitée
            UUID communeId,               // Zone : commune (optionnel si departementId fourni)
            UUID departementId,           // Zone : département (optionnel si communeId fourni)
            TypeGarde typeGarde,          // JOUR, NUIT, ou JOUR_ET_NUIT (défaut)
            LocalTime heureDebut,         // Optionnel : horaire personnalisé
            LocalTime heureFin,           // Optionnel : horaire personnalisé
            String notes
    ) {}

    public record CreateGardesBatchRequest(
            List<CreateGardeRequest> gardes
    ) {}

    /**
     * Response complète d'une garde
     */
    public record GardeResponse(
            UUID id,
            UUID planningId,
            PharmacieGardeInfo pharmacie,
            LocalDate dateDebut,          // Samedi
            LocalDate dateFin,            // Vendredi
            Integer numeroSemaine,
            String zone,                  // Nom de la commune ou département
            TypeGarde typeGarde,
            String typeGardeLibelle,
            StatutGarde statut,
            String statutLibelle,
            LocalTime heureDebut,
            LocalTime heureFin,
            String notes,
            Boolean confirmeParPharmacie,
            LocalDateTime dateConfirmation,
            Boolean notificationEnvoyee,
            Boolean rappelEnvoye,
            Double latitude,
            Double longitude,
            boolean estSemaineEnCours,
            boolean estSemaineProchaine,
            boolean estPassee,
            long nombreJours
    ) {}

    /**
     * Response résumée d'une garde (pour listes)
     */
    public record GardeResumeResponse(
            UUID id,
            String pharmacieNom,
            String pharmacieAdresse,
            String pharmacieTelephone,
            LocalDate dateDebut,
            LocalDate dateFin,
            Integer numeroSemaine,
            String zone,
            TypeGarde typeGarde,
            String horaires,
            StatutGarde statut,
            Double latitude,
            Double longitude,
            boolean estSemaineEnCours
    ) {}

    /**
     * Response avec distance (pour recherche proximité)
     */
    public record GardeProximiteResponse(
            GardeResumeResponse garde,
            Double distanceKm
    ) {}

    /**
     * Infos pharmacie dans une garde
     */
    public record PharmacieGardeInfo(
            UUID id,
            String code,
            String nom,
            String adresse,
            String telephone,
            String email,
            Double latitude,
            Double longitude,
            String commune,
            String departement
    ) {}

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE DTOs
    // ═══════════════════════════════════════════════════════════

    /**
     * Request pour rechercher les gardes par semaine et zone
     */
    public record RechercheGardeRequest(
            LocalDate dateDebut,          // Optionnel : début semaine (défaut: semaine courante)
            LocalDate dateFin,            // Optionnel : fin semaine
            UUID communeId,               // Optionnel : filtrer par commune
            UUID departementId,           // Optionnel : filtrer par département
            Double latitude,              // Optionnel : pour tri par proximité
            Double longitude,             // Optionnel : pour tri par proximité
            Double rayonKm                // Optionnel : rayon de recherche (défaut: 10km)
    ) {}

    /**
     * Response de recherche de gardes
     */
    public record RechercheGardeResponse(
            PeriodeInfo periode,
            String zone,
            int nombreResultats,
            List<GardeResumeResponse> gardes
    ) {}

    public record PeriodeInfo(
            LocalDate dateDebut,
            LocalDate dateFin,
            Integer numeroSemaine,
            String libelle
    ) {}
}