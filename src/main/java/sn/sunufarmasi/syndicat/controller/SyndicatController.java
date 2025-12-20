package sn.sunufarmasi.syndicat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.shared.dto.ApiResponse;
import sn.sunufarmasi.syndicat.dto.request.ChangePasswordSyndicatRequest;
import sn.sunufarmasi.syndicat.dto.request.CreateSyndicatRequest;
import sn.sunufarmasi.syndicat.dto.request.LoginSyndicatRequest;
import sn.sunufarmasi.syndicat.dto.request.UpdateSyndicatRequest;
import sn.sunufarmasi.syndicat.dto.response.SyndicatAuthResponse;
import sn.sunufarmasi.syndicat.dto.response.SyndicatDetailResponse;
import sn.sunufarmasi.syndicat.dto.response.SyndicatResponse;
import sn.sunufarmasi.syndicat.enums.StatutSyndicat;
import sn.sunufarmasi.syndicat.enums.TypeSyndicat;
import sn.sunufarmasi.syndicat.service.SyndicatService;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST pour la gestion des syndicats
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/syndicats")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SyndicatController {

    private final SyndicatService syndicatService;

    // ═══════════════════════════════════════════════════════════
    // CRÉATION (Admin uniquement)
    // ═══════════════════════════════════════════════════════════

    /**
     * Créer un nouveau syndicat (Admin uniquement)
     *
     * POST /api/v1/syndicats
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SyndicatResponse>> create(
            @Valid @RequestBody CreateSyndicatRequest request,
            @RequestHeader("X-Admin-Id") UUID adminId,      // TODO: Extraire du JWT
            @RequestHeader("X-Admin-Nom") String adminNom   // TODO: Extraire du JWT
    ) {
        log.info("API - Création syndicat: {} par admin: {}", request.nom(), adminId);

        SyndicatResponse response = syndicatService.create(request, adminId, adminNom);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Syndicat créé avec succès", response));
    }

    // ═══════════════════════════════════════════════════════════
    // AUTHENTIFICATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Connexion syndicat (username + mot de passe)
     *
     * POST /api/v1/syndicats/login
     */
    @PostMapping("/login")
    public ResponseEntity<SyndicatAuthResponse> login(
            @Valid @RequestBody LoginSyndicatRequest request
    ) {
        log.info("API - Connexion syndicat: {}", request.username());

        SyndicatAuthResponse response = syndicatService.login(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Changer le mot de passe
     *
     * PUT /api/v1/syndicats/me/password
     */
    @PutMapping("/me/password")
    @PreAuthorize("hasRole('SYNDICAT')")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestHeader("X-Syndicat-Id") UUID syndicatId,  // TODO: Extraire du JWT
            @Valid @RequestBody ChangePasswordSyndicatRequest request
    ) {
        log.info("API - Changement mot de passe syndicat: {}", syndicatId);

        syndicatService.changePassword(syndicatId, request);

        return ResponseEntity.ok(ApiResponse.success("Mot de passe modifié avec succès", null));
    }

    // ═══════════════════════════════════════════════════════════
    // LECTURE
    // ═══════════════════════════════════════════════════════════

    /**
     * Récupérer le profil du syndicat connecté
     *
     * GET /api/v1/syndicats/me
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('SYNDICAT')")
    public ResponseEntity<SyndicatDetailResponse> getMyProfile(
            @RequestHeader("X-Syndicat-Id") UUID syndicatId  // TODO: Extraire du JWT
    ) {
        log.info("API - Récupération profil syndicat: {}", syndicatId);

        SyndicatDetailResponse response = syndicatService.getById(syndicatId);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer un syndicat par ID
     *
     * GET /api/v1/syndicats/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SyndicatDetailResponse> getById(@PathVariable UUID id) {
        log.info("API - Récupération syndicat: {}", id);

        SyndicatDetailResponse response = syndicatService.getById(id);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer un syndicat par code
     *
     * GET /api/v1/syndicats/code/{code}
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<SyndicatDetailResponse> getByCode(@PathVariable String code) {
        log.info("API - Récupération syndicat par code: {}", code);

        SyndicatDetailResponse response = syndicatService.getByCode(code);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer tous les syndicats
     *
     * GET /api/v1/syndicats
     */
    @GetMapping
    public ResponseEntity<List<SyndicatResponse>> getAll() {
        log.info("API - Récupération de tous les syndicats");

        List<SyndicatResponse> responses = syndicatService.getAll();

        return ResponseEntity.ok(responses);
    }

    /**
     * Récupérer les syndicats par type
     *
     * GET /api/v1/syndicats/type/{type}
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<SyndicatResponse>> getByType(@PathVariable TypeSyndicat type) {
        log.info("API - Récupération syndicats par type: {}", type);

        List<SyndicatResponse> responses = syndicatService.getByType(type);

        return ResponseEntity.ok(responses);
    }

    /**
     * Récupérer les syndicats par statut
     *
     * GET /api/v1/syndicats/statut/{statut}
     */
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<SyndicatResponse>> getByStatut(@PathVariable StatutSyndicat statut) {
        log.info("API - Récupération syndicats par statut: {}", statut);

        List<SyndicatResponse> responses = syndicatService.getByStatut(statut);

        return ResponseEntity.ok(responses);
    }

    /**
     * Récupérer les syndicats d'une région
     *
     * GET /api/v1/syndicats/region/{regionId}
     */
    @GetMapping("/region/{regionId}")
    public ResponseEntity<List<SyndicatResponse>> getByRegion(@PathVariable UUID regionId) {
        log.info("API - Récupération syndicats de la région: {}", regionId);

        List<SyndicatResponse> responses = syndicatService.getByRegion(regionId);

        return ResponseEntity.ok(responses);
    }

    /**
     * Rechercher des syndicats par nom
     *
     * GET /api/v1/syndicats/search?nom=dakar
     */
    @GetMapping("/search")
    public ResponseEntity<List<SyndicatResponse>> searchByNom(@RequestParam String nom) {
        log.info("API - Recherche syndicats par nom: {}", nom);

        List<SyndicatResponse> responses = syndicatService.searchByNom(nom);

        return ResponseEntity.ok(responses);
    }

    /**
     * Trouver le syndicat gestionnaire d'une commune
     *
     * GET /api/v1/syndicats/gestionnaire/commune/{communeId}
     */
    @GetMapping("/gestionnaire/commune/{communeId}")
    public ResponseEntity<SyndicatResponse> findGestionnaireByCommune(@PathVariable UUID communeId) {
        log.info("API - Recherche syndicat gestionnaire de la commune: {}", communeId);

        SyndicatResponse response = syndicatService.findGestionnaireByCommune(communeId);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════════════
    // MODIFICATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Modifier le syndicat connecté
     *
     * PUT /api/v1/syndicats/me
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('SYNDICAT')")
    public ResponseEntity<SyndicatResponse> updateMyProfile(
            @RequestHeader("X-Syndicat-Id") UUID syndicatId,  // TODO: Extraire du JWT
            @Valid @RequestBody UpdateSyndicatRequest request
    ) {
        log.info("API - Modification profil syndicat: {}", syndicatId);

        SyndicatResponse response = syndicatService.update(syndicatId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * Modifier un syndicat (Admin)
     *
     * PUT /api/v1/syndicats/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SyndicatResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSyndicatRequest request
    ) {
        log.info("API - Modification syndicat: {}", id);

        SyndicatResponse response = syndicatService.update(id, request);

        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════════════
    // GESTION DU STATUT (Admin uniquement)
    // ═══════════════════════════════════════════════════════════

    /**
     * Suspendre un syndicat
     *
     * POST /api/v1/syndicats/{id}/suspendre
     */
    @PostMapping("/{id}/suspendre")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SyndicatResponse>> suspendre(
            @PathVariable UUID id,
            @RequestParam(required = false) String motif
    ) {
        log.info("API - Suspension syndicat: {} - Motif: {}", id, motif);

        SyndicatResponse response = syndicatService.suspendre(id, motif);

        return ResponseEntity.ok(ApiResponse.success("Syndicat suspendu", response));
    }

    /**
     * Réactiver un syndicat
     *
     * POST /api/v1/syndicats/{id}/reactiver
     */
    @PostMapping("/{id}/reactiver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SyndicatResponse>> reactiver(@PathVariable UUID id) {
        log.info("API - Réactivation syndicat: {}", id);

        SyndicatResponse response = syndicatService.reactiver(id);

        return ResponseEntity.ok(ApiResponse.success("Syndicat réactivé", response));
    }

    /**
     * Désactiver définitivement un syndicat
     *
     * DELETE /api/v1/syndicats/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> desactiver(@PathVariable UUID id) {
        log.info("API - Désactivation définitive syndicat: {}", id);

        syndicatService.desactiver(id);

        return ResponseEntity.ok(ApiResponse.success("Syndicat désactivé définitivement", null));
    }
}
