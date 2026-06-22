package sn.sunufarmasi.garde.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.garde.dto.GardeDTO.*;
import sn.sunufarmasi.garde.enums.TypeGarde;
import sn.sunufarmasi.garde.service.GardeService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import sn.sunufarmasi.garde.entity.StatutPlanning;

/**
 * Controller REST pour la gestion des gardes
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Gardes", description = "Gestion des gardes de pharmacie")
public class GardeController {

    private final GardeService gardeService;

    // ═══════════════════════════════════════════════════════════
    // PLANNINGS - GESTION (Syndicat/Admin)
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/syndicats/{syndicatId}/plannings")
    @PreAuthorize("hasAnyRole('SYNDICAT', 'ADMIN', 'ADMIN_SYNDICAT', 'PRESIDENT_SYNDICAT', 'SECRETAIRE_SYNDICAT', 'SUPER_ADMIN')")
    @Operation(summary = "Créer un planning de garde")
    public ResponseEntity<PlanningResponse> createPlanning(
            @PathVariable UUID syndicatId,
            @Valid @RequestBody CreatePlanningRequest request,
            @AuthenticationPrincipal UUID userId) {
        // Note: userName devrait venir du SecurityContext
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gardeService.createPlanning(syndicatId, request, userId, "Admin"));
    }

    @PutMapping("/plannings/{planningId}")
    @PreAuthorize("hasAnyRole('SYNDICAT', 'ADMIN', 'ADMIN_SYNDICAT', 'PRESIDENT_SYNDICAT', 'SECRETAIRE_SYNDICAT', 'SUPER_ADMIN')")
    @Operation(summary = "Mettre à jour un planning")
    public ResponseEntity<PlanningResponse> updatePlanning(
            @PathVariable UUID planningId,
            @Valid @RequestBody UpdatePlanningRequest request) {
        return ResponseEntity.ok(gardeService.updatePlanning(planningId, request));
    }

    @PostMapping("/plannings/{planningId}/soumettre")
    @PreAuthorize("hasAnyRole('SYNDICAT', 'ADMIN', 'ADMIN_SYNDICAT', 'PRESIDENT_SYNDICAT', 'SECRETAIRE_SYNDICAT', 'SUPER_ADMIN')")
    @Operation(summary = "Soumettre un planning pour validation")
    public ResponseEntity<PlanningResponse> soumettrePlanning(@PathVariable UUID planningId) {
        return ResponseEntity.ok(gardeService.soumettrePourValidation(planningId));
    }

    @PostMapping("/plannings/{planningId}/valider")
    @PreAuthorize("hasAnyRole('SYNDICAT', 'ADMIN', 'ADMIN_SYNDICAT', 'PRESIDENT_SYNDICAT', 'SUPER_ADMIN')")
    @Operation(summary = "Valider un planning")
    public ResponseEntity<PlanningResponse> validerPlanning(@PathVariable UUID planningId) {
        return ResponseEntity.ok(gardeService.validerPlanning(planningId));
    }

    @PostMapping("/plannings/{planningId}/publier")
    @PreAuthorize("hasAnyRole('SYNDICAT', 'ADMIN', 'ADMIN_SYNDICAT', 'PRESIDENT_SYNDICAT', 'SUPER_ADMIN')")
    @Operation(summary = "Publier un planning (chaîne auto BROUILLON → PUBLIE)")
    public ResponseEntity<PlanningResponse> publierPlanning(
            @PathVariable UUID planningId,
            Authentication authentication) {
        UUID userId = null;
        if (authentication != null && authentication.getPrincipal() != null) {
            try { userId = UUID.fromString(authentication.getPrincipal().toString()); } catch (Exception ignored) {}
        }
        return ResponseEntity.ok(gardeService.publierPlanning(planningId, userId));
    }

    @GetMapping("/plannings/{planningId}")
    @Operation(summary = "Obtenir un planning par ID")
    public ResponseEntity<PlanningResponse> getPlanning(@PathVariable UUID planningId) {
        return ResponseEntity.ok(gardeService.getPlanning(planningId));
    }

    @GetMapping("/syndicats/{syndicatId}/plannings")
    @Operation(summary = "Liste des plannings d'un syndicat")
    public ResponseEntity<List<PlanningResumeResponse>> getPlanningsBySyndicat(
            @PathVariable UUID syndicatId) {
        return ResponseEntity.ok(gardeService.getPlanningsBySyndicat(syndicatId));
    }

    @GetMapping("/syndicats/{syndicatId}/plannings/actuel")
    @Operation(summary = "Planning actuel d'un syndicat")
    public ResponseEntity<PlanningResponse> getPlanningActuel(@PathVariable UUID syndicatId) {
        PlanningResponse planning = gardeService.getPlanningActuel(syndicatId);
        return planning != null ? ResponseEntity.ok(planning) : ResponseEntity.notFound().build();
    }

    // ═══════════════════════════════════════════════════════════
    // GARDES - GESTION (Syndicat/Admin)
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/plannings/{planningId}/gardes")
    @PreAuthorize("hasAnyRole('SYNDICAT', 'ADMIN', 'ADMIN_SYNDICAT', 'PRESIDENT_SYNDICAT', 'SECRETAIRE_SYNDICAT', 'SUPER_ADMIN')")
    @Operation(summary = "Ajouter une garde au planning")
    public ResponseEntity<GardeResponse> addGarde(
            @PathVariable UUID planningId,
            @Valid @RequestBody CreateGardeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gardeService.addGarde(planningId, request));
    }

    @PostMapping("/plannings/{planningId}/gardes/batch")
    @PreAuthorize("hasAnyRole('SYNDICAT', 'ADMIN', 'ADMIN_SYNDICAT', 'PRESIDENT_SYNDICAT', 'SECRETAIRE_SYNDICAT', 'SUPER_ADMIN')")
    @Operation(summary = "Ajouter plusieurs gardes en lot")
    public ResponseEntity<List<GardeResponse>> addGardesBatch(
            @PathVariable UUID planningId,
            @Valid @RequestBody CreateGardesBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gardeService.addGardesBatch(planningId, request));
    }

    @PostMapping("/gardes/{gardeId}/confirmer")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'PHARMACIEN_TITULAIRE', 'ADMIN')")
    @Operation(summary = "Confirmer une garde (par la pharmacie)")
    public ResponseEntity<GardeResponse> confirmerGarde(@PathVariable UUID gardeId) {
        return ResponseEntity.ok(gardeService.confirmerGarde(gardeId));
    }

    @PostMapping("/gardes/{gardeId}/annuler")
    @PreAuthorize("hasAnyRole('SYNDICAT', 'ADMIN', 'ADMIN_SYNDICAT', 'PRESIDENT_SYNDICAT', 'SUPER_ADMIN')")
    @Operation(summary = "Annuler une garde")
    public ResponseEntity<GardeResponse> annulerGarde(
            @PathVariable UUID gardeId,
            @RequestParam String motif) {
        return ResponseEntity.ok(gardeService.annulerGarde(gardeId, motif));
    }

    @DeleteMapping("/gardes/{gardeId}")
    @PreAuthorize("hasAnyRole('SYNDICAT', 'ADMIN', 'ADMIN_SYNDICAT', 'PRESIDENT_SYNDICAT', 'SUPER_ADMIN')")
    @Operation(summary = "Supprimer une garde")
    public ResponseEntity<Void> deleteGarde(@PathVariable UUID gardeId) {
        gardeService.deleteGarde(gardeId);
        return ResponseEntity.noContent().build();
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN - LISTE TOUTES LES GARDES
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/admin/gardes")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Liste toutes les gardes (Super Admin)")
    public ResponseEntity<List<GardeResumeResponse>> getAllGardesAdmin() {
        return ResponseEntity.ok(gardeService.getAllGardesAdmin());
    }

    @GetMapping("/admin/plannings")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Liste tous les plannings de tous les syndicats (Super Admin)")
    public ResponseEntity<List<AdminPlanningResponse>> getAllPlanningsAdmin(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mois,
            @RequestParam(required = false) UUID syndicatId,
            @RequestParam(required = false) StatutPlanning statut) {
        if (mois == null) mois = YearMonth.now();
        return ResponseEntity.ok(gardeService.getAllPlanningsAdmin(mois, syndicatId, statut));
    }

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PUBLIQUE - PHARMACIES DE GARDE
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/gardes/aujourd-hui")
    @Operation(summary = "Pharmacies de garde aujourd'hui")
    public ResponseEntity<List<GardeResumeResponse>> getPharmaciesDeGardeAujourdhui() {
        return ResponseEntity.ok(gardeService.getPharmaciesDeGardeAujourdhui());
    }

    @GetMapping("/gardes")
    @Operation(summary = "Pharmacies de garde pour une date")
    public ResponseEntity<List<GardeResumeResponse>> getPharmaciesDeGarde(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) TypeGarde type) {
        if (type != null) {
            return ResponseEntity.ok(gardeService.getPharmaciesDeGarde(date, type));
        }
        return ResponseEntity.ok(gardeService.getPharmaciesDeGarde(date));
    }

    @GetMapping("/gardes/proximite")
    @Operation(summary = "Pharmacies de garde à proximité")
    public ResponseEntity<List<GardeProximiteResponse>> getPharmaciesDeGardeProximite(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double rayonKm) {
        return ResponseEntity.ok(gardeService.getPharmaciesDeGardeProximite(date, latitude, longitude, rayonKm));
    }

    // ═══════════════════════════════════════════════════════════
    // GARDES PAR PHARMACIE
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/pharmacies/{pharmacieId}/gardes")
    @Operation(summary = "Gardes à venir d'une pharmacie")
    public ResponseEntity<List<GardeResponse>> getGardesAVenirPharmacie(
            @PathVariable UUID pharmacieId) {
        return ResponseEntity.ok(gardeService.getGardesAVenirPharmacie(pharmacieId));
    }

    @GetMapping("/pharmacies/{pharmacieId}/gardes/historique")
    @Operation(summary = "Historique des gardes d'une pharmacie")
    public ResponseEntity<Page<GardeResponse>> getHistoriqueGardesPharmacie(
            @PathVariable UUID pharmacieId,
            Pageable pageable) {
        return ResponseEntity.ok(gardeService.getHistoriqueGardesPharmacie(pharmacieId, pageable));
    }

    // ═══════════════════════════════════════════════════════════
    // ENUMS
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/gardes/enums/types")
    @Operation(summary = "Liste des types de garde")
    public ResponseEntity<TypeGarde[]> getTypesGarde() {
        return ResponseEntity.ok(TypeGarde.values());
    }
}
