package sn.sunufarmasi.pharmacie.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.pharmacie.dto.request.CreatePharmacieRequest;
import sn.sunufarmasi.pharmacie.dto.request.UpdatePharmacieRequest;
import sn.sunufarmasi.pharmacie.dto.response.PharmacieDetailResponse;
import sn.sunufarmasi.pharmacie.dto.response.PharmacieResponse;
import sn.sunufarmasi.pharmacie.enums.StatutPharmacie;
import sn.sunufarmasi.pharmacie.service.PharmacieService;

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
            @RequestHeader("X-Pharmacien-Id") UUID pharmacienId // TODO: Extraire du JWT
    ) {
        log.info("API - Suppression pharmacie: {} par pharmacien: {}", id, pharmacienId);

        pharmacieService.delete(id, pharmacienId);

        return ResponseEntity.noContent().build();
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
}