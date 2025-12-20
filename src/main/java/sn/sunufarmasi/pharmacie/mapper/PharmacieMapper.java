package sn.sunufarmasi.pharmacie.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sn.sunufarmasi.pharmacie.dto.response.PharmacieDetailResponse;
import sn.sunufarmasi.pharmacie.dto.response.PharmacieResponse;
import sn.sunufarmasi.pharmacie.entity.Pharmacie;

import java.util.Collections;
import java.util.List;

/**
 * Mapper pour convertir Pharmacie entity vers DTOs
 *
 * @author WeCan
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class PharmacieMapper {

    private final ObjectMapper objectMapper;

    /**
     * Convertir Pharmacie → PharmacieResponse (version simple)
     */
    public PharmacieResponse toResponse(Pharmacie pharmacie) {
        if (pharmacie == null) {
            return null;
        }

        return new PharmacieResponse(
                pharmacie.getId(),
                pharmacie.getCode(),
                pharmacie.getNom(),
                pharmacie.getRaisonSociale(),

                // Localisation
                pharmacie.getCommune() != null ? pharmacie.getCommune().getId() : null,
                pharmacie.getCommune() != null ? pharmacie.getCommune().getNom() : null,
                pharmacie.getAdresseComplete(),
                pharmacie.getQuartier(),
                pharmacie.getLatitude(),
                pharmacie.getLongitude(),
                pharmacie.getLienGoogleMaps(),

                // Contact
                pharmacie.getTelephone(),
                pharmacie.getTelephoneSecondaire(),
                pharmacie.getEmail(),
                pharmacie.getSiteWeb(),

                // Documents
                pharmacie.getNumeroAgrementMinistere(),
                pharmacie.getNumeroOrdre(),
                pharmacie.getDateOuverture(),

                // Propriétaire
                pharmacie.getPharmacienProprietaire() != null ? pharmacie.getPharmacienProprietaire().getId() : null,
                pharmacie.getPharmacienProprietaire() != null ? pharmacie.getPharmacienProprietaire().getNomComplet() : null,

                // Syndicat
                pharmacie.getSyndicat() != null ? pharmacie.getSyndicat().getId() : null,
                pharmacie.getSyndicat() != null ? pharmacie.getSyndicat().getNom() : null,

                // Statut
                pharmacie.getStatut(),
                pharmacie.getDateValidation(),

                // Configuration
                pharmacie.getAccepteCommandes(),
                pharmacie.getProposeLivraison(),
                pharmacie.getRayonLivraisonKm(),

                // Images
                pharmacie.getLogoUrl(),
                pharmacie.getPhotoFacadeUrl(),

                // Métadonnées
                pharmacie.getCreatedAt(),
                pharmacie.getUpdatedAt()
        );
    }

    /**
     * Convertir Pharmacie → PharmacieDetailResponse (version complète)
     */
    public PharmacieDetailResponse toDetailResponse(Pharmacie pharmacie) {
        if (pharmacie == null) {
            return null;
        }

        return new PharmacieDetailResponse(
                pharmacie.getId(),
                pharmacie.getCode(),
                pharmacie.getNom(),
                pharmacie.getRaisonSociale(),

                // Localisation
                new PharmacieDetailResponse.LocalisationInfo(
                        pharmacie.getCommune() != null ? pharmacie.getCommune().getId() : null,
                        pharmacie.getCommune() != null ? pharmacie.getCommune().getNom() : null,
                        pharmacie.getCommune() != null && pharmacie.getCommune().getDepartement() != null
                                ? pharmacie.getCommune().getDepartement().getNom() : null,
                        pharmacie.getCommune() != null && pharmacie.getCommune().getDepartement() != null
                                && pharmacie.getCommune().getDepartement().getRegion() != null
                                ? pharmacie.getCommune().getDepartement().getRegion().getNom() : null,
                        pharmacie.getAdresseComplete(),
                        pharmacie.getQuartier(),
                        pharmacie.getLatitude(),
                        pharmacie.getLongitude(),
                        pharmacie.getLienGoogleMaps()
                ),

                // Contact
                new PharmacieDetailResponse.ContactInfo(
                        pharmacie.getTelephone(),
                        pharmacie.getTelephoneSecondaire(),
                        pharmacie.getEmail(),
                        pharmacie.getSiteWeb()
                ),

                // Documents
                new PharmacieDetailResponse.DocumentsInfo(
                        pharmacie.getNumeroAgrementMinistere(),
                        pharmacie.getNumeroOrdre(),
                        pharmacie.getDateOuverture(),
                        pharmacie.getRegistreCommerceUrl(),
                        pharmacie.getAgrementMinistereUrl(),
                        pharmacie.getAssuranceUrl()
                ),

                // Propriétaire
                pharmacie.getPharmacienProprietaire() != null
                        ? new PharmacieDetailResponse.PharmacienInfo(
                        pharmacie.getPharmacienProprietaire().getId(),
                        pharmacie.getPharmacienProprietaire().getNomComplet(),
                        pharmacie.getPharmacienProprietaire().getTelephone(),
                        pharmacie.getPharmacienProprietaire().getEmail()
                ) : null,

                // Syndicat
                pharmacie.getSyndicat() != null
                        ? new PharmacieDetailResponse.SyndicatInfo(
                        pharmacie.getSyndicat().getId(),
                        pharmacie.getSyndicat().getNom(),
                        pharmacie.getSyndicat().getRegion() != null
                                ? pharmacie.getSyndicat().getRegion().getNom() : null
                ) : null,

                // Statut
                pharmacie.getStatut(),
                pharmacie.getMotifRejet(),
                pharmacie.getDateValidation(),
                pharmacie.getValideParNom(),

                // Configuration
                new PharmacieDetailResponse.ConfigurationInfo(
                        pharmacie.getHoraires(),
                        pharmacie.getAccepteCommandes(),
                        pharmacie.getProposeLivraison(),
                        pharmacie.getRayonLivraisonKm(),
                        pharmacie.getNotificationsActives()
                ),

                // Images
                new PharmacieDetailResponse.ImagesInfo(
                        pharmacie.getLogoUrl(),
                        pharmacie.getPhotoFacadeUrl(),
                        parsePhotosInterieures(pharmacie.getPhotosInterieuresUrls())
                ),

                // Métadonnées
                pharmacie.getCreatedAt(),
                pharmacie.getUpdatedAt()
        );
    }

    /**
     * Parser les photos intérieures (JSON → List)
     */
    private List<String> parsePhotosInterieures(String photosJson) {
        if (photosJson == null || photosJson.isBlank()) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(photosJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}