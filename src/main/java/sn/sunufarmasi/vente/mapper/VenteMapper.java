package sn.sunufarmasi.vente.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sn.sunufarmasi.vente.dto.response.*;
import sn.sunufarmasi.vente.entity.LigneVente;
import sn.sunufarmasi.vente.entity.Vente;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour les entités Vente et LigneVente
 *
 * @author WeCan
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class VenteMapper {

    /**
     * Convertir Vente → VenteResponse (version simple)
     */
    public VenteResponse toResponse(Vente vente) {
        if (vente == null) {
            return null;
        }

        return new VenteResponse(
                vente.getId(),
                vente.getNumero(),
                vente.getNumeroTicket(),

                // Type et statut
                vente.getType(),
                vente.getType() != null ? vente.getType().getLibelle() : null,
                vente.getStatut(),
                vente.getStatut() != null ? vente.getStatut().getLibelle() : null,

                // Client
                vente.getClientId(),
                vente.getClientNom(),
                vente.getClientTelephone(),

                // Montants
                vente.getMontantBrutTTC(),
                vente.getMontantRemise(),
                vente.getMontantNetTTC(),
                vente.getMontantMutuelle(),
                vente.getMontantClient(),
                vente.getMontantPaye(),
                vente.getResteAPayer(),

                // Paiement
                vente.getModePaiement(),
                vente.getModePaiement() != null ? vente.getModePaiement().getLibelle() : null,
                vente.estPayee(),

                // Stats
                vente.getNombreArticles(),

                // Vendeur
                vente.getVendeurNom(),

                // Dates
                vente.getDateVente(),
                vente.getDatePaiement()
        );
    }

    /**
     * Convertir Vente → VenteDetailResponse (version complète)
     */
    public VenteDetailResponse toDetailResponse(Vente vente) {
        if (vente == null) {
            return null;
        }

        return new VenteDetailResponse(
                vente.getId(),
                vente.getNumero(),
                vente.getNumeroTicket(),

                // Pharmacie
                vente.getPharmacie() != null ? new VenteDetailResponse.PharmacieInfo(
                        vente.getPharmacie().getId(),
                        vente.getPharmacie().getCode(),
                        vente.getPharmacie().getNom()
                ) : null,

                // Type et statut
                vente.getType(),
                vente.getType() != null ? vente.getType().getLibelle() : null,
                vente.getStatut(),
                vente.getStatut() != null ? vente.getStatut().getLibelle() : null,

                // Client
                new VenteDetailResponse.ClientInfo(
                        vente.getClientId(),
                        vente.getClientNom(),
                        vente.getClientTelephone()
                ),

                // Ordonnance
                vente.getNumeroOrdonnance() != null ? new VenteDetailResponse.OrdonnanceInfo(
                        vente.getNumeroOrdonnance(),
                        vente.getMedecinPrescripteur(),
                        vente.getDateOrdonnance()
                ) : null,

                // Mutuelle
                vente.getMutuelleId() != null ? new VenteDetailResponse.MutuelleInfo(
                        vente.getMutuelleId(),
                        vente.getMutuelleNom(),
                        vente.getNumeroAdherent(),
                        vente.getTauxPriseEnCharge()
                ) : null,

                // Montants
                new VenteDetailResponse.MontantsInfo(
                        vente.getMontantBrutHT(),
                        vente.getMontantTVA(),
                        vente.getMontantBrutTTC(),
                        vente.getMontantRemise(),
                        vente.getPourcentageRemise(),
                        vente.getMontantNetTTC(),
                        vente.getMontantMutuelle(),
                        vente.getMontantClient()
                ),

                // Paiement
                new VenteDetailResponse.PaiementInfo(
                        vente.getModePaiement(),
                        vente.getModePaiement() != null ? vente.getModePaiement().getLibelle() : null,
                        vente.getMontantPaye(),
                        vente.getMontantRendu(),
                        vente.getResteAPayer(),
                        vente.getReferencePaiement(),
                        vente.getDatePaiement(),
                        vente.estPayee()
                ),

                // Lignes
                vente.getLignes() != null ? vente.getLignes().stream()
                        .map(this::toLigneResponse)
                        .collect(Collectors.toList()) : List.of(),
                vente.getNombreArticles(),

                // Vendeur
                new VenteDetailResponse.VendeurInfo(
                        vente.getVendeurId(),
                        vente.getVendeurNom()
                ),

                // Annulation
                vente.getMotifAnnulation() != null ? new VenteDetailResponse.AnnulationInfo(
                        vente.getMotifAnnulation(),
                        vente.getAnnuleParId(),
                        vente.getAnnuleParNom(),
                        vente.getDateAnnulation()
                ) : null,

                // Notes et dates
                vente.getNotes(),
                vente.getDateVente(),
                vente.getCreatedAt()
        );
    }

    /**
     * Convertir LigneVente → LigneVenteResponse
     */
    public VenteDetailResponse.LigneVenteResponse toLigneResponse(LigneVente ligne) {
        if (ligne == null) {
            return null;
        }

        return new VenteDetailResponse.LigneVenteResponse(
                ligne.getId(),
                ligne.getNumeroLigne(),
                ligne.getProduit() != null ? ligne.getProduit().getId() : null,
                ligne.getProduitCode(),
                ligne.getProduitNom(),
                ligne.getProduitDci(),
                ligne.getQuantite(),
                ligne.getQuantiteGratuite(),
                ligne.getPrixUnitaireTTC(),
                ligne.getRemisePourcentage(),
                ligne.getRemiseMontant(),
                ligne.getMontantTTC(),
                ligne.getNumeroLot(),
                ligne.getDatePeremption(),
                ligne.getSurOrdonnance(),
                ligne.getPosologie(),
                ligne.getEstRemboursable(),
                ligne.getMontantRemboursable()
        );
    }

    /**
     * Convertir Vente → TicketCaisseResponse
     */
    public TicketCaisseResponse toTicketResponse(Vente vente) {
        if (vente == null) {
            return null;
        }

        List<TicketCaisseResponse.LigneTicket> lignesTicket = vente.getLignes().stream()
                .map(l -> new TicketCaisseResponse.LigneTicket(
                        l.getProduitNom(),
                        l.getQuantite(),
                        l.getPrixUnitaireTTC(),
                        l.getRemiseMontant(),
                        l.getMontantTTC()
                ))
                .collect(Collectors.toList());

        return new TicketCaisseResponse(
                // En-tête pharmacie
                vente.getPharmacie() != null ? vente.getPharmacie().getNom() : null,
                vente.getPharmacie() != null ? vente.getPharmacie().getAdresseComplete() : null,
                vente.getPharmacie() != null ? vente.getPharmacie().getTelephone() : null,
//                vente.getPharmacie() != null ? vente.getPharmacie().getN() : null,
null,
                // Numéros
                vente.getNumeroTicket(),
                vente.getNumero(),
                vente.getDateVente(),

                // Vendeur
                vente.getVendeurNom(),

                // Client
                vente.getClientNom(),
                vente.getClientTelephone(),

                // Lignes
                lignesTicket,

                // Totaux
                vente.getMontantBrutTTC(),
                vente.getMontantRemise(),
                vente.getMontantNetTTC(),
                vente.getMontantMutuelle(),
                vente.getMontantClient(),
                vente.getMontantPaye(),
                vente.getMontantRendu(),

                // Paiement
                vente.getModePaiement() != null ? vente.getModePaiement().getLibelle() : "Non payé",

                // Messages
                vente.getMontantRemise() != null && vente.getMontantRemise().compareTo(BigDecimal.ZERO) > 0 ?
                        "Vous avez économisé " + vente.getMontantRemise() + " FCFA" : null,
                "Merci de votre visite!"
        );
    }
}
