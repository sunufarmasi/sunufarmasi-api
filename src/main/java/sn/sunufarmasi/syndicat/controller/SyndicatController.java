package sn.sunufarmasi.syndicat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.pharmacie.dto.response.PharmacieResponse;
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

import sn.sunufarmasi.syndicat.service.SyndicatService.ZoneDisponibleDto;

import java.util.List;
import java.util.Map;
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
public class SyndicatController {

    private final SyndicatService syndicatService;

    // ═══════════════════════════════════════════════════════════
    // CRÉATION (Admin uniquement)
    // ═══════════════════════════════════════════════════════════

    /**
     * Créer un nouveau syndicat (Admin uniquement)
     * POST /api/v1/syndicats
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SyndicatResponse>> create(
            @Valid @RequestBody CreateSyndicatRequest request,
            Authentication authentication
    ) {
        UUID adminId = UUID.fromString(authentication.getName());
        String adminNom = authentication.getName();
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
     * Changer le mot de passe du syndicat connecté
     * PUT /api/v1/syndicats/me/password
     */
    @PutMapping("/me/password")
    @PreAuthorize("hasRole('SYNDICAT')")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordSyndicatRequest request
    ) {
        UUID syndicatId = UUID.fromString(authentication.getName());
        log.info("API - Changement mot de passe syndicat: {}", syndicatId);

        syndicatService.changePassword(syndicatId, request);

        return ResponseEntity.ok(ApiResponse.success("Mot de passe modifié avec succès", null));
    }

    // ═══════════════════════════════════════════════════════════
    // PROFIL CONNECTÉ
    // ═══════════════════════════════════════════════════════════

    /**
     * Récupérer le profil du syndicat connecté
     * GET /api/v1/syndicats/me
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('SYNDICAT')")
    public ResponseEntity<SyndicatDetailResponse> getMyProfile(Authentication authentication) {
        UUID syndicatId = UUID.fromString(authentication.getName());
        log.info("API - Récupération profil syndicat: {}", syndicatId);

        SyndicatDetailResponse response = syndicatService.getById(syndicatId);
        return ResponseEntity.ok(response);
    }

    /**
     * Liste des pharmacies du syndicat connecté
     * GET /api/v1/syndicats/me/pharmacies
     */
    @GetMapping("/me/pharmacies")
    @PreAuthorize("hasRole('SYNDICAT')")
    public ResponseEntity<ApiResponse<List<PharmacieResponse>>> getMyPharmacies(Authentication authentication) {
        UUID syndicatId = UUID.fromString(authentication.getName());
        log.info("API - Récupération pharmacies syndicat: {}", syndicatId);

        List<PharmacieResponse> pharmacies = syndicatService.getPharmaciesBySyndicat(syndicatId);
        return ResponseEntity.ok(ApiResponse.success(pharmacies));
    }

    /**
     * Assigner une pharmacie existante à son réseau
     * POST /api/v1/syndicats/me/pharmacies/{pharmacieId}
     */
    @PostMapping("/me/pharmacies/{pharmacieId}")
    @PreAuthorize("hasRole('SYNDICAT')")
    public ResponseEntity<ApiResponse<PharmacieResponse>> assignerPharmacie(
            Authentication authentication,
            @PathVariable UUID pharmacieId
    ) {
        UUID syndicatId = UUID.fromString(authentication.getName());
        PharmacieResponse response = syndicatService.assignerPharmacie(syndicatId, pharmacieId);
        return ResponseEntity.ok(ApiResponse.success("Pharmacie ajoutée à votre réseau", response));
    }

    /**
     * Modifier le profil du syndicat connecté
     * PUT /api/v1/syndicats/me
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('SYNDICAT')")
    public ResponseEntity<SyndicatResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateSyndicatRequest request
    ) {
        UUID syndicatId = UUID.fromString(authentication.getName());
        log.info("API - Modification profil syndicat: {}", syndicatId);

        SyndicatResponse response = syndicatService.update(syndicatId, request);
        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════════════
    // LECTURE (public + admin)
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/{id}")
    public ResponseEntity<SyndicatDetailResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(syndicatService.getById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<SyndicatDetailResponse> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(syndicatService.getByCode(code));
    }

    @GetMapping
    public ResponseEntity<List<SyndicatResponse>> getAll() {
        return ResponseEntity.ok(syndicatService.getAll());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<SyndicatResponse>> getByType(@PathVariable TypeSyndicat type) {
        return ResponseEntity.ok(syndicatService.getByType(type));
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<SyndicatResponse>> getByStatut(@PathVariable StatutSyndicat statut) {
        return ResponseEntity.ok(syndicatService.getByStatut(statut));
    }

    @GetMapping("/region/{regionId}")
    public ResponseEntity<List<SyndicatResponse>> getByRegion(@PathVariable UUID regionId) {
        return ResponseEntity.ok(syndicatService.getByRegion(regionId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<SyndicatResponse>> searchByNom(@RequestParam String nom) {
        return ResponseEntity.ok(syndicatService.searchByNom(nom));
    }

    @GetMapping("/gestionnaire/commune/{communeId}")
    public ResponseEntity<SyndicatResponse> findGestionnaireByCommune(@PathVariable UUID communeId) {
        SyndicatResponse response = syndicatService.findGestionnaireByCommune(communeId);
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    /**
     * Zones disponibles (non encore couvertes par un syndicat actif)
     * GET /api/v1/syndicats/zones-disponibles?type=COMMUNE|DEPARTEMENT|REGION
     */
    @GetMapping("/zones-disponibles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ZoneDisponibleDto>>> getZonesDisponibles(
            @RequestParam TypeSyndicat type
    ) {
        log.info("API - Zones disponibles pour type: {}", type);
        List<ZoneDisponibleDto> zones = syndicatService.getZonesDisponibles(type);
        return ResponseEntity.ok(ApiResponse.success(zones));
    }

    // ═══════════════════════════════════════════════════════════
    // ADMINISTRATION (Admin uniquement)
    // ═══════════════════════════════════════════════════════════

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SyndicatResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSyndicatRequest request
    ) {
        return ResponseEntity.ok(syndicatService.update(id, request));
    }

    @PostMapping("/{id}/suspendre")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SyndicatResponse>> suspendre(
            @PathVariable UUID id,
            @RequestParam(required = false) String motif
    ) {
        SyndicatResponse response = syndicatService.suspendre(id, motif);
        return ResponseEntity.ok(ApiResponse.success("Syndicat suspendu", response));
    }

    @PostMapping("/{id}/reactiver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SyndicatResponse>> reactiver(@PathVariable UUID id) {
        SyndicatResponse response = syndicatService.reactiver(id);
        return ResponseEntity.ok(ApiResponse.success("Syndicat réactivé", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> supprimer(@PathVariable UUID id) {
        syndicatService.supprimer(id);
        return ResponseEntity.ok(ApiResponse.success("Syndicat supprimé définitivement", null));
    }

    // ═══════════════════════════════════════════════════════════
    // GESTION DES PAIEMENTS (Admin)
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/{id}/enregistrer-paiement")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SyndicatResponse>> enregistrerPaiement(
            @PathVariable UUID id,
            @RequestBody sn.sunufarmasi.syndicat.dto.request.EnregistrerPaiementRequest request
    ) {
        SyndicatResponse response = syndicatService.enregistrerPaiement(id, request);
        return ResponseEntity.ok(ApiResponse.success("Paiement enregistré avec succès", response));
    }

    @PostMapping("/{id}/renouveler")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SyndicatResponse>> renouvelerAbonnement(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "1") int mois) {
        SyndicatResponse response = syndicatService.renouvelerAbonnement(id, mois);
        return ResponseEntity.ok(ApiResponse.success("Abonnement renouvelé de " + mois + " mois", response));
    }

    // ═══════════════════════════════════════════════════════════
    // COMPTES ASSOCIÉS + RESET MOT DE PASSE
    // ═══════════════════════════════════════════════════════════

    /**
     * Comptes associés au syndicat connecté (même préfixe de code)
     * GET /api/v1/syndicats/me/comptes
     */
    @GetMapping("/me/comptes")
    @PreAuthorize("hasRole('SYNDICAT')")
    public ResponseEntity<ApiResponse<List<SyndicatResponse>>> getMyComptes(Authentication authentication) {
        UUID syndicatId = UUID.fromString(authentication.getName());
        List<SyndicatResponse> comptes = syndicatService.getComptesAssocies(syndicatId);
        return ResponseEntity.ok(ApiResponse.success(comptes));
    }

    /**
     * Comptes associés à un syndicat donné (Admin)
     * GET /api/v1/syndicats/{id}/comptes
     */
    @GetMapping("/{id}/comptes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SyndicatResponse>>> getComptes(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(syndicatService.getComptesAssocies(id)));
    }

    /**
     * Réinitialiser le mot de passe d'un syndicat (Admin uniquement)
     * PUT /api/v1/syndicats/{id}/reset-password
     */
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYNDICAT')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body
    ) {
        syndicatService.resetPassword(id, body.get("nouveauMotDePasse"));
        return ResponseEntity.ok(ApiResponse.success("Mot de passe réinitialisé avec succès", null));
    }

    @PostMapping("/{syndicatId}/pharmacies/{pharmacieId}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<sn.sunufarmasi.pharmacie.dto.response.PharmacieResponse>> assignerPharmacieAdmin(
            @PathVariable UUID syndicatId,
            @PathVariable UUID pharmacieId
    ) {
        sn.sunufarmasi.pharmacie.dto.response.PharmacieResponse response =
                syndicatService.assignerPharmacieAdmin(syndicatId, pharmacieId);
        return ResponseEntity.ok(ApiResponse.success("Pharmacie assignée au syndicat", response));
    }
}
