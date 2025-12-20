package sn.sunufarmasi.garde.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import sn.sunufarmasi.garde.enums.TypeGarde;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

/**
 * DTO Request pour la planification en lot de gardes
 * Permet de planifier plusieurs gardes en une seule requête
 * NOUVEAU MODÈLE : par SEMAINE (samedi à vendredi) et par ZONE
 *
 * @author WeCan
 * @since 1.0.0
 */
public record PlanifierGardesRequest(

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

        /**
         * Type de garde pour toutes les gardes du lot
         * Défaut: JOUR_ET_NUIT (24h/24)
         */
        TypeGarde type,

        @NotEmpty(message = "La liste des gardes ne peut pas être vide")
        @Valid
        List<GardePlanifiee> gardes

) {
    /**
     * Une garde individuelle dans la planification
     */
    public record GardePlanifiee(

            @NotNull(message = "L'ID de la pharmacie est obligatoire")
            UUID pharmacieId,

            /**
             * N'importe quelle date dans la semaine souhaitée.
             * Le système calculera automatiquement le samedi (début) et vendredi (fin).
             */
            @NotNull(message = "La date est obligatoire")
            @FutureOrPresent(message = "La date doit être aujourd'hui ou dans le futur")
            LocalDate dateGarde,

            String notes

    ) {
        /**
         * Calculer le début de semaine (samedi)
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
         * Calculer la fin de semaine (vendredi)
         */
        public LocalDate getDateFinSemaine() {
            LocalDate debut = getDateDebutSemaine();
            return debut != null ? debut.plusDays(6) : null;
        }
    }

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
}