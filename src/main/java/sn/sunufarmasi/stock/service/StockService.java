package sn.sunufarmasi.stock.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.pharmacie.entity.Pharmacie;
import sn.sunufarmasi.pharmacie.repository.PharmacieRepositorye;
import sn.sunufarmasi.shared.exception.BadRequestException;
import sn.sunufarmasi.shared.exception.ConflictException;
import sn.sunufarmasi.shared.exception.ResourceNotFoundException;
import sn.sunufarmasi.stock.dto.request.*;
import sn.sunufarmasi.stock.dto.response.AlerteStockResponse;
import sn.sunufarmasi.stock.dto.response.MouvementStockResponse;
import sn.sunufarmasi.stock.dto.response.StockResponse;
import sn.sunufarmasi.stock.entity.MouvementStock;
import sn.sunufarmasi.stock.entity.Produit;
import sn.sunufarmasi.stock.entity.ProduitPharmacie;
import sn.sunufarmasi.stock.enums.TypeMouvement;
import sn.sunufarmasi.stock.mapper.ProduitMapper;
import sn.sunufarmasi.stock.repository.MouvementStockRepository;
import sn.sunufarmasi.stock.repository.ProduitPharmacieRepository;
import sn.sunufarmasi.stock.repository.ProduitRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service pour la gestion du stock par pharmacie
 *
 * @author WeCan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StockService {

    private final ProduitPharmacieRepository produitPharmacieRepository;
    private final ProduitRepository produitRepository;
    private final PharmacieRepositorye pharmacieRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final ProduitMapper produitMapper;

    // ═══════════════════════════════════════════════════════════
    // AJOUT PRODUIT AU STOCK
    // ═══════════════════════════════════════════════════════════

    /**
     * Ajouter un produit au stock d'une pharmacie
     */
    public StockResponse ajouterProduit(UUID pharmacieId, AjouterStockRequest request, UUID userId, String userName) {
        log.info("Ajout produit {} au stock pharmacie {}", request.produitId(), pharmacieId);

        // Vérifier que le produit n'existe pas déjà
        if (produitPharmacieRepository.existsByPharmacieIdAndProduitId(pharmacieId, request.produitId())) {
            throw new ConflictException("Ce produit est déjà dans le stock de cette pharmacie");
        }

        Pharmacie pharmacie = findPharmacieOrThrow(pharmacieId);
        Produit produit = findProduitOrThrow(request.produitId());

        // Créer le ProduitPharmacie
        ProduitPharmacie pp = ProduitPharmacie.builder()
                .pharmacie(pharmacie)
                .produit(produit)
                .quantiteStock(request.quantiteInitiale() != null ? request.quantiteInitiale() : 0)
                .prixVenteTTC(request.prixVenteTTC() != null ? request.prixVenteTTC() : produit.getPrixPublicTTC())
                .prixAchatHT(request.prixAchatHT() != null ? request.prixAchatHT() : produit.getPrixAchatHT())
                .seuilAlerte(request.seuilAlerte() != null ? request.seuilAlerte() : 10)
                .seuilReappro(request.seuilReappro() != null ? request.seuilReappro() : 20)
                .quantiteOptimale(request.quantiteOptimale())
                .datePeremptionProche(request.datePeremption())
                .alertePeremptionJours(request.alertePeremptionJours() != null ? request.alertePeremptionJours() : 90)
                .emplacement(request.emplacement())
                .rayon(request.rayon())
                .etagere(request.etagere())
                .estActif(true)
                .venteAutorisee(true)
                .build();

        ProduitPharmacie saved = produitPharmacieRepository.save(pp);

        // Créer le mouvement d'initialisation si quantité > 0
        if (request.quantiteInitiale() != null && request.quantiteInitiale() > 0) {
            MouvementStock mvt = MouvementStock.builder()
                    .pharmacie(pharmacie)
                    .produit(produit)
                    .produitPharmacie(saved)
                    .type(TypeMouvement.INITIALISATION)
                    .quantite(request.quantiteInitiale())
                    .quantiteAvant(0)
                    .quantiteApres(request.quantiteInitiale())
                    .numeroLot(request.numeroLot())
                    .datePeremption(request.datePeremption())
                    .prixUnitaire(request.prixAchatHT())
                    .dateMouvement(LocalDateTime.now())
                    .effectueParId(userId)
                    .effectueParNom(userName)
                    .motif("Stock initial")
                    .build();
            mouvementStockRepository.save(mvt);
        }

        log.info("Produit {} ajouté au stock pharmacie {}", produit.getCode(), pharmacie.getCode());

        return produitMapper.toStockResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    // ENTRÉES DE STOCK
    // ═══════════════════════════════════════════════════════════

    /**
     * Enregistrer une entrée de stock
     */
    public MouvementStockResponse entreeStock(UUID pharmacieId, EntreeStockRequest request, UUID userId, String userName, String userRole) {
        log.info("Entrée stock pharmacie {}: {} x {} ({})",
                pharmacieId, request.quantite(), request.produitId(), request.type());

        if (!request.isTypeValide()) {
            throw new BadRequestException("Le type de mouvement n'est pas une entrée valide");
        }

        Pharmacie pharmacie = findPharmacieOrThrow(pharmacieId);
        ProduitPharmacie pp = findProduitPharmacieOrThrow(pharmacieId, request.produitId());

        int stockAvant = pp.getQuantiteStock();

        // Mettre à jour le stock
        pp.ajouterStock(request.quantite());

        // Mettre à jour la date de péremption si fournie
        if (request.datePeremption() != null) {
            if (pp.getDatePeremptionProche() == null || request.datePeremption().isBefore(pp.getDatePeremptionProche())) {
                pp.setDatePeremptionProche(request.datePeremption());
            }
        }

        // Mettre à jour le prix d'achat si fourni
        if (request.prixUnitaire() != null) {
            pp.setPrixAchatHT(request.prixUnitaire());
        }

        produitPharmacieRepository.save(pp);

        // Créer le mouvement
        MouvementStock mvt = MouvementStock.builder()
                .pharmacie(pharmacie)
                .produit(pp.getProduit())
                .produitPharmacie(pp)
                .type(request.type())
                .quantite(request.quantite())
                .quantiteAvant(stockAvant)
                .quantiteApres(pp.getQuantiteStock())
                .numeroLot(request.numeroLot())
                .datePeremption(request.datePeremption())
                .prixUnitaire(request.prixUnitaire())
                .referenceExterne(request.referenceExterne())
                .fournisseur(request.fournisseur())
                .motif(request.motif())
                .notes(request.notes())
                .dateMouvement(LocalDateTime.now())
                .effectueParId(userId)
                .effectueParNom(userName)
                .effectueParRole(userRole)
                .build();

        MouvementStock savedMvt = mouvementStockRepository.save(mvt);

        log.info("Entrée stock enregistrée: {} ({} → {})", pp.getProduit().getCode(), stockAvant, pp.getQuantiteStock());

        return produitMapper.toMouvementResponse(savedMvt);
    }

    // ═══════════════════════════════════════════════════════════
    // SORTIES DE STOCK
    // ═══════════════════════════════════════════════════════════

    /**
     * Enregistrer une sortie de stock
     */
    public MouvementStockResponse sortieStock(UUID pharmacieId, SortieStockRequest request, UUID userId, String userName, String userRole) {
        log.info("Sortie stock pharmacie {}: {} x {} ({})",
                pharmacieId, request.quantite(), request.produitId(), request.type());

        if (!request.isTypeValide()) {
            throw new BadRequestException("Le type de mouvement n'est pas une sortie valide");
        }

        Pharmacie pharmacie = findPharmacieOrThrow(pharmacieId);
        ProduitPharmacie pp = findProduitPharmacieOrThrow(pharmacieId, request.produitId());

        // Vérifier stock disponible
        if (pp.getQuantiteDisponible() < request.quantite()) {
            throw new BadRequestException("Stock insuffisant. Disponible: " + pp.getQuantiteDisponible());
        }

        int stockAvant = pp.getQuantiteStock();

        // Retirer du stock
        pp.retirerStock(request.quantite());
        produitPharmacieRepository.save(pp);

        // Créer le mouvement
        MouvementStock mvt = MouvementStock.builder()
                .pharmacie(pharmacie)
                .produit(pp.getProduit())
                .produitPharmacie(pp)
                .type(request.type())
                .quantite(request.quantite())
                .quantiteAvant(stockAvant)
                .quantiteApres(pp.getQuantiteStock())
                .numeroLot(request.numeroLot())
                .prixUnitaire(request.prixUnitaire() != null ? request.prixUnitaire() : pp.getPrixVenteTTC())
                .referenceExterne(request.referenceExterne())
                .client(request.client())
                .motif(request.motif())
                .notes(request.notes())
                .dateMouvement(LocalDateTime.now())
                .effectueParId(userId)
                .effectueParNom(userName)
                .effectueParRole(userRole)
                .build();

        MouvementStock savedMvt = mouvementStockRepository.save(mvt);

        log.info("Sortie stock enregistrée: {} ({} → {})", pp.getProduit().getCode(), stockAvant, pp.getQuantiteStock());

        return produitMapper.toMouvementResponse(savedMvt);
    }

    // ═══════════════════════════════════════════════════════════
    // AJUSTEMENT INVENTAIRE
    // ═══════════════════════════════════════════════════════════

    /**
     * Ajuster le stock suite à un inventaire
     */
    public MouvementStockResponse ajusterStock(UUID pharmacieId, AjustementStockRequest request, UUID userId, String userName) {
        log.info("Ajustement stock pharmacie {}: {} → {}", pharmacieId, request.produitId(), request.nouvelleQuantite());

        Pharmacie pharmacie = findPharmacieOrThrow(pharmacieId);
        ProduitPharmacie pp = findProduitPharmacieOrThrow(pharmacieId, request.produitId());

        int stockAvant = pp.getQuantiteStock();
        int difference = request.nouvelleQuantite() - stockAvant;

        if (difference == 0) {
            throw new BadRequestException("La nouvelle quantité est identique à l'actuelle");
        }

        // Déterminer le type de mouvement
        TypeMouvement type = difference > 0 ? TypeMouvement.AJUSTEMENT_POSITIF : TypeMouvement.AJUSTEMENT_NEGATIF;

        // Mettre à jour le stock
        pp.setQuantiteStock(request.nouvelleQuantite());

        // Mettre à jour la péremption si fournie
        if (request.datePeremption() != null) {
            pp.setDatePeremptionProche(request.datePeremption());
        }

        produitPharmacieRepository.save(pp);

        // Créer le mouvement
        MouvementStock mvt = MouvementStock.builder()
                .pharmacie(pharmacie)
                .produit(pp.getProduit())
                .produitPharmacie(pp)
                .type(type)
                .quantite(Math.abs(difference))
                .quantiteAvant(stockAvant)
                .quantiteApres(request.nouvelleQuantite())
                .motif(request.motif())
                .notes(request.notes())
                .dateMouvement(LocalDateTime.now())
                .effectueParId(userId)
                .effectueParNom(userName)
                .effectueParRole("INVENTAIRE")
                .build();

        MouvementStock savedMvt = mouvementStockRepository.save(mvt);

        log.info("Ajustement stock: {} ({} → {})", pp.getProduit().getCode(), stockAvant, request.nouvelleQuantite());

        return produitMapper.toMouvementResponse(savedMvt);
    }

    // ═══════════════════════════════════════════════════════════
    // CONSULTATION STOCK
    // ═══════════════════════════════════════════════════════════

    /**
     * Récupérer le stock d'une pharmacie
     */
    @Transactional(readOnly = true)
    public List<StockResponse> getStock(UUID pharmacieId) {
        return produitPharmacieRepository.findByPharmacieIdAndEstActif(pharmacieId, true).stream()
                .map(produitMapper::toStockResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer le stock avec pagination
     */
    @Transactional(readOnly = true)
    public Page<StockResponse> getStock(UUID pharmacieId, Pageable pageable) {
        return produitPharmacieRepository.findByPharmacieId(pharmacieId, pageable)
                .map(produitMapper::toStockResponse);
    }

    /**
     * Rechercher dans le stock
     */
    @Transactional(readOnly = true)
    public List<StockResponse> searchStock(UUID pharmacieId, String search) {
        return produitPharmacieRepository.searchInPharmacie(pharmacieId, search).stream()
                .map(produitMapper::toStockResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer un produit du stock par code barre
     */
    @Transactional(readOnly = true)
    public StockResponse getByCodeBarre(UUID pharmacieId, String codeBarre) {
        ProduitPharmacie pp = produitPharmacieRepository.findByPharmacieIdAndCodeBarre(pharmacieId, codeBarre)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé dans le stock avec ce code barre"));
        return produitMapper.toStockResponse(pp);
    }

    // ═══════════════════════════════════════════════════════════
    // ALERTES
    // ═══════════════════════════════════════════════════════════

    /**
     * Récupérer toutes les alertes de stock
     */
    @Transactional(readOnly = true)
    public AlerteStockResponse getAlertes(UUID pharmacieId) {
        LocalDate aujourdhui = LocalDate.now();
        LocalDate dateLimitePeremption = aujourdhui.plusDays(90);

        // Récupérer les différentes alertes
        List<ProduitPharmacie> ruptures = produitPharmacieRepository.findEnRupture(pharmacieId);
        List<ProduitPharmacie> stocksBas = produitPharmacieRepository.findStockBas(pharmacieId);
        List<ProduitPharmacie> aReappro = produitPharmacieRepository.findAReapprovisionner(pharmacieId);
        List<ProduitPharmacie> perimes = produitPharmacieRepository.findPerimes(pharmacieId, aujourdhui);
        List<ProduitPharmacie> bientotPerimes = produitPharmacieRepository.findBientotPerimes(pharmacieId, aujourdhui, dateLimitePeremption);

        // Calculer la valeur des périmés
        BigDecimal valeurPerimes = perimes.stream()
                .map(ProduitPharmacie::getValeurStock)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Construire la réponse
        return new AlerteStockResponse(
                new AlerteStockResponse.ResumeAlertes(
                        ruptures.size() + stocksBas.size() + perimes.size() + bientotPerimes.size(),
                        ruptures.size(),
                        stocksBas.size(),
                        aReappro.size(),
                        perimes.size(),
                        bientotPerimes.size(),
                        valeurPerimes
                ),
                ruptures.stream().map(pp -> produitMapper.toAlerteResponse(pp, "RUPTURE")).collect(Collectors.toList()),
                stocksBas.stream().map(pp -> produitMapper.toAlerteResponse(pp, "STOCK_BAS")).collect(Collectors.toList()),
                aReappro.stream().map(pp -> produitMapper.toAlerteResponse(pp, "A_REAPPROVISIONNER")).collect(Collectors.toList()),
                perimes.stream().map(pp -> produitMapper.toAlerteResponse(pp, "PERIME")).collect(Collectors.toList()),
                bientotPerimes.stream().map(pp -> produitMapper.toAlerteResponse(pp, "BIENTOT_PERIME")).collect(Collectors.toList())
        );
    }

    // ═══════════════════════════════════════════════════════════
    // HISTORIQUE MOUVEMENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Récupérer l'historique des mouvements
     */
    @Transactional(readOnly = true)
    public Page<MouvementStockResponse> getHistorique(UUID pharmacieId, Pageable pageable) {
        return mouvementStockRepository.findByPharmacieIdOrderByDateMouvementDesc(pharmacieId, pageable)
                .map(produitMapper::toMouvementResponse);
    }

    /**
     * Récupérer l'historique d'un produit
     */
    @Transactional(readOnly = true)
    public List<MouvementStockResponse> getHistoriqueProduit(UUID pharmacieId, UUID produitId) {
        return mouvementStockRepository.findByPharmacieAndProduit(pharmacieId, produitId).stream()
                .map(produitMapper::toMouvementResponse)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    // MISE À JOUR PARAMÈTRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Modifier les paramètres d'un produit en stock
     */
    public StockResponse updateParams(UUID pharmacieId, UUID produitId, UpdateStockParamsRequest request) {
        log.info("Modification paramètres stock: pharmacie={}, produit={}", pharmacieId, produitId);

        ProduitPharmacie pp = findProduitPharmacieOrThrow(pharmacieId, produitId);

        if (request.prixVenteTTC() != null) pp.setPrixVenteTTC(request.prixVenteTTC());
        if (request.prixAchatHT() != null) pp.setPrixAchatHT(request.prixAchatHT());
        if (request.margePourcentage() != null) pp.setMargePourcentage(request.margePourcentage());
        if (request.seuilAlerte() != null) pp.setSeuilAlerte(request.seuilAlerte());
        if (request.seuilReappro() != null) pp.setSeuilReappro(request.seuilReappro());
        if (request.quantiteOptimale() != null) pp.setQuantiteOptimale(request.quantiteOptimale());
        if (request.datePeremptionProche() != null) pp.setDatePeremptionProche(request.datePeremptionProche());
        if (request.alertePeremptionJours() != null) pp.setAlertePeremptionJours(request.alertePeremptionJours());
        if (request.emplacement() != null) pp.setEmplacement(request.emplacement());
        if (request.rayon() != null) pp.setRayon(request.rayon());
        if (request.etagere() != null) pp.setEtagere(request.etagere());
        if (request.estActif() != null) pp.setEstActif(request.estActif());
        if (request.venteAutorisee() != null) pp.setVenteAutorisee(request.venteAutorisee());
        if (request.commandeAuto() != null) pp.setCommandeAuto(request.commandeAuto());

        ProduitPharmacie saved = produitPharmacieRepository.save(pp);

        log.info("Paramètres stock modifiés: {}", pp.getProduit().getCode());

        return produitMapper.toStockResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES
    // ═══════════════════════════════════════════════════════════

    private Pharmacie findPharmacieOrThrow(UUID pharmacieId) {
        return pharmacieRepository.findById(pharmacieId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacie non trouvée: " + pharmacieId));
    }

    private Produit findProduitOrThrow(UUID produitId) {
        return produitRepository.findById(produitId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé: " + produitId));
    }

    private ProduitPharmacie findProduitPharmacieOrThrow(UUID pharmacieId, UUID produitId) {
        return produitPharmacieRepository.findByPharmacieIdAndProduitId(pharmacieId, produitId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé dans le stock de cette pharmacie"));
    }
}
