package sn.sunufarmasi.pharmacie.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.pharmacie.dto.request.AdminCreatePharmacieRequest;
import sn.sunufarmasi.pharmacie.dto.request.CreatePharmacieRequest;
import sn.sunufarmasi.pharmacie.dto.request.UpdatePharmacieRequest;
import sn.sunufarmasi.pharmacie.dto.response.PharmacieDetailResponse;
import sn.sunufarmasi.pharmacie.dto.response.PharmacieResponse;
import sn.sunufarmasi.pharmacie.enums.StatutPharmacie;
import sn.sunufarmasi.pharmacie.service.PharmacieService;
import sn.sunufarmasi.shared.dto.ApiResponse;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST pour la gestion des pharmacies
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/pharmacies")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PharmacieController {

    private final PharmacieService pharmacieService;

    /**
     * Récupérer toutes les pharmacies (Admin)
     *
     * GET /api/v1/pharmacies
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<PharmacieResponse>> getAll() {
        log.info("API - Récupération de toutes les pharmacies");
        return ResponseEntity.ok(pharmacieService.getAll());
    }

    /**
     * Créer une pharmacie (Super Admin) — sans X-Pharmacien-Id, avec syndicat
     *
     * POST /api/v1/pharmacies/admin
     */
    @PostMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PharmacieResponse> adminCreate(
            @Valid @RequestBody AdminCreatePharmacieRequest request
    ) {
        log.info("API Admin - Création pharmacie: {}", request.nom());
        return ResponseEntity.status(HttpStatus.CREATED).body(pharmacieService.adminCreate(request));
    }

    /**
     * Assigner une pharmacie à un syndicat (Admin)
     *
     * PUT /api/v1/pharmacies/{id}/syndicat/{syndicatId}
     */
    @PutMapping("/{id}/syndicat/{syndicatId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PharmacieResponse> adminAssignerSyndicat(
            @PathVariable UUID id,
            @PathVariable UUID syndicatId
    ) {
        log.info("API Admin - Assignation pharmacie {} → syndicat {}", id, syndicatId);
        return ResponseEntity.ok(pharmacieService.adminAssignerSyndicat(id, syndicatId));
    }

    /**
     * Activer une pharmacie (Super Admin uniquement)
     *
     * POST /api/v1/pharmacies/{id}/activer
     */
    @PostMapping("/{id}/activer")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PharmacieResponse> activer(@PathVariable UUID id) {
        log.info("API - Activation pharmacie: {}", id);
        return ResponseEntity.ok(pharmacieService.activer(id));
    }

    /**
     * Suspendre une pharmacie pour non-paiement (Super Admin uniquement)
     *
     * POST /api/v1/pharmacies/{id}/suspendre
     */
    @PostMapping("/{id}/suspendre")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PharmacieResponse> suspendre(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "Non-paiement") String motif
    ) {
        log.info("API - Suspension pharmacie: {} - Motif: {}", id, motif);
        return ResponseEntity.ok(pharmacieService.suspendreForNonPaiement(id, motif));
    }

    /**
     * Créer une nouvelle pharmacie
     *
     * POST /api/v1/pharmacies
     * Header: Authorization: Bearer {token}
     */
    @PostMapping
    public ResponseEntity<PharmacieResponse> create(
            @Valid @RequestBody CreatePharmacieRequest request,
            @RequestHeader("X-Pharmacien-Id") UUID pharmacienId // TODO: Extraire du JWT
    ) {
        log.info("API - Création pharmacie par pharmacien: {}", pharmacienId);

        PharmacieResponse response = pharmacieService.create(request, pharmacienId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Modifier une pharmacie (Admin — sans X-Pharmacien-Id)
     *
     * PUT /api/v1/pharmacies/{id}/admin
     */
    @PutMapping("/{id}/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PharmacieResponse> adminUpdate(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePharmacieRequest request
    ) {
        log.info("API Admin - Modification pharmacie: {}", id);
        return ResponseEntity.ok(pharmacieService.adminUpdate(id, request));
    }

    /**
     * Modifier une pharmacie
     *
     * PUT /api/v1/pharmacies/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<PharmacieResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePharmacieRequest request,
            @RequestHeader("X-Pharmacien-Id") UUID pharmacienId // TODO: Extraire du JWT
    ) {
        log.info("API - Modification pharmacie: {} par pharmacien: {}", id, pharmacienId);

        PharmacieResponse response = pharmacieService.update(id, request, pharmacienId);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer une pharmacie par ID (détails complets)
     *
     * GET /api/v1/pharmacies/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PharmacieDetailResponse> getById(@PathVariable UUID id) {
        log.info("API - Récupération pharmacie: {}", id);

        PharmacieDetailResponse response = pharmacieService.getById(id);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer une pharmacie par code
     *
     * GET /api/v1/pharmacies/code/{code}
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<PharmacieDetailResponse> getByCode(@PathVariable String code) {
        log.info("API - Récupération pharmacie par code: {}", code);

        PharmacieDetailResponse response = pharmacieService.getByCode(code);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer toutes les pharmacies du pharmacien connecté
     *
     * GET /api/v1/pharmacies/me
     */
    @GetMapping("/me")
    public ResponseEntity<List<PharmacieResponse>> getMyPharmacies(
            @RequestHeader("X-Pharmacien-Id") UUID pharmacienId // TODO: Extraire du JWT
    ) {
        log.info("API - Récupération pharmacies du pharmacien: {}", pharmacienId);

        List<PharmacieResponse> responses = pharmacieService.getByPharmacien(pharmacienId);

        return ResponseEntity.ok(responses);
    }

    /**
     * Récupérer les pharmacies d'une commune
     *
     * GET /api/v1/pharmacies/commune/{communeId}
     */
    @GetMapping("/commune/{communeId}")
    public ResponseEntity<List<PharmacieResponse>> getByCommune(@PathVariable UUID communeId) {
        log.info("API - Récupération pharmacies de la commune: {}", communeId);

        List<PharmacieResponse> responses = pharmacieService.getByCommune(communeId);

        return ResponseEntity.ok(responses);
    }

    /**
     * Récupérer les pharmacies d'un syndicat
     *
     * GET /api/v1/pharmacies/syndicat/{syndicatId}
     */
    @GetMapping("/syndicat/{syndicatId}")
    public ResponseEntity<List<PharmacieResponse>> getBySyndicat(@PathVariable UUID syndicatId) {
        log.info("API - Récupération pharmacies du syndicat: {}", syndicatId);

        List<PharmacieResponse> responses = pharmacieService.getBySyndicat(syndicatId);

        return ResponseEntity.ok(responses);
    }

    /**
     * Rechercher des pharmacies proches d'un point GPS
     *
     * GET /api/v1/pharmacies/nearby?latitude=14.6928&longitude=-17.4467&radius=5
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<PharmacieResponse>> findNearby(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5") double radius
    ) {
        log.info("API - Recherche pharmacies proches de ({}, {}) - Rayon: {} km",
                latitude, longitude, radius);

        List<PharmacieResponse> responses = pharmacieService.findNearby(latitude, longitude, radius);

        return ResponseEntity.ok(responses);
    }

    /**
     * Rechercher des pharmacies par nom
     *
     * GET /api/v1/pharmacies/search?nom=plateau
     */
    @GetMapping("/search")
    public ResponseEntity<List<PharmacieResponse>> searchByNom(@RequestParam String nom) {
        log.info("API - Recherche pharmacies par nom: {}", nom);

        List<PharmacieResponse> responses = pharmacieService.searchByNom(nom);

        return ResponseEntity.ok(responses);
    }

    /**
     * Récupérer les pharmacies par statut
     *
     * GET /api/v1/pharmacies/statut/{statut}
     */
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<PharmacieResponse>> getByStatut(@PathVariable StatutPharmacie statut) {
        log.info("API - Récupération pharmacies avec statut: {}", statut);

        List<PharmacieResponse> responses = pharmacieService.getByStatut(statut);

        return ResponseEntity.ok(responses);
    }

    /**
     * Supprimer une pharmacie
     *
     * DELETE /api/v1/pharmacies/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @RequestHeader("X-Pharmacien-Id") UUID pharmacienId
    ) {
        log.info("API - Suppression pharmacie: {} par pharmacien: {}", id, pharmacienId);
        pharmacieService.delete(id, pharmacienId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Supprimer une pharmacie (Admin — sans vérification propriétaire)
     * DELETE /api/v1/pharmacies/{id}/admin
     */
    @DeleteMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adminDelete(@PathVariable UUID id) {
        log.info("API - Suppression admin pharmacie: {}", id);
        pharmacieService.adminDelete(id);
        return ResponseEntity.ok(ApiResponse.<Void>successMessage("Pharmacie supprimée avec succès"));
    }

    /**
     * Valider une pharmacie (syndicat/admin)
     *
     * POST /api/v1/pharmacies/{id}/valider
     */
    @PostMapping("/{id}/valider")
    public ResponseEntity<PharmacieResponse> valider(
            @PathVariable UUID id,
            @RequestParam String validePar
    ) {
        log.info("API - Validation pharmacie: {} par: {}", id, validePar);

        PharmacieResponse response = pharmacieService.valider(id, validePar);

        return ResponseEntity.ok(response);
    }

    /**
     * Rejeter une pharmacie
     *
     * POST /api/v1/pharmacies/{id}/rejeter
     */
    @PostMapping("/{id}/rejeter")
    public ResponseEntity<PharmacieResponse> rejeter(
            @PathVariable UUID id,
            @RequestParam String motif
    ) {
        log.info("API - Rejet pharmacie: {} - Motif: {}", id, motif);

        PharmacieResponse response = pharmacieService.rejeter(id, motif);

        return ResponseEntity.ok(response);
    }

    /**
     * Renouveler l'abonnement d'une pharmacie (Admin)
     *
     * POST /api/v1/pharmacies/{id}/renouveler-abonnement?mois=12
     */
    @PostMapping("/{id}/renouveler-abonnement")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PharmacieResponse> renouvelerAbonnement(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "12") int mois,
            @RequestParam(required = false) String reference
    ) {
        log.info("API - Renouvellement abonnement pharmacie: {} pour {} mois", id, mois);
        return ResponseEntity.ok(pharmacieService.renouvelerAbonnement(id, mois, reference));
    }

    /**
     * Envoyer un e-mail de rappel de renouvellement (Admin)
     *
     * POST /api/v1/pharmacies/{id}/relancer-rappel
     */
    @PostMapping("/{id}/relancer-rappel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> relancerRappel(@PathVariable UUID id) {
        log.info("API - Envoi rappel renouvellement pharmacie: {}", id);
        pharmacieService.relancerRappel(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Enregistrer un paiement pour une pharmacie (Admin)
     * Active la pharmacie + met à jour dates abonnement
     *
     * POST /api/v1/pharmacies/{id}/enregistrer-paiement
     */
    @PostMapping("/{id}/enregistrer-paiement")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PharmacieResponse> enregistrerPaiement(
            @PathVariable UUID id,
            @RequestParam(required = false) String reference
    ) {
        log.info("API - Enregistrement paiement pharmacie: {}", id);
        return ResponseEntity.ok(pharmacieService.enregistrerPaiement(id, reference));
    }
}