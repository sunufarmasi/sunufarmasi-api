package sn.sunufarmasi.garde.dto.response;

import sn.sunufarmasi.garde.enums.StatutGarde;
import sn.sunufarmasi.garde.enums.TypeGarde;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO Response pour une garde (version simple)
 * NOUVEAU MODÈLE : par SEMAINE (samedi à vendredi) et par ZONE
 *
 * @author WeCan
 * @since 1.0.0
 */
public record GardeResponse(

        UUID id,

        // Période (semaine)
        LocalDate dateDebut,        // Samedi
        LocalDate dateFin,          // Vendredi
        LocalTime heureDebut,
        LocalTime heureFin,
        TypeGarde type,
        long dureeJours,            // 7 jours
        Integer numeroSemaine,      // Numéro de semaine ISO

        // Pharmacie
        UUID pharmacieId,
        String pharmacieNom,
        String pharmacieCode,
        String pharmacieAdresse,
        String pharmacieTelephone,

        // Zone
        UUID communeId,
        String communeNom,
        String departementNom,
        String regionNom,
        String zoneNom,             // Nom de la zone de garde (commune ou département)

        // Statut
        StatutGarde statut,
        boolean estSemaineEnCours,
        boolean estSemaineProchaine,
        boolean estPassee,

        // Contact
        String telephoneGarde

) {}