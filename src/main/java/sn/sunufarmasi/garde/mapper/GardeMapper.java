package sn.sunufarmasi.garde.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sn.sunufarmasi.garde.dto.response.GardeDetailResponse;
import sn.sunufarmasi.garde.dto.response.GardePubliqueResponse;
import sn.sunufarmasi.garde.dto.response.GardeResponse;
import sn.sunufarmasi.garde.entity.Garde;

import java.util.UUID;

/**
 * Mapper pour convertir Garde entity vers DTOs
 * NOUVEAU MODÈLE : Gardes par SEMAINE (samedi à vendredi) et par ZONE
 *
 * @author WeCan
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class GardeMapper {

    /**
     * Convertir Garde → GardeResponse (version simple)
     *
     * GardeResponse params:
     * UUID id,
     * LocalDate dateDebut, LocalDate dateFin,
     * LocalTime heureDebut, LocalTime heureFin,
     * TypeGarde type, long dureeJours, Integer numeroSemaine,
     * UUID pharmacieId, String pharmacieNom, String pharmacieCode, String pharmacieAdresse, String pharmacieTelephone,
     * UUID communeId, String communeNom, String departementNom, String regionNom, String zoneNom,
     * StatutGarde statut, boolean estSemaineEnCours, boolean estSemaineProchaine, boolean estPassee,
     * String telephoneGarde
     */
    public GardeResponse toResponse(Garde garde) {
        if (garde == null) {
            return null;
        }

        return new GardeResponse(
                // id
                garde.getId(),

                // Période (semaine)
                garde.getDateDebut(),       // Samedi
                garde.getDateFin(),         // Vendredi
                garde.getHeureDebut(),
                garde.getHeureFin(),
                garde.getTypeGarde(),
                garde.getNombreJours(),     // 7 jours
                garde.getNumeroSemaine(),   // Numéro de semaine ISO

                // Pharmacie
                garde.getPharmacie() != null ? garde.getPharmacie().getId() : null,
                garde.getPharmacie() != null ? garde.getPharmacie().getNom() : null,
                garde.getPharmacie() != null ? garde.getPharmacie().getCode() : null,
                garde.getPharmacie() != null ? garde.getPharmacie().getAdresseComplete() : null,
                garde.getPharmacie() != null ? garde.getPharmacie().getTelephone() : null,

                // Zone
                getZoneCommuneId(garde),
                getZoneCommuneNom(garde),
                getZoneDepartementNom(garde),
                getZoneRegionNom(garde),
                garde.getZoneNom(),

                // Statut
                garde.getStatut(),
                garde.estSemaineEnCours(),
                garde.estSemaineProchaine(),
                garde.estPassee(),

                // Contact
                getTelephoneEffectif(garde)
        );
    }

    /**
     * Convertir Garde → GardeDetailResponse (version complète)
     */
    public GardeDetailResponse toDetailResponse(Garde garde) {
        if (garde == null) {
            return null;
        }

        return new GardeDetailResponse(
                garde.getId(),

                // Période (semaine)
                new GardeDetailResponse.PeriodeInfo(
                        garde.getDateDebut(),           // Samedi
                        garde.getDateFin(),             // Vendredi
                        garde.getHeureDebut(),
                        garde.getHeureFin(),
                        garde.getTypeGarde(),
                        garde.getTypeGarde() != null ? garde.getTypeGarde().getLibelle() : null,
                        garde.getNombreJours(),         // 7 jours
                        garde.getNumeroSemaine()        // Numéro de semaine ISO
                ),

                // Pharmacie
                buildPharmacieInfo(garde),

                // Zone
                buildZoneInfo(garde),

                // Syndicat
                buildSyndicatInfo(garde),

                // Statut
                garde.getStatut(),
                garde.getMotifAnnulation(),
                garde.estSemaineEnCours(),
                garde.estSemaineProchaine(),
                garde.estPassee(),

                // Remplacement
                garde.getEstRemplacement() != null ? garde.getEstRemplacement() : false,
                garde.getGardeRemplaceeId(),

                // Informations complémentaires
                garde.getNotes(),
                getTelephoneEffectif(garde),

                // Confirmation
                garde.getConfirmeParPharmacie(),
                garde.getDateConfirmation(),
                garde.getNotificationEnvoyee(),
                garde.getRappelEnvoye(),

                // Métadonnées
                new GardeDetailResponse.MetadataInfo(
                        garde.getCreatedAt(),
                        garde.getUpdatedAt(),
                        garde.getCreeParId(),
                        garde.getCreeParNom()
                )
        );
    }

    /**
     * Convertir Garde → GardePubliqueResponse (API publique)
     */
    public GardePubliqueResponse toPubliqueResponse(Garde garde) {
        if (garde == null) {
            return null;
        }

        return GardePubliqueResponse.of(
                garde.getPharmacie() != null ? garde.getPharmacie().getNom() : null,
                garde.getPharmacie() != null ? garde.getPharmacie().getAdresseComplete() : null,
                getTelephoneEffectif(garde),
                garde.getPharmacie() != null ? garde.getPharmacie().getLatitude() : null,
                garde.getPharmacie() != null ? garde.getPharmacie().getLongitude() : null,
                garde.getDateDebut(),           // Samedi
                garde.getDateFin(),             // Vendredi
                garde.getHeureDebut(),
                garde.getHeureFin(),
                garde.getTypeGarde(),
                garde.getNumeroSemaine(),
                getZoneCommuneNom(garde),
                getZoneDepartementNom(garde),
                getZoneRegionNom(garde),
                garde.estSemaineEnCours(),
                garde.estSemaineProchaine()
        );
    }

    /**
     * Convertir Garde → GardePubliqueResponse avec distance
     */
    public GardePubliqueResponse toPubliqueResponse(Garde garde, Double userLat, Double userLng) {
        GardePubliqueResponse response = toPubliqueResponse(garde);
        if (response == null || userLat == null || userLng == null) {
            return response;
        }

        Double distance = null;
        if (garde.getPharmacie() != null &&
                garde.getPharmacie().getLatitude() != null &&
                garde.getPharmacie().getLongitude() != null) {
            distance = calculateDistance(
                    userLat, userLng,
                    garde.getPharmacie().getLatitude(),
                    garde.getPharmacie().getLongitude()
            );
        }

        return response.withDistance(distance);
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES - ZONE
    // ═══════════════════════════════════════════════════════════

    /**
     * Obtenir l'ID de la commune de la zone de garde
     */
    private UUID getZoneCommuneId(Garde garde) {
        // Priorité : commune directe sur la garde
        if (garde.getCommune() != null) {
            return garde.getCommune().getId();
        }
        // Fallback : commune de la pharmacie
        if (garde.getPharmacie() != null && garde.getPharmacie().getCommune() != null) {
            return garde.getPharmacie().getCommune().getId();
        }
        return null;
    }

    /**
     * Obtenir le nom de la commune
     */
    private String getZoneCommuneNom(Garde garde) {
        // Priorité : commune directe
        if (garde.getCommune() != null) {
            return garde.getCommune().getNom();
        }
        // Fallback : commune de la pharmacie
        if (garde.getPharmacie() != null && garde.getPharmacie().getCommune() != null) {
            return garde.getPharmacie().getCommune().getNom();
        }
        return null;
    }

    /**
     * Obtenir le nom du département de la zone
     */
    private String getZoneDepartementNom(Garde garde) {
        // Priorité : département direct sur la garde
        if (garde.getDepartement() != null) {
            return garde.getDepartement().getNom();
        }
        // Via commune de la garde
        if (garde.getCommune() != null && garde.getCommune().getDepartement() != null) {
            return garde.getCommune().getDepartement().getNom();
        }
        // Fallback : via pharmacie
        if (garde.getPharmacie() != null &&
                garde.getPharmacie().getCommune() != null &&
                garde.getPharmacie().getCommune().getDepartement() != null) {
            return garde.getPharmacie().getCommune().getDepartement().getNom();
        }
        return null;
    }

    /**
     * Obtenir le nom de la région de la zone
     */
    private String getZoneRegionNom(Garde garde) {
        // Via département direct
        if (garde.getDepartement() != null && garde.getDepartement().getRegion() != null) {
            return garde.getDepartement().getRegion().getNom();
        }
        // Via commune de la garde
        if (garde.getCommune() != null &&
                garde.getCommune().getDepartement() != null &&
                garde.getCommune().getDepartement().getRegion() != null) {
            return garde.getCommune().getDepartement().getRegion().getNom();
        }
        // Fallback : via pharmacie
        if (garde.getPharmacie() != null &&
                garde.getPharmacie().getCommune() != null &&
                garde.getPharmacie().getCommune().getDepartement() != null &&
                garde.getPharmacie().getCommune().getDepartement().getRegion() != null) {
            return garde.getPharmacie().getCommune().getDepartement().getRegion().getNom();
        }
        // Fallback : via syndicat
        if (garde.getPlanning() != null &&
                garde.getPlanning().getSyndicat() != null &&
                garde.getPlanning().getSyndicat().getRegion() != null) {
            return garde.getPlanning().getSyndicat().getRegion().getNom();
        }
        return null;
    }

    /**
     * Obtenir l'ID du département de la zone
     */
    private UUID getZoneDepartementId(Garde garde) {
        // Priorité : département direct
        if (garde.getDepartement() != null) {
            return garde.getDepartement().getId();
        }
        // Via commune de la garde
        if (garde.getCommune() != null && garde.getCommune().getDepartement() != null) {
            return garde.getCommune().getDepartement().getId();
        }
        // Fallback : via pharmacie
        if (garde.getPharmacie() != null &&
                garde.getPharmacie().getCommune() != null &&
                garde.getPharmacie().getCommune().getDepartement() != null) {
            return garde.getPharmacie().getCommune().getDepartement().getId();
        }
        return null;
    }

    /**
     * Obtenir l'ID de la région de la zone
     */
    private UUID getZoneRegionId(Garde garde) {
        // Via département direct
        if (garde.getDepartement() != null && garde.getDepartement().getRegion() != null) {
            return garde.getDepartement().getRegion().getId();
        }
        // Via commune de la garde
        if (garde.getCommune() != null &&
                garde.getCommune().getDepartement() != null &&
                garde.getCommune().getDepartement().getRegion() != null) {
            return garde.getCommune().getDepartement().getRegion().getId();
        }
        // Fallback : via pharmacie
        if (garde.getPharmacie() != null &&
                garde.getPharmacie().getCommune() != null &&
                garde.getPharmacie().getCommune().getDepartement() != null &&
                garde.getPharmacie().getCommune().getDepartement().getRegion() != null) {
            return garde.getPharmacie().getCommune().getDepartement().getRegion().getId();
        }
        // Via syndicat
        if (garde.getPlanning() != null &&
                garde.getPlanning().getSyndicat() != null &&
                garde.getPlanning().getSyndicat().getRegion() != null) {
            return garde.getPlanning().getSyndicat().getRegion().getId();
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES - BUILDERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Obtenir le téléphone effectif (pharmacie)
     */
    private String getTelephoneEffectif(Garde garde) {
        if (garde.getPharmacie() != null && garde.getPharmacie().getTelephone() != null) {
            return garde.getPharmacie().getTelephone();
        }
        return null;
    }

    private GardeDetailResponse.PharmacieInfo buildPharmacieInfo(Garde garde) {
        if (garde.getPharmacie() == null) {
            return null;
        }

        return new GardeDetailResponse.PharmacieInfo(
                garde.getPharmacie().getId(),
                garde.getPharmacie().getCode(),
                garde.getPharmacie().getNom(),
                garde.getPharmacie().getAdresseComplete(),
                getTelephoneEffectif(garde),
                garde.getPharmacie().getLatitude(),
                garde.getPharmacie().getLongitude()
        );
    }

    private GardeDetailResponse.ZoneInfo buildZoneInfo(Garde garde) {
        return new GardeDetailResponse.ZoneInfo(
                getZoneCommuneId(garde),
                getZoneCommuneNom(garde),
                getZoneDepartementId(garde),
                getZoneDepartementNom(garde),
                getZoneRegionId(garde),
                getZoneRegionNom(garde)
        );
    }

    private GardeDetailResponse.SyndicatInfo buildSyndicatInfo(Garde garde) {
        if (garde.getPlanning() != null && garde.getPlanning().getSyndicat() != null) {
            var syndicat = garde.getPlanning().getSyndicat();
            return new GardeDetailResponse.SyndicatInfo(
                    syndicat.getId(),
                    syndicat.getCode(),
                    syndicat.getNom(),
                    syndicat.getTelephone()
            );
        }
        return null;
    }

    /**
     * Calcule la distance en km entre deux points (formule Haversine)
     */
    private Double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        final int R = 6371; // Rayon de la Terre en km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round(R * c * 10.0) / 10.0; // Arrondi à 1 décimale
    }
}