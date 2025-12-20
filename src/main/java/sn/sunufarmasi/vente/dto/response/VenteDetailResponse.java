package sn.sunufarmasi.vente.dto.response;

import sn.sunufarmasi.vente.enums.ModePaiement;
import sn.sunufarmasi.vente.enums.StatutVente;
import sn.sunufarmasi.vente.enums.TypeVente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO Response pour une vente (version détaillée)
 *
 * @author WeCan
 * @since 1.0.0
 */
public record VenteDetailResponse(

        UUID id,
        String numero,
        String numeroTicket,

        // Pharmacie
        PharmacieInfo pharmacie,

        // Type et statut
        TypeVente type,
        String typeLibelle,
        StatutVente statut,
        String statutLibelle,

        // Client
        ClientInfo client,

        // Ordonnance
        OrdonnanceInfo ordonnance,

        // Mutuelle
        MutuelleInfo mutuelle,

        // Montants
        MontantsInfo montants,

        // Paiement
        PaiementInfo paiement,

        // Lignes
        List<LigneVenteResponse> lignes,
        Integer nombreArticles,

        // Vendeur
        VendeurInfo vendeur,

        // Annulation
        AnnulationInfo annulation,

        // Notes et dates
        String notes,
        LocalDateTime dateVente,
        LocalDateTime createdAt

) {
    public record PharmacieInfo(
            UUID id,
            String code,
            String nom
    ) {}

    public record ClientInfo(
            UUID id,
            String nom,
            String telephone
    ) {}

    public record OrdonnanceInfo(
            String numero,
            String medecinPrescripteur,
            LocalDateTime date
    ) {}

    public record MutuelleInfo(
            UUID id,
            String nom,
            String numeroAdherent,
            BigDecimal tauxPriseEnCharge
    ) {}

    public record MontantsInfo(
            BigDecimal brutHT,
            BigDecimal tva,
            BigDecimal brutTTC,
            BigDecimal remise,
            BigDecimal pourcentageRemise,
            BigDecimal netTTC,
            BigDecimal partMutuelle,
            BigDecimal partClient
    ) {}

    public record PaiementInfo(
            ModePaiement mode,
            String modeLibelle,
            BigDecimal montantPaye,
            BigDecimal montantRendu,
            BigDecimal resteAPayer,
            String reference,
            LocalDateTime date,
            boolean estPayee
    ) {}

    public record VendeurInfo(
            UUID id,
            String nom
    ) {}

    public record AnnulationInfo(
            String motif,
            UUID annuleParId,
            String annuleParNom,
            LocalDateTime date
    ) {}

    /**
     * Ligne de vente
     */
    public record LigneVenteResponse(
            UUID id,
            Integer numeroLigne,
            UUID produitId,
            String produitCode,
            String produitNom,
            String produitDci,
            Integer quantite,
            Integer quantiteGratuite,
            BigDecimal prixUnitaireTTC,
            BigDecimal remisePourcentage,
            BigDecimal remiseMontant,
            BigDecimal montantTTC,
            String numeroLot,
            LocalDate datePeremption,
            Boolean surOrdonnance,
            String posologie,
            Boolean estRemboursable,
            BigDecimal montantRemboursable
    ) {}
}
