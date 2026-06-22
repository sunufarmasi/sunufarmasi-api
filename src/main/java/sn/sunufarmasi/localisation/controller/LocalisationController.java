package sn.sunufarmasi.localisation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.localisation.dto.request.*;
import sn.sunufarmasi.localisation.dto.response.*;
import sn.sunufarmasi.localisation.service.LocalisationService;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST pour la gestion de la localisation
 * 
 * Endpoints publics (consultation) et protégés (création/modification)
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Localisation", description = "Gestion des pays, régions, départements et communes")
public class LocalisationController {

    private final LocalisationService localisationService;

    // ═══════════════════════════════════════════════════════════
    // LOCALISATIONS - Endpoint consolidé pour l'app mobile
    // ═══════════════════════════════════════════════════════════

    /**
     * GET /api/v1/public/localisations
     * Retourne toutes les régions du Sénégal.
     * Appelé par l'app mobile (ApiService.getLocalisations) au démarrage
     * pour peupler le sélecteur de région.
     */
    @GetMapping("/public/localisations")
    @Operation(summary = "Toutes les régions (app mobile)", description = "Endpoint consolidé utilisé par l'application mobile")
    public ResponseEntity<List<RegionResponse>> getLocalisations() {
        return ResponseEntity.ok(localisationService.getAllRegions());
    }

    // ═══════════════════════════════════════════════════════════
    // PAYS - ENDPOINTS PUBLICS
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/public/pays")
    @Operation(summary = "Liste tous les pays actifs")
    public ResponseEntity<List<PaysResponse>> getAllPays() {
        return ResponseEntity.ok(localisationService.getAllPays());
    }

    @GetMapping("/public/pays/{id}")
    @Operation(summary = "Obtenir un pays par ID")
    public ResponseEntity<PaysResponse> getPaysById(@PathVariable UUID id) {
        return ResponseEntity.ok(localisationService.getPaysById(id));
    }

    @GetMapping("/public/pays/code/{code}")
    @Operation(summary = "Obtenir un pays par code")
    public ResponseEntity<PaysResponse> getPaysByCode(@PathVariable String code) {
        return ResponseEntity.ok(localisationService.getPaysByCode(code));
    }

    // ═══════════════════════════════════════════════════════════
    // PAYS - ENDPOINTS ADMIN
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/admin/pays")
//    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un pays")
    public ResponseEntity<PaysResponse> creerPays(@Valid @RequestBody CreatePaysRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(localisationService.creerPays(request));
    }

    @DeleteMapping("/admin/pays/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Désactiver un pays")
    public ResponseEntity<Void> supprimerPays(@PathVariable UUID id) {
        localisationService.supprimerPays(id);
        return ResponseEntity.noContent().build();
    }

    // ═══════════════════════════════════════════════════════════
    // RÉGIONS - ENDPOINTS PUBLICS
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/public/regions")
    @Operation(summary = "Liste toutes les régions actives")
    public ResponseEntity<List<RegionResponse>> getAllRegions() {
        return ResponseEntity.ok(localisationService.getAllRegions());
    }

    @GetMapping("/public/regions/{id}")
    @Operation(summary = "Obtenir une région par ID")
    public ResponseEntity<RegionResponse> getRegionById(@PathVariable UUID id) {
        return ResponseEntity.ok(localisationService.getRegionById(id));
    }

    @GetMapping("/public/pays/{paysId}/regions")
    @Operation(summary = "Liste les régions d'un pays")
    public ResponseEntity<List<RegionResponse>> getRegionsByPays(@PathVariable UUID paysId) {
        return ResponseEntity.ok(localisationService.getRegionsByPays(paysId));
    }

    @GetMapping("/public/pays/code/{paysCode}/regions")
    @Operation(summary = "Liste les régions d'un pays par code")
    public ResponseEntity<List<RegionResponse>> getRegionsByPaysCode(@PathVariable String paysCode) {
        return ResponseEntity.ok(localisationService.getRegionsByPaysCode(paysCode));
    }

    // ═══════════════════════════════════════════════════════════
    // RÉGIONS - ENDPOINTS ADMIN
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/admin/regions")
//    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer une région")
    public ResponseEntity<RegionResponse> creerRegion(@Valid @RequestBody CreateRegionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(localisationService.creerRegion(request));
    }

    @DeleteMapping("/admin/regions/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Désactiver une région")
    public ResponseEntity<Void> supprimerRegion(@PathVariable UUID id) {
        localisationService.supprimerRegion(id);
        return ResponseEntity.noContent().build();
    }

    // ═══════════════════════════════════════════════════════════
    // DÉPARTEMENTS - ENDPOINTS PUBLICS
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/public/departements")
    @Operation(summary = "Liste tous les départements actifs")
    public ResponseEntity<List<DepartementResponse>> getAllDepartements() {
        return ResponseEntity.ok(localisationService.getAllDepartements());
    }

    @GetMapping("/public/departements/{id}")
    @Operation(summary = "Obtenir un département par ID")
    public ResponseEntity<DepartementResponse> getDepartementById(@PathVariable UUID id) {
        return ResponseEntity.ok(localisationService.getDepartementById(id));
    }

    @GetMapping("/public/regions/{regionId}/departements")
    @Operation(summary = "Liste les départements d'une région")
    public ResponseEntity<List<DepartementResponse>> getDepartementsByRegion(@PathVariable UUID regionId) {
        return ResponseEntity.ok(localisationService.getDepartementsByRegion(regionId));
    }

    // ═══════════════════════════════════════════════════════════
    // DÉPARTEMENTS - ENDPOINTS ADMIN
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/admin/departements")
//    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un département")
    public ResponseEntity<DepartementResponse> creerDepartement(@Valid @RequestBody CreateDepartementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(localisationService.creerDepartement(request));
    }

    @DeleteMapping("/admin/departements/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Désactiver un département")
    public ResponseEntity<Void> supprimerDepartement(@PathVariable UUID id) {
        localisationService.supprimerDepartement(id);
        return ResponseEntity.noContent().build();
    }

    // ═══════════════════════════════════════════════════════════
    // COMMUNES - ENDPOINTS PUBLICS
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/public/communes")
    @Operation(summary = "Liste toutes les communes actives")
    public ResponseEntity<List<CommuneResponse>> getAllCommunes() {
        return ResponseEntity.ok(localisationService.getAllCommunes());
    }

    @GetMapping("/public/communes/{id}")
    @Operation(summary = "Obtenir une commune par ID")
    public ResponseEntity<CommuneResponse> getCommuneById(@PathVariable UUID id) {
        return ResponseEntity.ok(localisationService.getCommuneById(id));
    }

    @GetMapping("/public/departements/{departementId}/communes")
    @Operation(summary = "Liste les communes d'un département")
    public ResponseEntity<List<CommuneResponse>> getCommunesByDepartement(@PathVariable UUID departementId) {
        return ResponseEntity.ok(localisationService.getCommunesByDepartement(departementId));
    }

    @GetMapping("/public/regions/{regionId}/communes")
    @Operation(summary = "Liste les communes d'une région")
    public ResponseEntity<List<CommuneResponse>> getCommunesByRegion(@PathVariable UUID regionId) {
        return ResponseEntity.ok(localisationService.getCommunesByRegion(regionId));
    }

    @GetMapping("/public/communes/search")
    @Operation(summary = "Rechercher des communes par nom")
    public ResponseEntity<List<CommuneResponse>> searchCommunes(@RequestParam String q) {
        return ResponseEntity.ok(localisationService.searchCommunes(q));
    }

    @GetMapping("/public/communes/proximite")
    @Operation(summary = "Trouver les communes proches d'un point")
    public ResponseEntity<List<CommuneResponse>> getCommunesProches(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") Double rayonKm
    ) {
        return ResponseEntity.ok(localisationService.getCommunesProches(latitude, longitude, rayonKm));
    }

    // ═══════════════════════════════════════════════════════════
    // COMMUNES - ENDPOINTS ADMIN
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/admin/communes")
//    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer une commune")
    public ResponseEntity<CommuneResponse> creerCommune(@Valid @RequestBody CreateCommuneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(localisationService.creerCommune(request));
    }

    @DeleteMapping("/admin/communes/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Désactiver une commune")
    public ResponseEntity<Void> supprimerCommune(@PathVariable UUID id) {
        localisationService.supprimerCommune(id);
        return ResponseEntity.noContent().build();
    }

    // ═══════════════════════════════════════════════════════════
    // INITIALISATION
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/admin/localisation/init-senegal")
//    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Initialiser les données du Sénégal")
    public ResponseEntity<String> initialiserSenegal() {
        localisationService.initialiserSenegal();
        return ResponseEntity.ok("Données du Sénégal initialisées avec succès");
    }
}
