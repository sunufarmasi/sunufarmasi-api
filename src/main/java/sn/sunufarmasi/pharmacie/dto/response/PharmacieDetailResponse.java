package sn.sunufarmasi.pharmacie.dto.response;

import sn.sunufarmasi.pharmacie.enums.StatutPharmacie;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO Response détaillé pour une pharmacie (avec toutes les infos)
 *
 * @author WeCan
 * @since 1.0.0
 */
public record PharmacieDetailResponse(
        UUID id,
        String code,
        String nom,
        String raisonSociale,

        // Localisation complète
        LocalisationInfo localisation,

        // Contact
        ContactInfo contact,

        // Documents
        DocumentsInfo documents,

        // Propriétaire
        PharmacienInfo pharmacienProprietaire,

        // Syndicat
        SyndicatInfo syndicat,

        // Statut
        StatutPharmacie statut,
        String motifRejet,
        LocalDateTime dateValidation,
        String valideParNom,

        // Configuration
        ConfigurationInfo configuration,

        // Images
        ImagesInfo images,

        // Métadonnées
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public record LocalisationInfo(
            UUID communeId,
            String communeNom,
            String departementNom,
            String regionNom,
            String adresseComplete,
            String quartier,
            Double latitude,
            Double longitude,
            String lienGoogleMaps
    ) {}

    public record ContactInfo(
            String telephone,
            String telephoneSecondaire,
            String email,
            String siteWeb
    ) {}

    public record DocumentsInfo(
            String numeroAgrementMinistere,
            String numeroOrdre,
            LocalDate dateOuverture,
            String registreCommerceUrl,
            String agrementMinistereUrl,
            String assuranceUrl
    ) {}

    public record PharmacienInfo(
            UUID id,
            String nomComplet,
            String telephone,
            String email
    ) {}

    public record SyndicatInfo(
            UUID id,
            String nom,
            String regionNom
    ) {}

    public record ConfigurationInfo(
            String horaires,
            Boolean accepteCommandes,
            Boolean proposeLivraison,
            Integer rayonLivraisonKm,
            Boolean notificationsActives
    ) {}

    public record ImagesInfo(
            String logoUrl,
            String photoFacadeUrl,
            List<String> photosInterieures
    ) {}
}