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
import sn.sunufarmasi.stock.dto.request.*;
import sn.sunufarmasi.stock.dto.response.AlerteStockResponse;
import sn.sunufarmasi.stock.dto.response.MouvementStockResponse;
import sn.sunufarmasi.stock.dto.response.StockResponse;
import sn.sunufarmasi.stock.enums.TypeMouvement;
import sn.sunufarmasi.stock.service.StockService;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST pour la gestion du stock d'une pharmacie
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/pharmacies/{pharmacieId}/stock")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StockController {

    private final StockService stockService;

    // ═══════════════════════════════════════════════════════════
    // AJOUT PRODUIT AU STOCK
    // ═══════════════════════════════════════════════════════════

    /**
     * Ajouter un produit au stock de la pharmacie
     *
     * POST /api/v1/pharmacies/{pharmacieId}/stock
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'VENDEUR')")
    public ResponseEntity<ApiResponse<StockResponse>> ajouterProduit(
            @PathVariable UUID pharmacieId,
            @Valid @RequestBody AjouterStockRequest request,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Name") String userName
    ) {
        log.info("API - Ajout produit au stock: pharmacie={}, produit={}", pharmacieId, request.produitId());

        StockResponse response = stockService.ajouterProduit(pharmacieId, request, userId, userName);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Produit ajouté au stock", response));
    }

    // ═══════════════════════════════════════════════════════════
    // ENTRÉES DE STOCK
    // ═══════════════════════════════════════════════════════════

    /**
     * Enregistrer une entrée de stock
     *
     * POST /api/v1/pharmacies/{pharmacieId}/stock/entree
     */
    @PostMapping("/entree")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'VENDEUR')")
    public ResponseEntity<ApiResponse<MouvementStockResponse>> entreeStock(
            @PathVariable UUID pharmacieId,
            @Valid @RequestBody EntreeStockRequest request,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Name") String userName,
            @RequestHeader(value = "X-User-Role", defaultValue = "VENDEUR") String userRole
    ) {
        log.info("API - Entrée stock: pharmacie={}, produit={}, qté={}, type={}",
                pharmacieId, request.produitId(), request.quantite(), request.type());

        MouvementStockResponse response = stockService.entreeStock(pharmacieId, request, userId, userName, userRole);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Entrée de stock enregistrée", response));
    }

    // ═══════════════════════════════════════════════════════════
    // SORTIES DE STOCK
    // ═══════════════════════════════════════════════════════════

    /**
     * Enregistrer une sortie de stock
     *
     * POST /api/v1/pharmacies/{pharmacieId}/stock/sortie
     */
    @PostMapping("/sortie")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'VENDEUR')")
    public ResponseEntity<ApiResponse<MouvementStockResponse>> sortieStock(
            @PathVariable UUID pharmacieId,
            @Valid @RequestBody SortieStockRequest request,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Name") String userName,
            @RequestHeader(value = "X-User-Role", defaultValue = "VENDEUR") String userRole
    ) {
        log.info("API - Sortie stock: pharmacie={}, produit={}, qté={}, type={}",
                pharmacieId, request.produitId(), request.quantite(), request.type());

        MouvementStockResponse response = stockService.sortieStock(pharmacieId, request, userId, userName, userRole);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sortie de stock enregistrée", response));
    }

    // ═══════════════════════════════════════════════════════════
    // AJUSTEMENT INVENTAIRE
    // ═══════════════════════════════════════════════════════════

    /**
     * Ajuster le stock (inventaire)
     *
     * POST /api/v1/pharmacies/{pharmacieId}/stock/ajustement
     */
    @PostMapping("/ajustement")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<ApiResponse<MouvementStockResponse>> ajusterStock(
            @PathVariable UUID pharmacieId,
            @Valid @RequestBody AjustementStockRequest request,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Name") String userName
    ) {
        log.info("API - Ajustement stock: pharmacie={}, produit={}, nouvelle qté={}",
                pharmacieId, request.produitId(), request.nouvelleQuantite());

        MouvementStockResponse response = stockService.ajusterStock(pharmacieId, request, userId, userName);

        return ResponseEntity.ok(ApiResponse.success("Stock ajusté", response));
    }

    // ═══════════════════════════════════════════════════════════
    // CONSULTATION STOCK
    // ═══════════════════════════════════════════════════════════

    /**
     * Récupérer tout le stock de la pharmacie
     *
     * GET /api/v1/pharmacies/{pharmacieId}/stock
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'VENDEUR')")
    public ResponseEntity<List<StockResponse>> getStock(@PathVariable UUID pharmacieId) {
        log.info("API - Récupération stock pharmacie: {}", pharmacieId);
        return ResponseEntity.ok(stockService.getStock(pharmacieId));
    }

    /**
     * Récupérer le stock avec pagination
     *
     * GET /api/v1/pharmacies/{pharmacieId}/stock/page
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'VENDEUR')")
    public ResponseEntity<Page<StockResponse>> getStockPaginated(
            @PathVariable UUID pharmacieId,
            Pageable pageable
    ) {
        log.info("API - Récupération stock paginé pharmacie: {}", pharmacieId);
        return ResponseEntity.ok(stockService.getStock(pharmacieId, pageable));
    }

    /**
     * Rechercher dans le stock
     *
     * GET /api/v1/pharmacies/{pharmacieId}/stock/search?q=paracetamol
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'VENDEUR')")
    public ResponseEntity<List<StockResponse>> searchStock(
            @PathVariable UUID pharmacieId,
            @RequestParam String q
    ) {
        log.info("API - Recherche stock: pharmacie={}, q={}", pharmacieId, q);
        return ResponseEntity.ok(stockService.searchStock(pharmacieId, q));
    }

    /**
     * Récupérer un produit par code barre
     *
     * GET /api/v1/pharmacies/{pharmacieId}/stock/codebarre/{codeBarre}
     */
    @GetMapping("/codebarre/{codeBarre}")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'VENDEUR')")
    public ResponseEntity<StockResponse> getByCodeBarre(
            @PathVariable UUID pharmacieId,
            @PathVariable String codeBarre
    ) {
        log.info("API - Récupération produit par code barre: {}", codeBarre);
        return ResponseEntity.ok(stockService.getByCodeBarre(pharmacieId, codeBarre));
    }

    // ═══════════════════════════════════════════════════════════
    // ALERTES
    // ═══════════════════════════════════════════════════════════

    /**
     * Récupérer les alertes de stock
     *
     * GET /api/v1/pharmacies/{pharmacieId}/stock/alertes
     */
    @GetMapping("/alertes")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'VENDEUR')")
    public ResponseEntity<AlerteStockResponse> getAlertes(@PathVariable UUID pharmacieId) {
        log.info("API - Récupération alertes stock: {}", pharmacieId);
        return ResponseEntity.ok(stockService.getAlertes(pharmacieId));
    }

    // ═══════════════════════════════════════════════════════════
    // HISTORIQUE MOUVEMENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Récupérer l'historique des mouvements
     *
     * GET /api/v1/pharmacies/{pharmacieId}/stock/historique
     */
    @GetMapping("/historique")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'VENDEUR')")
    public ResponseEntity<Page<MouvementStockResponse>> getHistorique(
            @PathVariable UUID pharmacieId,
            Pageable pageable
    ) {
        log.info("API - Récupération historique stock: {}", pharmacieId);
        return ResponseEntity.ok(stockService.getHistorique(pharmacieId, pageable));
    }

    /**
     * Récupérer l'historique d'un produit
     *
     * GET /api/v1/pharmacies/{pharmacieId}/stock/produits/{produitId}/historique
     */
    @GetMapping("/produits/{produitId}/historique")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'VENDEUR')")
    public ResponseEntity<List<MouvementStockResponse>> getHistoriqueProduit(
            @PathVariable UUID pharmacieId,
            @PathVariable UUID produitId
    ) {
        log.info("API - Récupération historique produit: pharmacie={}, produit={}", pharmacieId, produitId);
        return ResponseEntity.ok(stockService.getHistoriqueProduit(pharmacieId, produitId));
    }

    // ═══════════════════════════════════════════════════════════
    // MODIFICATION PARAMÈTRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Modifier les paramètres d'un produit en stock
     *
     * PUT /api/v1/pharmacies/{pharmacieId}/stock/produits/{produitId}
     */
    @PutMapping("/produits/{produitId}")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<StockResponse> updateParams(
            @PathVariable UUID pharmacieId,
            @PathVariable UUID produitId,
            @Valid @RequestBody UpdateStockParamsRequest request
    ) {
        log.info("API - Modification paramètres stock: pharmacie={}, produit={}", pharmacieId, produitId);
        return ResponseEntity.ok(stockService.updateParams(pharmacieId, produitId, request));
    }

    // ═══════════════════════════════════════════════════════════
    // ENUMS
    // ═══════════════════════════════════════════════════════════

    /**
     * Récupérer les types de mouvements d'entrée
     *
     * GET /api/v1/pharmacies/{pharmacieId}/stock/enums/entrees
     */
    @GetMapping("/enums/entrees")
    public ResponseEntity<List<TypeMouvement>> getTypesEntree() {
        return ResponseEntity.ok(
                java.util.Arrays.stream(TypeMouvement.values())
                        .filter(TypeMouvement::isEntree)
                        .toList()
        );
    }

    /**
     * Récupérer les types de mouvements de sortie
     *
     * GET /api/v1/pharmacies/{pharmacieId}/stock/enums/sorties
     */
    @GetMapping("/enums/sorties")
    public ResponseEntity<List<TypeMouvement>> getTypesSortie() {
        return ResponseEntity.ok(
                java.util.Arrays.stream(TypeMouvement.values())
                        .filter(TypeMouvement::isSortie)
                        .toList()
        );
    }
}
