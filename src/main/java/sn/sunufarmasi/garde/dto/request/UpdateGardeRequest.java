package sn.sunufarmasi.garde.dto.request;

import jakarta.validation.constraints.*;
import sn.sunufarmasi.garde.enums.TypeGarde;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

/**
 * DTO Request pour la modification d'une garde
 * NOUVEAU MODÈLE : par SEMAINE (samedi à vendredi) et par ZONE
 *
 * @author WeCan
 * @since 1.0.0
 */
public record UpdateGardeRequest(

        /**
         * Nouvelle date (n'importe quelle date dans la semaine souhaitée)
         * Si fournie, recalcule automatiquement dateDebut et dateFin
         */
        LocalDate dateGarde,

        /**
         * Nouvelle heure de début personnalisée
         */
        LocalTime heureDebut,

        /**
         * Nouvelle heure de fin personnalisée
         */
        LocalTime heureFin,

        /**
         * Nouveau type de garde
         */
        TypeGarde type,

        /**
         * Nouvelle pharmacie
         */
        UUID pharmacieId,

        /**
         * Nouvelle commune (si changement de zone)
         */
        UUID communeId,

        /**
         * Nouveau département (si changement de zone)
         */
        UUID departementId,

        @Size(max = 1000, message = "Les notes ne doivent pas dépasser 1000 caractères")
        String notes,

        @Pattern(regexp = "^\\+221[0-9]{9}$", message = "Format téléphone invalide (ex: +221771234567)")
        String telephoneGarde

) {
    /**
     * Calculer le début de semaine (samedi) pour la date donnée
     */
    public LocalDate getDateDebutSemaine() {
        if (dateGarde == null) return null;
        DayOfWeek jour = dateGarde.getDayOfWeek();
        if (jour == DayOfWeek.SATURDAY) {
            return dateGarde;
        } else if (jour == DayOfWeek.SUNDAY) {
            return dateGarde.minusDays(1);
        } else {
            return dateGarde.with(TemporalAdjusters.previous(DayOfWeek.SATURDAY));
        }
    }

    /**
     * Calculer la fin de semaine (vendredi) pour la date donnée
     */
    public LocalDate getDateFinSemaine() {
        LocalDate debut = getDateDebutSemaine();
        return debut != null ? debut.plusDays(6) : null;
    }

    /**
     * Vérifie si une zone est spécifiée
     */
    public boolean hasZone() {
        return communeId != null || departementId != null;
    }
}