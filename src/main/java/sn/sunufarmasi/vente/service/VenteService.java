package sn.sunufarmasi.vente.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.pharmacie.entity.Pharmacie;
import sn.sunufarmasi.pharmacie.repository.PharmacieRepositorye;
import sn.sunufarmasi.stock.entity.MouvementStock;
import sn.sunufarmasi.stock.entity.ProduitPharmacie;
import sn.sunufarmasi.stock.enums.TypeMouvement;
import sn.sunufarmasi.stock.repository.MouvementStockRepository;
import sn.sunufarmasi.stock.repository.ProduitPharmacieRepository;
import sn.sunufarmasi.vente.dto.request.*;
import sn.sunufarmasi.vente.dto.response.*;
import sn.sunufarmasi.vente.entity.LigneVente;
import sn.sunufarmasi.vente.entity.Vente;
import sn.sunufarmasi.vente.enums.StatutVente;
import sn.sunufarmasi.vente.enums.TypeVente;
import sn.sunufarmasi.vente.mapper.VenteMapper;
import sn.sunufarmasi.vente.repository.LigneVenteRepository;
import sn.sunufarmasi.vente.repository.VenteRepository;
import sn.sunufarmasi.shared.exception.BadRequestException;
import sn.sunufarmasi.shared.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des ventes
 *
 * @author WeCan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VenteService {

    private final VenteRepository venteRepository;
    private final LigneVenteRepository ligneVenteRepository;
    private final PharmacieRepositorye pharmacieRepository;
    private final ProduitPharmacieRepository produitPharmacieRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final VenteMapper venteMapper;

    // ═══════════════════════════════════════════════════════════
    // CRÉATION DE VENTE
    // ═══════════════════════════════════════════════════════════

    /**
     * Créer une nouvelle vente
     */
    public VenteDetailResponse create(UUID pharmacieId, CreateVenteRequest request,
                                       UUID vendeurId, String vendeurNom) {
        log.info("Création vente pour pharmacie {}", pharmacieId);

        Pharmacie pharmacie = findPharmacieOrThrow(pharmacieId);

        // Créer la vente
        Vente vente = Vente.builder()
                .pharmacie(pharmacie)
                .type(request.type() != null ? request.type() : TypeVente.COMPTOIR)
                .statut(StatutVente.EN_COURS)
                .dateVente(LocalDateTime.now())
                .vendeurId(vendeurId)
                .vendeurNom(vendeurNom)
                // Client
                .clientId(request.clientId())
                .clientNom(request.clientNom())
                .clientTelephone(request.clientTelephone())
                // Ordonnance
                .numeroOrdonnance(request.numeroOrdonnance())
                .medecinPrescripteur(request.medecinPrescripteur())
                .dateOrdonnance(request.dateOrdonnance())
                // Mutuelle
                .mutuelleId(request.mutuelleId())
                .mutuelleNom(request.mutuelleNom())
                .numeroAdherent(request.numeroAdherent())
                .tauxPriseEnCharge(request.tauxPriseEnCharge())
                // Notes
                .notes(request.notes())
                .build();

        // Générer les numéros
        vente.setNumero(generateNumero(pharmacie));
        vente.setNumeroTicket(generateNumeroTicket(pharmacie));

        // Sauvegarder la vente
        vente = venteRepository.save(vente);

        // Ajouter les lignes
        int numeroLigne = 1;
        for (LigneVenteRequest ligneReq : request.lignes()) {
            LigneVente ligne = creerLigne(pharmacieId, vente, ligneReq, numeroLigne++);
            vente.ajouterLigne(ligne);
        }

        // Appliquer la remise si fournie
        if (request.pourcentageRemise() != null) {
            vente.appliquerRemise(request.pourcentageRemise());
        } else if (request.montantRemise() != null) {
            vente.appliquerRemiseMontant(request.montantRemise());
        }

        // Enregistrer le paiement si fourni
        if (request.modePaiement() != null && request.montantPaye() != null) {
            vente.enregistrerPaiement(request.montantPaye(), request.modePaiement(), request.referencePaiement());

            // Si vente validée, décrémenter le stock
            if (vente.getStatut() == StatutVente.VALIDEE) {
                decrementerStock(vente, vendeurId, vendeurNom);
            }
        }

        vente = venteRepository.save(vente);

        log.info("Vente créée: {} - {} FCFA", vente.getNumero(), vente.getMontantNetTTC());

        return venteMapper.toDetailResponse(vente);
    }

    /**
     * Créer une ligne de vente
     */
    private LigneVente creerLigne(UUID pharmacieId, Vente vente, LigneVenteRequest request, int numeroLigne) {
        // Récupérer le produit en stock
        ProduitPharmacie pp = produitPharmacieRepository
                .findByPharmacieIdAndProduitId(pharmacieId, request.produitId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé dans le stock"));

        // Vérifier la disponibilité
        if (pp.getQuantiteDisponible() < request.quantite()) {
            throw new BadRequestException("Stock insuffisant pour " + pp.getProduit().getNom() +
                    ". Disponible: " + pp.getQuantiteDisponible());
        }

        // Créer la ligne
        LigneVente ligne = new LigneVente();
        ligne.setVente(vente);
        ligne.setNumeroLigne(numeroLigne);
        ligne.setQuantite(request.quantite());
        ligne.setQuantiteGratuite(request.quantiteGratuite());

        // Initialiser depuis le produit
        ligne.initialiserDepuisProduitPharmacie(pp);

        // Surcharger le prix si fourni
        if (request.prixUnitaireTTC() != null) {
            ligne.setPrixUnitaireTTC(request.prixUnitaireTTC());
        }

        // Appliquer la remise ligne
        if (request.remisePourcentage() != null) {
            ligne.appliquerRemise(request.remisePourcentage());
        }

        // Lot et péremption
        ligne.setNumeroLot(request.numeroLot());
        ligne.setDatePeremption(request.datePeremption());
        ligne.setPosologie(request.posologie());

        // Calculer les montants
        ligne.calculerMontants();

        return ligne;
    }

    // ═══════════════════════════════════════════════════════════
    // PAIEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Enregistrer un paiement sur une vente
     */
    public VenteDetailResponse enregistrerPaiement(UUID venteId, PaiementRequest request,
                                                    UUID userId, String userName) {
        log.info("Enregistrement paiement sur vente {}: {} FCFA", venteId, request.montant());

        Vente vente = findByIdOrThrow(venteId);

        if (vente.getStatut() == StatutVente.ANNULEE) {
            throw new BadRequestException("Impossible de payer une vente annulée");
        }

        if (vente.estPayee()) {
            throw new BadRequestException("Cette vente est déjà entièrement payée");
        }

        boolean etaitEnCours = vente.getStatut() == StatutVente.EN_COURS;

        vente.enregistrerPaiement(request.montant(), request.modePaiement(), request.reference());

        // Si vente validée pour la première fois, décrémenter le stock
        if (etaitEnCours && vente.getStatut() == StatutVente.VALIDEE) {
            decrementerStock(vente, userId, userName);
        }

        vente = venteRepository.save(vente);

        log.info("Paiement enregistré: {} - Statut: {}", vente.getNumero(), vente.getStatut());

        return venteMapper.toDetailResponse(vente);
    }

    /**
     * Décrémenter le stock après validation
     */
    private void decrementerStock(Vente vente, UUID userId, String userName) {
        for (LigneVente ligne : vente.getLignes()) {
            ProduitPharmacie pp = ligne.getProduitPharmacie();
            if (pp != null) {
                int qteTotale = ligne.getQuantiteTotale();
                int stockAvant = pp.getQuantiteStock();

                // Retirer du stock
                pp.retirerStock(qteTotale);
                produitPharmacieRepository.save(pp);

                // Créer le mouvement
                MouvementStock mvt = MouvementStock.builder()
                        .pharmacie(vente.getPharmacie())
                        .produit(ligne.getProduit())
                        .produitPharmacie(pp)
                        .type(TypeMouvement.VENTE)
                        .quantite(qteTotale)
                        .quantiteAvant(stockAvant)
                        .quantiteApres(pp.getQuantiteStock())
                        .prixUnitaire(ligne.getPrixUnitaireTTC())
                        .montantTotal(ligne.getMontantTTC())
                        .referenceExterne(vente.getNumero())
                        .client(vente.getClientNom())
                        .dateMouvement(LocalDateTime.now())
                        .effectueParId(userId)
                        .effectueParNom(userName)
                        .effectueParRole("VENDEUR")
                        .build();
                mouvementStockRepository.save(mvt);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ANNULATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Annuler une vente
     */
    public VenteDetailResponse annuler(UUID venteId, String motif, UUID userId, String userName) {
        log.info("Annulation vente {}", venteId);

        Vente vente = findByIdOrThrow(venteId);

        if (!vente.peutEtreAnnulee()) {
            throw new BadRequestException("Cette vente ne peut plus être annulée");
        }

        boolean stockDejaDecrement = vente.getStatut() == StatutVente.VALIDEE;

        vente.setStatut(StatutVente.ANNULEE);
        vente.setMotifAnnulation(motif);
        vente.setAnnuleParId(userId);
        vente.setAnnuleParNom(userName);
        vente.setDateAnnulation(LocalDateTime.now());

        // Réintégrer le stock si déjà décrémenté
        if (stockDejaDecrement) {
            reintegrerStock(vente, userId, userName);
        }

        vente = venteRepository.save(vente);

        log.info("Vente annulée: {}", vente.getNumero());

        return venteMapper.toDetailResponse(vente);
    }

    /**
     * Réintégrer le stock après annulation
     */
    private void reintegrerStock(Vente vente, UUID userId, String userName) {
        for (LigneVente ligne : vente.getLignes()) {
            ProduitPharmacie pp = ligne.getProduitPharmacie();
            if (pp != null) {
                int qteTotale = ligne.getQuantiteTotale();
                int stockAvant = pp.getQuantiteStock();

                // Réajouter au stock
                pp.ajouterStock(qteTotale);
                produitPharmacieRepository.save(pp);

                // Créer le mouvement
                MouvementStock mvt = MouvementStock.builder()
                        .pharmacie(vente.getPharmacie())
                        .produit(ligne.getProduit())
                        .produitPharmacie(pp)
                        .type(TypeMouvement.RETOUR_CLIENT)
                        .quantite(qteTotale)
                        .quantiteAvant(stockAvant)
                        .quantiteApres(pp.getQuantiteStock())
                        .prixUnitaire(ligne.getPrixUnitaireTTC())
                        .referenceExterne(vente.getNumero())
                        .motif("Annulation vente: " + vente.getMotifAnnulation())
                        .dateMouvement(LocalDateTime.now())
                        .effectueParId(userId)
                        .effectueParNom(userName)
                        .effectueParRole("ANNULATION")
                        .build();
                mouvementStockRepository.save(mvt);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CONSULTATION
    // ═══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public VenteDetailResponse getById(UUID venteId) {
        Vente vente = findByIdOrThrow(venteId);
        return venteMapper.toDetailResponse(vente);
    }

    @Transactional(readOnly = true)
    public VenteDetailResponse getByNumero(String numero) {
        Vente vente = venteRepository.findByNumero(numero)
                .orElseThrow(() -> new ResourceNotFoundException("Vente non trouvée: " + numero));
        return venteMapper.toDetailResponse(vente);
    }

    @Transactional(readOnly = true)
    public Page<VenteResponse> getByPharmacie(UUID pharmacieId, Pageable pageable) {
        return venteRepository.findByPharmacieIdOrderByDateVenteDesc(pharmacieId, pageable)
                .map(venteMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<VenteResponse> getByPharmacieAndPeriode(UUID pharmacieId, LocalDate debut, LocalDate fin) {
        LocalDateTime dateDebut = debut.atStartOfDay();
        LocalDateTime dateFin = fin.atTime(LocalTime.MAX);
        return venteRepository.findByPharmacieAndPeriode(pharmacieId, dateDebut, dateFin).stream()
                .map(venteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VenteResponse> getVentesDuJour(UUID pharmacieId) {
        LocalDate aujourdhui = LocalDate.now();
        return getByPharmacieAndPeriode(pharmacieId, aujourdhui, aujourdhui);
    }

    // ═══════════════════════════════════════════════════════════
    // TICKET DE CAISSE
    // ═══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public TicketCaisseResponse getTicket(UUID venteId) {
        Vente vente = findByIdOrThrow(venteId);
        return venteMapper.toTicketResponse(vente);
    }

    // ═══════════════════════════════════════════════════════════
    // STATISTIQUES
    // ═══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public StatistiquesVenteResponse getStatistiques(UUID pharmacieId, LocalDate debut, LocalDate fin) {
        LocalDateTime dateDebut = debut.atStartOfDay();
        LocalDateTime dateFin = fin.atTime(LocalTime.MAX);

        // KPIs principaux
        BigDecimal ca = venteRepository.sumChiffreAffaires(pharmacieId, dateDebut, dateFin);
        Long nombreVentes = venteRepository.countVentes(pharmacieId, dateDebut, dateFin);
        BigDecimal panierMoyen = venteRepository.avgPanierMoyen(pharmacieId, dateDebut, dateFin);
        BigDecimal totalRemises = venteRepository.sumRemises(pharmacieId, dateDebut, dateFin);
        BigDecimal montantMutuelle = venteRepository.sumMontantMutuelle(pharmacieId, dateDebut, dateFin);

        // Par mode de paiement
        Map<String, BigDecimal> ventesParMode = new HashMap<>();
        Map<String, Long> nombreParMode = new HashMap<>();
        venteRepository.statsByModePaiement(pharmacieId, dateDebut, dateFin).forEach(row -> {
            String mode = row[0] != null ? row[0].toString() : "AUTRE";
            ventesParMode.put(mode, (BigDecimal) row[2]);
            nombreParMode.put(mode, (Long) row[1]);
        });

        // Par type
        Map<String, BigDecimal> ventesParType = new HashMap<>();
        Map<String, Long> nombreParType = new HashMap<>();
        venteRepository.statsByType(pharmacieId, dateDebut, dateFin).forEach(row -> {
            String type = row[0] != null ? row[0].toString() : "AUTRE";
            ventesParType.put(type, (BigDecimal) row[2]);
            nombreParType.put(type, (Long) row[1]);
        });

        // Par vendeur
        List<StatistiquesVenteResponse.StatVendeur> statsVendeurs = venteRepository
                .statsByVendeur(pharmacieId, dateDebut, dateFin).stream()
                .map(row -> new StatistiquesVenteResponse.StatVendeur(
                        (UUID) row[0],
                        (String) row[1],
                        (Long) row[2],
                        (BigDecimal) row[3],
                        ((Long) row[2]) > 0 ? ((BigDecimal) row[3]).divide(BigDecimal.valueOf((Long) row[2]), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO
                ))
                .collect(Collectors.toList());

        // Évolution journalière
        List<StatistiquesVenteResponse.StatJour> evolution = venteRepository
                .statsParJour(pharmacieId, dateDebut, dateFin).stream()
                .map(row -> new StatistiquesVenteResponse.StatJour(
                        ((java.sql.Date) row[0]).toLocalDate(),
                        (Long) row[1],
                        (BigDecimal) row[2]
                ))
                .collect(Collectors.toList());

        // Top produits
        List<StatistiquesVenteResponse.TopProduit> topQte = ligneVenteRepository
                .topProduitsParQuantite(pharmacieId, dateDebut, dateFin).stream()
                .limit(10)
                .map(row -> new StatistiquesVenteResponse.TopProduit(
                        (UUID) row[0], (String) row[1], ((Long) row[2]).intValue(), (BigDecimal) row[3]
                ))
                .collect(Collectors.toList());

        List<StatistiquesVenteResponse.TopProduit> topCA = ligneVenteRepository
                .topProduitsParCA(pharmacieId, dateDebut, dateFin).stream()
                .limit(10)
                .map(row -> new StatistiquesVenteResponse.TopProduit(
                        (UUID) row[0], (String) row[1], ((Long) row[2]).intValue(), (BigDecimal) row[3]
                ))
                .collect(Collectors.toList());

        return new StatistiquesVenteResponse(
                debut, fin,
                ca, nombreVentes, panierMoyen, 0,
                totalRemises, BigDecimal.ZERO,
                montantMutuelle, 0L,
                ventesParMode, nombreParMode,
                ventesParType, nombreParType,
                statsVendeurs, evolution,
                topQte, topCA
        );
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES
    // ═══════════════════════════════════════════════════════════

    private Vente findByIdOrThrow(UUID id) {
        return venteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vente non trouvée: " + id));
    }

    private Pharmacie findPharmacieOrThrow(UUID pharmacieId) {
        return pharmacieRepository.findById(pharmacieId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacie non trouvée: " + pharmacieId));
    }

    private String generateNumero(Pharmacie pharmacie) {
        LocalDateTime now = LocalDateTime.now();
        Long count = venteRepository.countVentesDuJour(pharmacie.getId(), now) + 1;
        String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("VTE-%s-%s-%04d", pharmacie.getCode(), date, count);
    }

    private String generateNumeroTicket(Pharmacie pharmacie) {
        LocalDateTime now = LocalDateTime.now();
        Long count = venteRepository.countVentesDuJour(pharmacie.getId(), now) + 1;
        return String.format("T%04d", count);
    }
}
