package sn.sunufarmasi.garde.dto.response;

import sn.sunufarmasi.garde.enums.TypeGarde;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO Response pour l'API publique de recherche de pharmacie de garde
 * Contient uniquement les informations essentielles pour le grand public
 * NOUVEAU MODÈLE : par SEMAINE (samedi à vendredi) et par ZONE
 *
 * @author WeCan
 * @since 1.0.0
 */
public record GardePubliqueResponse(

        // Pharmacie
        String pharmacieNom,
        String pharmacieAdresse,
        String pharmacieTelephone,
        Double latitude,
        Double longitude,

        // Période (semaine)
        LocalDate dateDebut,        // Samedi
        LocalDate dateFin,          // Vendredi
        LocalTime heureDebut,
        LocalTime heureFin,
        TypeGarde type,
        String typeLibelle,
        Integer numeroSemaine,      // Numéro de semaine ISO

        // Zone
        String commune,
        String departement,
        String region,

        // État
        boolean estSemaineEnCours,
        boolean estSemaineProchaine,

        // Indication distance (sera calculée côté client ou service)
        Double distanceKm

) {
    /**
     * Créer une réponse sans distance
     */
    public static GardePubliqueResponse of(
            String pharmacieNom, String pharmacieAdresse, String pharmacieTelephone,
            Double latitude, Double longitude,
            LocalDate dateDebut, LocalDate dateFin,
            LocalTime heureDebut, LocalTime heureFin,
            TypeGarde type,
            Integer numeroSemaine,
            String commune, String departement, String region,
            boolean estSemaineEnCours, boolean estSemaineProchaine
    ) {
        return new GardePubliqueResponse(
                pharmacieNom, pharmacieAdresse, pharmacieTelephone,
                latitude, longitude,
                dateDebut, dateFin, heureDebut, heureFin,
                type, type != null ? type.getLibelle() : null, numeroSemaine,
                commune, departement, region,
                estSemaineEnCours, estSemaineProchaine,
                null
        );
    }

    /**
     * Créer une réponse avec distance
     */
    public GardePubliqueResponse withDistance(Double distanceKm) {
        return new GardePubliqueResponse(
                this.pharmacieNom, this.pharmacieAdresse, this.pharmacieTelephone,
                this.latitude, this.longitude,
                this.dateDebut, this.dateFin, this.heureDebut, this.heureFin,
                this.type, this.typeLibelle, this.numeroSemaine,
                this.commune, this.departement, this.region,
                this.estSemaineEnCours, this.estSemaineProchaine,
                distanceKm
        );
    }

    /**
     * Obtenir les horaires formatés
     */
    public String getHorairesFormates() {
        if (heureDebut != null && heureFin != null) {
            return heureDebut.toString() + " - " + heureFin.toString();
        }
        return type != null ? type.getHorairesDefaut() : "24h/24";
    }

    /**
     * Obtenir le libellé de la période
     */
    public String getPeriodeLibelle() {
        return "Du " + dateDebut + " au " + dateFin + " (Semaine " + numeroSemaine + ")";
    }

    /**
     * Obtenir la zone complète
     */
    public String getZoneComplete() {
        StringBuilder sb = new StringBuilder();
        if (commune != null) sb.append(commune);
        if (departement != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(departement);
        }
        if (region != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(region);
        }
        return sb.toString();
    }
}