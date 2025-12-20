package sn.sunufarmasi.pharmacie.dto.response;

import sn.sunufarmasi.pharmacie.enums.StatutPharmacie;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO Response pour une pharmacie (version simple)
 *
 * @author WeCan
 * @since 1.0.0
 */
public record PharmacieResponse(
        UUID id,
        String code,
        String nom,
        String raisonSociale,

        // Localisation
        UUID communeId,
        String communeNom,
        String adresseComplete,
        String quartier,
        Double latitude,
        Double longitude,
        String lienGoogleMaps,

        // Contact
        String telephone,
        String telephoneSecondaire,
        String email,
        String siteWeb,

        // Documents
        String numeroAgrementMinistere,
        String numeroOrdre,
        LocalDate dateOuverture,

        // Propriétaire
        UUID pharmacienProprietaireId,
        String pharmacienProprietaireNom,

        // Syndicat
        UUID syndicatId,
        String syndicatNom,

        // Statut
        StatutPharmacie statut,
        LocalDateTime dateValidation,

        // Configuration
        Boolean accepteCommandes,
        Boolean proposeLivraison,
        Integer rayonLivraisonKm,

        // Images
        String logoUrl,
        String photoFacadeUrl,

        // Métadonnées
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Vérifie si la pharmacie est opérationnelle
     */
    public boolean isOperationnelle() {
        return statut == StatutPharmacie.VALIDEE;
    }
}