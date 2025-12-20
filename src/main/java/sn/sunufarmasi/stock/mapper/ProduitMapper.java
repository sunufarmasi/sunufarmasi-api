package sn.sunufarmasi.stock.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sn.sunufarmasi.stock.dto.response.AlerteStockResponse;
import sn.sunufarmasi.stock.dto.response.MouvementStockResponse;
import sn.sunufarmasi.stock.dto.response.ProduitResponse;
import sn.sunufarmasi.stock.dto.response.StockResponse;
import sn.sunufarmasi.stock.entity.MouvementStock;
import sn.sunufarmasi.stock.entity.Produit;
import sn.sunufarmasi.stock.entity.ProduitPharmacie;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Mapper pour les entités Produit, ProduitPharmacie, MouvementStock
 *
 * @author WeCan
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ProduitMapper {

    // ═══════════════════════════════════════════════════════════
    // PRODUIT (Catalogue)
    // ═══════════════════════════════════════════════════════════

    /**
     * Convertir Produit → ProduitResponse
     */
    public ProduitResponse toProduitResponse(Produit produit) {
        if (produit == null) {
            return null;
        }

        return new ProduitResponse(
                produit.getId(),
                produit.getCode(),
                produit.getCodeBarre(),

                // Dénomination
                produit.getNom(),
                produit.getNomComplet(),
                produit.getDci(),
                produit.getDescription(),

                // Classification
                produit.getCategorie(),
                produit.getCategorie() != null ? produit.getCategorie().getLibelle() : null,
                produit.getForme(),
                produit.getForme() != null ? produit.getForme().getLibelle() : null,
                produit.getDosage(),
                produit.getUniteVente(),
                produit.getContenance(),

                // Fabricant
                produit.getLaboratoire(),
                produit.getPaysOrigine(),

                // Prix
                produit.getPrixPublicTTC(),
                produit.getPrixAchatHT(),
                produit.getTauxTVA(),
                produit.getTauxRemboursement(),

                // Réglementation
                produit.getSurOrdonnance(),
                produit.getListeMedicament(),
                produit.getEstGenerique(),
                produit.getEstRemboursable(),

                // Conservation
                produit.getTemperatureConservation(),
                produit.getChaineFroid(),

                // Images
                produit.getImageUrl(),

                // Statut
                produit.getStatut()
        );
    }

    // ═══════════════════════════════════════════════════════════
    // STOCK (ProduitPharmacie)
    // ═══════════════════════════════════════════════════════════

    /**
     * Convertir ProduitPharmacie → StockResponse
     */
    public StockResponse toStockResponse(ProduitPharmacie pp) {
        if (pp == null) {
            return null;
        }

        Produit produit = pp.getProduit();

        return new StockResponse(
                pp.getId(),

                // Produit
                new StockResponse.ProduitInfo(
                        produit.getId(),
                        produit.getCode(),
                        produit.getCodeBarre(),
                        produit.getNom(),
                        produit.getNomComplet(),
                        produit.getDci(),
                        produit.getCategorie(),
                        produit.getForme(),
                        produit.getDosage(),
                        produit.getLaboratoire(),
                        produit.getSurOrdonnance(),
                        produit.getImageUrl()
                ),

                // Stock
                pp.getQuantiteStock(),
                pp.getQuantiteReservee(),
                pp.getQuantiteDisponible(),

                // Alertes
                pp.getSeuilAlerte(),
                pp.getSeuilReappro(),
                pp.stockBas(),
                pp.enRupture(),
                pp.aReapprovisionner(),

                // Péremption
                pp.getDatePeremptionProche(),
                pp.getAlertePeremptionJours(),
                pp.bientotPerime(),
                pp.estPerime(),

                // Prix pharmacie
                pp.getPrixVenteTTC(),
                pp.getPrixAchatHT(),
                pp.getMargePourcentage(),
                pp.getMargeUnitaire(),
                pp.getValeurStock(),

                // Emplacement
                pp.getEmplacementComplet(),

                // Paramètres
                pp.getEstActif(),
                pp.getVenteAutorisee(),
                pp.peutEtreVendu(),

                // Stats
                pp.getTotalVendu(),
                pp.getDerniereVente(),
                pp.getDerniereEntree()
        );
    }

    /**
     * Convertir ProduitPharmacie → AlerteStockResponse.ProduitAlerte
     */
    public AlerteStockResponse.ProduitAlerte toAlerteResponse(ProduitPharmacie pp, String typeAlerte) {
        if (pp == null) {
            return null;
        }

        Produit produit = pp.getProduit();
        Integer joursRestants = null;

        if (pp.getDatePeremptionProche() != null) {
            joursRestants = (int) ChronoUnit.DAYS.between(LocalDate.now(), pp.getDatePeremptionProche());
        }

        return new AlerteStockResponse.ProduitAlerte(
                pp.getId(),
                produit.getId(),
                produit.getCode(),
                produit.getNom(),
                produit.getNomComplet(),
                pp.getQuantiteStock(),
                pp.getSeuilAlerte(),
                pp.getSeuilReappro(),
                pp.getDatePeremptionProche(),
                joursRestants,
                pp.getPrixAchatHT(),
                pp.getValeurStock(),
                pp.getEmplacementComplet(),
                typeAlerte
        );
    }

    // ═══════════════════════════════════════════════════════════
    // MOUVEMENT STOCK
    // ═══════════════════════════════════════════════════════════

    /**
     * Convertir MouvementStock → MouvementStockResponse
     */
    public MouvementStockResponse toMouvementResponse(MouvementStock mvt) {
        if (mvt == null) {
            return null;
        }

        Produit produit = mvt.getProduit();

        return new MouvementStockResponse(
                mvt.getId(),

                // Type et quantité
                mvt.getType(),
                mvt.getType() != null ? mvt.getType().getLibelle() : null,
                mvt.isEntree(),
                mvt.getQuantite(),
                mvt.getQuantiteAvant(),
                mvt.getQuantiteApres(),
                mvt.getImpact(),

                // Produit
                produit != null ? produit.getId() : null,
                produit != null ? produit.getCode() : null,
                produit != null ? produit.getNom() : null,

                // Lot et péremption
                mvt.getNumeroLot(),
                mvt.getDatePeremption(),

                // Prix
                mvt.getPrixUnitaire(),
                mvt.getMontantTotal(),

                // Références
                mvt.getReferenceExterne(),
                mvt.getFournisseur(),
                mvt.getClient(),

                // Informations
                mvt.getMotif(),
                mvt.getNotes(),

                // Traçabilité
                mvt.getDateMouvement(),
                mvt.getEffectueParId(),
                mvt.getEffectueParNom(),
                mvt.getEffectueParRole()
        );
    }
}
