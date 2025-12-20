package sn.sunufarmasi.garde.dto.request;

import jakarta.validation.constraints.*;
import sn.sunufarmasi.garde.enums.TypeGarde;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

/**
 * DTO Request pour la création d'une garde
 * NOUVEAU MODÈLE : par SEMAINE (samedi à vendredi) et par ZONE
 *
 * @author WeCan
 * @since 1.0.0
 */
public record CreateGardeRequest(

        // ═══════════════════════════════════════════════════════════
        // PÉRIODE (SEMAINE)
        // ═══════════════════════════════════════════════════════════

        /**
         * N'importe quelle date dans la semaine souhaitée.
         * Le système calculera automatiquement le samedi (début) et vendredi (fin).
         * Exemple: si vous passez le 10 décembre 2024 (mardi),
         * la garde sera créée du 7 décembre (samedi) au 13 décembre (vendredi).
         */
        @NotNull(message = "La date est obligatoire")
        @FutureOrPresent(message = "La date doit être aujourd'hui ou dans le futur")
        LocalDate dateGarde,

        /**
         * Heure de début personnalisée (optionnel)
         * Si non spécifié, utilise les horaires par défaut du type de garde
         */
        LocalTime heureDebut,

        /**
         * Heure de fin personnalisée (optionnel)
         * Si non spécifié, utilise les horaires par défaut du type de garde
         */
        LocalTime heureFin,

        /**
         * Type de garde: JOUR, NUIT, ou JOUR_ET_NUIT (défaut)
         */
        TypeGarde type,

        // ═══════════════════════════════════════════════════════════
        // PHARMACIE
        // ═══════════════════════════════════════════════════════════

        @NotNull(message = "L'ID de la pharmacie est obligatoire")
        UUID pharmacieId,

        // ═══════════════════════════════════════════════════════════
        // ZONE (commune OU département obligatoire)
        // ═══════════════════════════════════════════════════════════

        /**
         * ID de la commune (si garde au niveau commune)
         * Au moins communeId ou departementId doit être fourni
         */
        UUID communeId,

        /**
         * ID du département (si garde au niveau département)
         * Au moins communeId ou departementId doit être fourni
         */
        UUID departementId,

        // ═══════════════════════════════════════════════════════════
        // INFORMATIONS COMPLÉMENTAIRES
        // ═══════════════════════════════════════════════════════════

        @Size(max = 1000, message = "Les notes ne doivent pas dépasser 1000 caractères")
        String notes,

        @Pattern(regexp = "^\\+221[0-9]{9}$", message = "Format téléphone invalide (ex: +221771234567)")
        String telephoneGarde

) {
    /**
     * Valide qu'au moins une zone est spécifiée
     */
    public boolean isZoneValid() {
        return communeId != null || departementId != null;
    }

    /**
     * Obtenir le type de garde (défaut: JOUR_ET_NUIT)
     */
    public TypeGarde getTypeEffectif() {
        return type != null ? type : TypeGarde.JOUR_ET_NUIT;
    }

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
}