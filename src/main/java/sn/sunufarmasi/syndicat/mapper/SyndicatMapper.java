package sn.sunufarmasi.syndicat.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sn.sunufarmasi.syndicat.dto.response.SyndicatDetailResponse;
import sn.sunufarmasi.syndicat.dto.response.SyndicatResponse;
import sn.sunufarmasi.syndicat.entity.Syndicat;
import sn.sunufarmasi.syndicat.enums.TypeSyndicat;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Mapper pour convertir Syndicat entity vers DTOs
 *
 * @author WeCan
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class SyndicatMapper {

    /**
     * Convertir Syndicat → SyndicatResponse (version simple)
     */
    public SyndicatResponse toResponse(Syndicat syndicat) {
        if (syndicat == null) {
            return null;
        }

        // Déterminer la zone selon le type
        UUID zoneId = null;
        String nomZone = null;
        List<UUID> communeIds = Collections.emptyList();
        List<String> communeNoms = Collections.emptyList();

        if (syndicat.getType() == TypeSyndicat.COMMUNE && syndicat.getCommune() != null) {
            zoneId = syndicat.getCommune().getId();
            nomZone = syndicat.getCommune().getNom();
        } else if (syndicat.getType() == TypeSyndicat.DEPARTEMENT && syndicat.getDepartement() != null) {
            zoneId = syndicat.getDepartement().getId();
            nomZone = syndicat.getDepartement().getNom();
        } else if (syndicat.getType() == TypeSyndicat.REGION && syndicat.getRegion() != null) {
            zoneId = syndicat.getRegion().getId();
            nomZone = syndicat.getRegion().getNom();
        } else if (syndicat.getType() == TypeSyndicat.ZONE && syndicat.getCommunesZone() != null) {
            communeIds = syndicat.getCommunesZone().stream()
                    .map(c -> c.getId()).toList();
            communeNoms = syndicat.getCommunesZone().stream()
                    .map(c -> c.getNom()).toList();
            // Nom de zone = liste des communes
            nomZone = String.join(", ", communeNoms);
        }

        return new SyndicatResponse(
                syndicat.getId(),
                syndicat.getCode(),
                syndicat.getNom(),
                syndicat.getDescription(),

                // Type et zone
                syndicat.getType(),
                zoneId,
                nomZone,
                syndicat.getRegion() != null ? syndicat.getRegion().getId() : null,
                syndicat.getRegion() != null ? syndicat.getRegion().getNom() : null,

                // Communes zone (ZONE type)
                communeIds,
                communeNoms,

                // Contact
                syndicat.getTelephone(),
                syndicat.getEmail(),

                // Responsable
                syndicat.getResponsable() != null ? syndicat.getResponsable().getId() : null,
                syndicat.getResponsable() != null
                        ? syndicat.getResponsable().getNomComplet()
                        : syndicat.getNomResponsable(),
                syndicat.getResponsable() != null
                        ? syndicat.getResponsable().getTelephone()
                        : syndicat.getTelephoneResponsable(),

                // Abonnement
                syndicat.getPlan(),
                syndicat.getStatutAbonnement(),
                syndicat.getMontantMensuel(),
                syndicat.getDateDernierPaiement(),
                syndicat.getEssaiGratuit(),
                syndicat.getDateFinEssai(),
                syndicat.getDateFinAbonnement(),

                // Statistiques
                syndicat.getNombrePharmaciesActuelles(),

                // Statut
                syndicat.getStatut(),

                // Métadonnées
                syndicat.getCreatedAt(),
                syndicat.getDerniereConnexion()
        );
    }

    /**
     * Convertir Syndicat → SyndicatDetailResponse (version complète)
     */
    public SyndicatDetailResponse toDetailResponse(Syndicat syndicat) {
        return toDetailResponse(syndicat, null, null);
    }

    /**
     * Convertir Syndicat → SyndicatDetailResponse avec statistiques supplémentaires
     */
    public SyndicatDetailResponse toDetailResponse(
            Syndicat syndicat,
            Integer nombrePharmaciesValidees,
            Integer nombrePharmaciesEnAttente
    ) {
        if (syndicat == null) {
            return null;
        }

        return new SyndicatDetailResponse(
                syndicat.getId(),
                syndicat.getCode(),
                syndicat.getNom(),
                syndicat.getDescription(),
                syndicat.getUsername(),

                // Zone
                buildZoneInfo(syndicat),

                // Contact
                new SyndicatDetailResponse.ContactInfo(
                        syndicat.getTelephone(),
                        syndicat.getTelephoneSecondaire(),
                        syndicat.getEmail(),
                        syndicat.getAdresse()
                ),

                // Responsable
                buildResponsableInfo(syndicat),

                // Abonnement
                new SyndicatDetailResponse.AbonnementInfo(
                        syndicat.getPlan(),
                        syndicat.getStatutAbonnement(),
                        syndicat.getMontantMensuel(),
                        syndicat.getEssaiGratuit(),
                        syndicat.getDateDebutEssai(),
                        syndicat.getDateFinEssai(),
                        syndicat.getDateDebutAbonnement(),
                        syndicat.getDateFinAbonnement(),
                        syndicat.getDateDernierPaiement(),
                        syndicat.getProchainPaiement(),
                        syndicat.getRenouvellementAutomatique(),
                        syndicat.getNombrePharmaciesMax()
                ),

                // Statistiques
                new SyndicatDetailResponse.StatistiquesInfo(
                        syndicat.getNombrePharmaciesActuelles(),
                        syndicat.getNombrePharmaciesMax(),
                        nombrePharmaciesValidees,
                        nombrePharmaciesEnAttente
                ),

                // Statut
                syndicat.getStatut(),

                // Métadonnées
                new SyndicatDetailResponse.MetadataInfo(
                        syndicat.getCreeParId(),
                        syndicat.getCreeParNom(),
                        syndicat.getCreatedAt(),
                        syndicat.getUpdatedAt(),
                        syndicat.getDerniereConnexion()
                )
        );
    }

    /**
     * Construire les informations de zone géographique
     */
    private SyndicatDetailResponse.ZoneInfo buildZoneInfo(Syndicat syndicat) {
        UUID communeId = null;
        String nomCommune = null;
        UUID departementId = null;
        String nomDepartement = null;

        if (syndicat.getType() == TypeSyndicat.COMMUNE && syndicat.getCommune() != null) {
            communeId = syndicat.getCommune().getId();
            nomCommune = syndicat.getCommune().getNom();

            // Récupérer le département de la commune
            if (syndicat.getCommune().getDepartement() != null) {
                departementId = syndicat.getCommune().getDepartement().getId();
                nomDepartement = syndicat.getCommune().getDepartement().getNom();
            }
        } else if (syndicat.getType() == TypeSyndicat.DEPARTEMENT && syndicat.getDepartement() != null) {
            departementId = syndicat.getDepartement().getId();
            nomDepartement = syndicat.getDepartement().getNom();
        }

        return new SyndicatDetailResponse.ZoneInfo(
                syndicat.getType(),
                communeId,
                nomCommune,
                departementId,
                nomDepartement,
                syndicat.getRegion() != null ? syndicat.getRegion().getId() : null,
                syndicat.getRegion() != null ? syndicat.getRegion().getNom() : null
        );
    }

    /**
     * Construire les informations du responsable
     */
    private SyndicatDetailResponse.ResponsableInfo buildResponsableInfo(Syndicat syndicat) {
        if (syndicat.getResponsable() != null) {
            return new SyndicatDetailResponse.ResponsableInfo(
                    syndicat.getResponsable().getId(),
                    syndicat.getResponsable().getNomComplet(),
                    syndicat.getResponsable().getTelephone(),
                    syndicat.getResponsable().getEmail()
            );
        }

        // Si pas de pharmacien associé mais un nom de responsable
        if (syndicat.getNomResponsable() != null) {
            return new SyndicatDetailResponse.ResponsableInfo(
                    null,
                    syndicat.getNomResponsable(),
                    null,
                    null
            );
        }

        return null;
    }
}
