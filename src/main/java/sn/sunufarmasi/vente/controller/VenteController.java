package sn.sunufarmasi.vente.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.vente.dto.request.*;
import sn.sunufarmasi.vente.dto.response.*;
import sn.sunufarmasi.vente.enums.ModePaiement;
import sn.sunufarmasi.vente.enums.StatutVente;
import sn.sunufarmasi.vente.enums.TypeVente;
import sn.sunufarmasi.vente.service.VenteService;
import sn.sunufarmasi.shared.dto.ApiResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Controller REST pour la gestion des ventes
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/pharmacies/{pharmacieId}/ventes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class VenteController {

    private final VenteService venteService;

    // ═══════════════════════════════════════════════════════════
    // CRÉATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Créer une nouvelle vente
     *
     * POST /api/v1/pharmacies/{pharmacieId}/ventes
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'VENDEUR')")
    public ResponseEntity<ApiResponse<VenteDetailResponse>> create(
            @PathVariable UUID pharmacieId,
            @Valid @RequestBody CreateVenteRequest request,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Name") String userName
    ) {
        log.info("API - Création vente: pharmacie={}", pharmacieId);

        VenteDetailResponse response = venteService.create(pharmacieId, request, userId, userName);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vente créée avec succès", response));
    }

    // ═══════════════════════════════════════════════════════════
    // PAIEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Enregistrer un paiement
     *
     * POST /api/v1/pharmacies/{pharmacieId}/ventes/{venteId}/paiement
     */
    @PostMapping("/{venteId}/paiement")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'VENDEUR')")
    public ResponseEntity<ApiResponse<VenteDetailResponse>> enregistrerPaiement(
            @PathVariable UUID pharmacieId,
            @PathVariable UUID venteId,
            @Valid @RequestBody PaiementRequest request,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Name") String userName
    ) {
        log.info("API - Paiement vente {}: {} FCFA", venteId, request.montant());

        VenteDetailResponse response = venteService.enregistrerPaiement(venteId, request, userId, userName);

        return ResponseEntity.ok(ApiResponse.success("Paiement enregistré", response));
    }

    // ═══════════════════════════════════════════════════════════
    // ANNULATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Annuler une vente
     *
     * DELETE /api/v1/pharmacies/{pharmacieId}/ventes/{venteId}
     */
    @DeleteMapping("/{venteId}")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<ApiResponse<VenteDetailResponse>> annuler(
            @PathVariable UUID pharmacieId,
            @PathVariable UUID venteId,
            @RequestParam(required = false) String motif,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Name") String userName
    ) {
        log.info("API - Annulation vente {}", venteId);

        VenteDetailResponse response = venteService.annuler(venteId, motif, userId, userName);

        return ResponseEntity.ok(ApiResponse.success("Vente annulée", response));
    }

    // ═══════════════════════════════════════════════════════════
    // CONSULTATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Récupérer une vente par ID
     *
     * GET /api/v1/pharmacies/{pharmacieId}/ventes/{venteId}
     */
    @GetMapping("/{venteId}")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'VENDEUR')")
    public ResponseEntity<VenteDetailResponse> getById(
            @PathVariable UUID pharmacieId,
            @PathVariable UUID venteId
    ) {
        log.info("API - Récupération vente {}", venteId);
        return ResponseEntity.ok(venteService.getById(venteId));
    }

    /**
     * Récupérer une vente par numéro
     *
     * GET /api/v1/pharmacies/{pharmacieId}/ventes/numero/{numero}
     */
    @GetMapping("/numero/{numero}")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'VENDEUR')")
    public ResponseEntity<VenteDetailResponse> getByNumero(
            @PathVariable UUID pharmacieId,
            @PathVariable String numero
    ) {
        log.info("API - Récupération vente par numéro {}", numero);
        return ResponseEntity.ok(venteService.getByNumero(numero));
    }

    /**
     * Lister les ventes de la pharmacie
     *
     * GET /api/v1/pharmacies/{pharmacieId}/ventes
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'VENDEUR')")
    public ResponseEntity<Page<VenteResponse>> getVentes(
            @PathVariable UUID pharmacieId,
            Pageable pageable
    ) {
        log.info("API - Liste ventes pharmacie {}", pharmacieId);
        return ResponseEntity.ok(venteService.getByPharmacie(pharmacieId, pageable));
    }

    /**
     * Ventes par période
     *
     * GET /api/v1/pharmacies/{pharmacieId}/ventes/periode?debut=2024-01-01&fin=2024-01-31
     */
    @GetMapping("/periode")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'VENDEUR')")
    public ResponseEntity<List<VenteResponse>> getByPeriode(
            @PathVariable UUID pharmacieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        log.info("API - Ventes période {} - {}", debut, fin);
        return ResponseEntity.ok(venteService.getByPharmacieAndPeriode(pharmacieId, debut, fin));
    }

    /**
     * Ventes du jour
     *
     * GET /api/v1/pharmacies/{pharmacieId}/ventes/jour
     */
    @GetMapping("/jour")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'VENDEUR')")
    public ResponseEntity<List<VenteResponse>> getVentesDuJour(@PathVariable UUID pharmacieId) {
        log.info("API - Ventes du jour pharmacie {}", pharmacieId);
        return ResponseEntity.ok(venteService.getVentesDuJour(pharmacieId));
    }

    // ═══════════════════════════════════════════════════════════
    // TICKET DE CAISSE
    // ═══════════════════════════════════════════════════════════

    /**
     * Récupérer le ticket de caisse
     *
     * GET /api/v1/pharmacies/{pharmacieId}/ventes/{venteId}/ticket
     */
    @GetMapping("/{venteId}/ticket")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'VENDEUR')")
    public ResponseEntity<TicketCaisseResponse> getTicket(
            @PathVariable UUID pharmacieId,
            @PathVariable UUID venteId
    ) {
        log.info("API - Ticket de caisse vente {}", venteId);
        return ResponseEntity.ok(venteService.getTicket(venteId));
    }

    /**
     * Récupérer le ticket formaté pour impression
     *
     * GET /api/v1/pharmacies/{pharmacieId}/ventes/{venteId}/ticket/print
     */
    @GetMapping("/{venteId}/ticket/print")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'VENDEUR')")
    public ResponseEntity<String> getTicketPrint(
            @PathVariable UUID pharmacieId,
            @PathVariable UUID venteId,
            @RequestParam(defaultValue = "40") int largeur
    ) {
        log.info("API - Impression ticket vente {}", venteId);
        TicketCaisseResponse ticket = venteService.getTicket(venteId);
        return ResponseEntity.ok(ticket.toTextFormat(largeur));
    }

    // ═══════════════════════════════════════════════════════════
    // STATISTIQUES
    // ═══════════════════════════════════════════════════════════

    /**
     * Statistiques de ventes
     *
     * GET /api/v1/pharmacies/{pharmacieId}/ventes/statistiques?debut=2024-01-01&fin=2024-01-31
     */
    @GetMapping("/statistiques")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<StatistiquesVenteResponse> getStatistiques(
            @PathVariable UUID pharmacieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        log.info("API - Statistiques ventes {} - {}", debut, fin);
        return ResponseEntity.ok(venteService.getStatistiques(pharmacieId, debut, fin));
    }

    // ═══════════════════════════════════════════════════════════
    // ENUMS
    // ═══════════════════════════════════════════════════════════

    /**
     * Types de vente
     *
     * GET /api/v1/pharmacies/{pharmacieId}/ventes/enums/types
     */
    @GetMapping("/enums/types")
    public ResponseEntity<TypeVente[]> getTypesVente() {
        return ResponseEntity.ok(TypeVente.values());
    }

    /**
     * Modes de paiement
     *
     * GET /api/v1/pharmacies/{pharmacieId}/ventes/enums/paiements
     */
    @GetMapping("/enums/paiements")
    public ResponseEntity<ModePaiement[]> getModesPaiement() {
        return ResponseEntity.ok(ModePaiement.values());
    }

    /**
     * Statuts de vente
     *
     * GET /api/v1/pharmacies/{pharmacieId}/ventes/enums/statuts
     */
    @GetMapping("/enums/statuts")
    public ResponseEntity<StatutVente[]> getStatuts() {
        return ResponseEntity.ok(StatutVente.values());
    }
}
