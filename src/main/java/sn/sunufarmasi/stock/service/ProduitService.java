package sn.sunufarmasi.stock.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.shared.exception.ConflictException;
import sn.sunufarmasi.shared.exception.ResourceNotFoundException;
import sn.sunufarmasi.stock.dto.request.CreateProduitRequest;
import sn.sunufarmasi.stock.dto.request.UpdateProduitRequest;
import sn.sunufarmasi.stock.dto.response.ProduitResponse;
import sn.sunufarmasi.stock.entity.Produit;
import sn.sunufarmasi.stock.enums.CategorieProduit;
import sn.sunufarmasi.stock.enums.FormeProduit;
import sn.sunufarmasi.stock.enums.StatutProduit;
import sn.sunufarmasi.stock.mapper.ProduitMapper;
import sn.sunufarmasi.stock.repository.ProduitRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour la gestion du catalogue de produits
 *
 * @author WeCan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProduitService {

    private final sn.sunufarmasi.stock.repository.ProduitRepository produitRepository;
    private final ProduitMapper produitMapper;

    // ═══════════════════════════════════════════════════════════
    // CRÉATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Créer un nouveau produit dans le catalogue
     */
    public ProduitResponse create(CreateProduitRequest request) {
        log.info("Création produit: {}", request.nom());

        // Vérifier unicité code barre
        if (request.codeBarre() != null && produitRepository.existsByCodeBarre(request.codeBarre())) {
            throw new ConflictException("Un produit avec ce code barre existe déjà");
        }

        // Générer le code
        String code = generateCode();

        // Créer l'entité
        Produit produit = Produit.builder()
                .code(code)
                .codeBarre(request.codeBarre())
                .codeCip(request.codeCip())
                .numeroAmm(request.numeroAmm())
                .nom(request.nom())
                .dci(request.dci())
                .description(request.description())
                .categorie(request.categorie())
                .forme(request.forme())
                .dosage(request.dosage())
                .uniteVente(request.uniteVente())
                .contenance(request.contenance())
                .laboratoire(request.laboratoire())
                .paysOrigine(request.paysOrigine())
                .prixPublicTTC(request.prixPublicTTC())
                .prixAchatHT(request.prixAchatHT())
                .tauxTVA(request.tauxTVA())
                .tauxRemboursement(request.tauxRemboursement())
                .surOrdonnance(request.surOrdonnance())
                .listeMedicament(request.listeMedicament())
                .estGenerique(request.estGenerique())
                .produitReferenceId(request.produitReferenceId())
                .estRemboursable(request.estRemboursable())
                .temperatureConservation(request.temperatureConservation())
                .chaineFroid(request.chaineFroid())
                .dureeConservationMois(request.dureeConservationMois())
                .imageUrl(request.imageUrl())
                .noticeUrl(request.noticeUrl())
                .statut(StatutProduit.ACTIF)
                .build();

        Produit saved = produitRepository.save(produit);

        log.info("Produit créé: {} - {}", saved.getCode(), saved.getNom());

        return produitMapper.toProduitResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    // LECTURE
    // ═══════════════════════════════════════════════════════════

    /**
     * Récupérer un produit par ID
     */
    @Transactional(readOnly = true)
    public ProduitResponse getById(UUID id) {
        Produit produit = findByIdOrThrow(id);
        return produitMapper.toProduitResponse(produit);
    }

    /**
     * Récupérer un produit par code
     */
    @Transactional(readOnly = true)
    public ProduitResponse getByCode(String code) {
        Produit produit = produitRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec le code: " + code));
        return produitMapper.toProduitResponse(produit);
    }

    /**
     * Récupérer un produit par code barre
     */
    @Transactional(readOnly = true)
    public ProduitResponse getByCodeBarre(String codeBarre) {
        Produit produit = produitRepository.findByCodeBarre(codeBarre)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec le code barre: " + codeBarre));
        return produitMapper.toProduitResponse(produit);
    }

    /**
     * Rechercher des produits
     */
    @Transactional(readOnly = true)
    public List<ProduitResponse> search(String search) {
        return produitRepository.searchByNomOrDciOrCode(search).stream()
                .map(produitMapper::toProduitResponse)
                .collect(Collectors.toList());
    }

    /**
     * Rechercher des produits actifs avec pagination
     */
    @Transactional(readOnly = true)
    public Page<ProduitResponse> searchActifs(String search, Pageable pageable) {
        return produitRepository.searchActifs(search, pageable)
                .map(produitMapper::toProduitResponse);
    }

    /**
     * Récupérer par catégorie
     */
    @Transactional(readOnly = true)
    public List<ProduitResponse> getByCategorie(CategorieProduit categorie) {
        return produitRepository.findByCategorieAndStatut(categorie, StatutProduit.ACTIF).stream()
                .map(produitMapper::toProduitResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer par forme
     */
    @Transactional(readOnly = true)
    public List<ProduitResponse> getByForme(FormeProduit forme) {
        return produitRepository.findByFormeAndStatut(forme, StatutProduit.ACTIF).stream()
                .map(produitMapper::toProduitResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer par laboratoire
     */
    @Transactional(readOnly = true)
    public List<ProduitResponse> getByLaboratoire(String laboratoire) {
        return produitRepository.findByLaboratoireContainingIgnoreCase(laboratoire).stream()
                .map(produitMapper::toProduitResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer tous les laboratoires
     */
    @Transactional(readOnly = true)
    public List<String> getAllLaboratoires() {
        return produitRepository.findAllLaboratoires();
    }

    /**
     * Récupérer les génériques d'un princeps
     */
    @Transactional(readOnly = true)
    public List<ProduitResponse> getGeneriques(UUID princepsId) {
        return produitRepository.findGeneriquesByPrinceps(princepsId).stream()
                .map(produitMapper::toProduitResponse)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    // MODIFICATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Modifier un produit
     */
    public ProduitResponse update(UUID id, UpdateProduitRequest request) {
        log.info("Modification produit: {}", id);

        Produit produit = findByIdOrThrow(id);

        // Vérifier unicité code barre si modifié
        if (request.codeBarre() != null && !request.codeBarre().equals(produit.getCodeBarre())) {
            if (produitRepository.existsByCodeBarre(request.codeBarre())) {
                throw new ConflictException("Un produit avec ce code barre existe déjà");
            }
            produit.setCodeBarre(request.codeBarre());
        }

        // Mettre à jour les champs non null
        if (request.nom() != null) produit.setNom(request.nom());
        if (request.dci() != null) produit.setDci(request.dci());
        if (request.description() != null) produit.setDescription(request.description());
        if (request.categorie() != null) produit.setCategorie(request.categorie());
        if (request.forme() != null) produit.setForme(request.forme());
        if (request.dosage() != null) produit.setDosage(request.dosage());
        if (request.uniteVente() != null) produit.setUniteVente(request.uniteVente());
        if (request.contenance() != null) produit.setContenance(request.contenance());
        if (request.laboratoire() != null) produit.setLaboratoire(request.laboratoire());
        if (request.prixPublicTTC() != null) produit.setPrixPublicTTC(request.prixPublicTTC());
        if (request.prixAchatHT() != null) produit.setPrixAchatHT(request.prixAchatHT());
        if (request.tauxTVA() != null) produit.setTauxTVA(request.tauxTVA());
        if (request.tauxRemboursement() != null) produit.setTauxRemboursement(request.tauxRemboursement());
        if (request.surOrdonnance() != null) produit.setSurOrdonnance(request.surOrdonnance());
        if (request.listeMedicament() != null) produit.setListeMedicament(request.listeMedicament());
        if (request.estRemboursable() != null) produit.setEstRemboursable(request.estRemboursable());
        if (request.temperatureConservation() != null) produit.setTemperatureConservation(request.temperatureConservation());
        if (request.chaineFroid() != null) produit.setChaineFroid(request.chaineFroid());
        if (request.dureeConservationMois() != null) produit.setDureeConservationMois(request.dureeConservationMois());
        if (request.imageUrl() != null) produit.setImageUrl(request.imageUrl());
        if (request.noticeUrl() != null) produit.setNoticeUrl(request.noticeUrl());
        if (request.statut() != null) produit.setStatut(request.statut());

        Produit saved = produitRepository.save(produit);

        log.info("Produit modifié: {}", saved.getCode());

        return produitMapper.toProduitResponse(saved);
    }

    /**
     * Désactiver un produit
     */
    public void desactiver(UUID id) {
        log.info("Désactivation produit: {}", id);

        Produit produit = findByIdOrThrow(id);
        produit.setStatut(StatutProduit.INACTIF);
        produitRepository.save(produit);

        log.info("Produit désactivé: {}", produit.getCode());
    }

    /**
     * Réactiver un produit
     */
    public void reactiver(UUID id) {
        log.info("Réactivation produit: {}", id);

        Produit produit = findByIdOrThrow(id);
        produit.setStatut(StatutProduit.ACTIF);
        produitRepository.save(produit);

        log.info("Produit réactivé: {}", produit.getCode());
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES
    // ═══════════════════════════════════════════════════════════

    private Produit findByIdOrThrow(UUID id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé: " + id));
    }

    private String generateCode() {
        long count = produitRepository.count() + 1;
        return String.format("PRD-%06d", count);
    }
}
