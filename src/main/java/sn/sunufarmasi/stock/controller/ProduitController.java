package sn.sunufarmasi.stock.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.shared.dto.ApiResponse;
import sn.sunufarmasi.stock.dto.request.CreateProduitRequest;
import sn.sunufarmasi.stock.dto.request.UpdateProduitRequest;
import sn.sunufarmasi.stock.dto.response.ProduitResponse;
import sn.sunufarmasi.stock.enums.CategorieProduit;
import sn.sunufarmasi.stock.enums.FormeProduit;
import sn.sunufarmasi.stock.enums.UniteVente;
import sn.sunufarmasi.stock.service.ProduitService;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST pour le catalogue de produits
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/produits")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProduitController {

    private final sn.sunufarmasi.stock.service.ProduitService produitService;

    // ═══════════════════════════════════════════════════════════
    // CRÉATION (Admin uniquement)
    // ═══════════════════════════════════════════════════════════

    /**
     * Créer un nouveau produit
     *
     * POST /api/v1/produits
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProduitResponse>> create(
            @Valid @RequestBody CreateProduitRequest request
    ) {
        log.info("API - Création produit: {}", request.nom());

        ProduitResponse response = produitService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Produit créé avec succès", response));
    }

    // ═══════════════════════════════════════════════════════════
    // LECTURE
    // ═══════════════════════════════════════════════════════════

    /**
     * Récupérer un produit par ID
     *
     * GET /api/v1/produits/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProduitResponse> getById(@PathVariable UUID id) {
        log.info("API - Récupération produit: {}", id);
        return ResponseEntity.ok(produitService.getById(id));
    }

    /**
     * Récupérer un produit par code
     *
     * GET /api/v1/produits/code/{code}
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ProduitResponse> getByCode(@PathVariable String code) {
        log.info("API - Récupération produit par code: {}", code);
        return ResponseEntity.ok(produitService.getByCode(code));
    }

    /**
     * Récupérer un produit par code barre
     *
     * GET /api/v1/produits/codebarre/{codeBarre}
     */
    @GetMapping("/codebarre/{codeBarre}")
    public ResponseEntity<ProduitResponse> getByCodeBarre(@PathVariable String codeBarre) {
        log.info("API - Récupération produit par code barre: {}", codeBarre);
        return ResponseEntity.ok(produitService.getByCodeBarre(codeBarre));
    }

    /**
     * Rechercher des produits
     *
     * GET /api/v1/produits/search?q=paracetamol
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProduitResponse>> search(@RequestParam String q) {
        log.info("API - Recherche produits: {}", q);
        return ResponseEntity.ok(produitService.search(q));
    }

    /**
     * Rechercher avec pagination
     *
     * GET /api/v1/produits/search/page?q=paracetamol
     */
    @GetMapping("/search/page")
    public ResponseEntity<Page<ProduitResponse>> searchPaginated(
            @RequestParam String q,
            Pageable pageable
    ) {
        log.info("API - Recherche produits paginée: {}", q);
        return ResponseEntity.ok(produitService.searchActifs(q, pageable));
    }

    /**
     * Récupérer par catégorie
     *
     * GET /api/v1/produits/categorie/{categorie}
     */
    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<List<ProduitResponse>> getByCategorie(@PathVariable CategorieProduit categorie) {
        log.info("API - Récupération produits par catégorie: {}", categorie);
        return ResponseEntity.ok(produitService.getByCategorie(categorie));
    }

    /**
     * Récupérer par forme galénique
     *
     * GET /api/v1/produits/forme/{forme}
     */
    @GetMapping("/forme/{forme}")
    public ResponseEntity<List<ProduitResponse>> getByForme(@PathVariable FormeProduit forme) {
        log.info("API - Récupération produits par forme: {}", forme);
        return ResponseEntity.ok(produitService.getByForme(forme));
    }

    /**
     * Récupérer par laboratoire
     *
     * GET /api/v1/produits/laboratoire?nom=sanofi
     */
    @GetMapping("/laboratoire")
    public ResponseEntity<List<ProduitResponse>> getByLaboratoire(@RequestParam String nom) {
        log.info("API - Récupération produits par laboratoire: {}", nom);
        return ResponseEntity.ok(produitService.getByLaboratoire(nom));
    }

    /**
     * Récupérer tous les laboratoires
     *
     * GET /api/v1/produits/laboratoires
     */
    @GetMapping("/laboratoires")
    public ResponseEntity<List<String>> getAllLaboratoires() {
        log.info("API - Récupération liste laboratoires");
        return ResponseEntity.ok(produitService.getAllLaboratoires());
    }

    /**
     * Récupérer les génériques d'un princeps
     *
     * GET /api/v1/produits/{id}/generiques
     */
    @GetMapping("/{id}/generiques")
    public ResponseEntity<List<ProduitResponse>> getGeneriques(@PathVariable UUID id) {
        log.info("API - Récupération génériques du produit: {}", id);
        return ResponseEntity.ok(produitService.getGeneriques(id));
    }

    /**
     * Récupérer toutes les catégories
     *
     * GET /api/v1/produits/enums/categories
     */
    @GetMapping("/enums/categories")
    public ResponseEntity<CategorieProduit[]> getCategories() {
        return ResponseEntity.ok(CategorieProduit.values());
    }

    /**
     * Récupérer toutes les formes galéniques
     *
     * GET /api/v1/produits/enums/formes
     */
    @GetMapping("/enums/formes")
    public ResponseEntity<FormeProduit[]> getFormes() {
        return ResponseEntity.ok(FormeProduit.values());
    }

    /**
     * Récupérer toutes les unités de vente
     *
     * GET /api/v1/produits/enums/unites
     */
    @GetMapping("/enums/unites")
    public ResponseEntity<UniteVente[]> getUnites() {
        return ResponseEntity.ok(UniteVente.values());
    }

    // ═══════════════════════════════════════════════════════════
    // MODIFICATION (Admin)
    // ═══════════════════════════════════════════════════════════

    /**
     * Modifier un produit
     *
     * PUT /api/v1/produits/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProduitResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProduitRequest request
    ) {
        log.info("API - Modification produit: {}", id);
        return ResponseEntity.ok(produitService.update(id, request));
    }

    /**
     * Désactiver un produit
     *
     * POST /api/v1/produits/{id}/desactiver
     */
    @PostMapping("/{id}/desactiver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> desactiver(@PathVariable UUID id) {
        log.info("API - Désactivation produit: {}", id);
        produitService.desactiver(id);
        return ResponseEntity.ok(ApiResponse.success("Produit désactivé", null));
    }

    /**
     * Réactiver un produit
     *
     * POST /api/v1/produits/{id}/reactiver
     */
    @PostMapping("/{id}/reactiver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> reactiver(@PathVariable UUID id) {
        log.info("API - Réactivation produit: {}", id);
        produitService.reactiver(id);
        return ResponseEntity.ok(ApiResponse.success("Produit réactivé", null));
    }
}
