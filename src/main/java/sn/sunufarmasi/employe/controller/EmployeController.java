package sn.sunufarmasi.employe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.employe.dto.request.*;
import sn.sunufarmasi.employe.dto.response.*;
import sn.sunufarmasi.employe.enums.TypePermission;
import sn.sunufarmasi.employe.service.EmployeService;
import sn.sunufarmasi.shared.dto.ApiResponse;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST pour la gestion des employés
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/employes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EmployeController {

    private final EmployeService employeService;

    // ═══════════════════════════════════════════════════════════
    // CRÉATION (Pharmacien uniquement)
    // ═══════════════════════════════════════════════════════════

    /**
     * Créer un nouvel employé
     *
     * POST /api/v1/employes
     */
    @PostMapping
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<ApiResponse<EmployeCreatedResponse>> create(
            @Valid @RequestBody CreateEmployeRequest request,
            @RequestHeader("X-Pharmacien-Id") UUID pharmacienId  // TODO: Extraire du JWT
    ) {
        log.info("API - Création employé par pharmacien: {} pour pharmacie: {}",
                pharmacienId, request.pharmacieId());

        EmployeCreatedResponse response = employeService.create(request, pharmacienId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employé créé avec succès", response));
    }

    // ═══════════════════════════════════════════════════════════
    // AUTHENTIFICATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Connexion employé (username + mot de passe)
     *
     * POST /api/v1/employes/login
     */
    @PostMapping("/login")
    public ResponseEntity<EmployeAuthResponse> login(
            @Valid @RequestBody LoginEmployeRequest request
    ) {
        log.info("API - Connexion employé: {}", request.username());

        EmployeAuthResponse response = employeService.login(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Changer son mot de passe (employé connecté)
     *
     * PUT /api/v1/employes/me/password
     */
    @PutMapping("/me/password")
    @PreAuthorize("hasRole('VENDEUR')")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestHeader("X-Employe-Id") UUID employeId,  // TODO: Extraire du JWT
            @Valid @RequestBody ChangePasswordEmployeRequest request
    ) {
        log.info("API - Changement mot de passe employé: {}", employeId);

        employeService.changePassword(employeId, request);

        return ResponseEntity.ok(ApiResponse.success("Mot de passe modifié avec succès", null));
    }

    /**
     * Réinitialiser le mot de passe d'un employé (par le pharmacien)
     *
     * POST /api/v1/employes/{id}/reset-password
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @PathVariable UUID id,
            @RequestHeader("X-Pharmacien-Id") UUID pharmacienId  // TODO: Extraire du JWT
    ) {
        log.info("API - Réinitialisation mot de passe employé: {} par pharmacien: {}", id, pharmacienId);

        String nouveauMotDePasse = employeService.resetPassword(id, pharmacienId);

        return ResponseEntity.ok(ApiResponse.success(
                "Mot de passe réinitialisé. Communiquez le nouveau mot de passe à l'employé.",
                nouveauMotDePasse
        ));
    }

    // ═══════════════════════════════════════════════════════════
    // LECTURE
    // ═══════════════════════════════════════════════════════════

    /**
     * Récupérer le profil de l'employé connecté
     *
     * GET /api/v1/employes/me
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('VENDEUR')")
    public ResponseEntity<EmployeDetailResponse> getMyProfile(
            @RequestHeader("X-Employe-Id") UUID employeId  // TODO: Extraire du JWT
    ) {
        log.info("API - Récupération profil employé: {}", employeId);

        EmployeDetailResponse response = employeService.getById(employeId);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer un employé par ID
     *
     * GET /api/v1/employes/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'ADMIN')")
    public ResponseEntity<EmployeDetailResponse> getById(@PathVariable UUID id) {
        log.info("API - Récupération employé: {}", id);

        EmployeDetailResponse response = employeService.getById(id);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer un employé par code
     *
     * GET /api/v1/employes/code/{code}
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'ADMIN')")
    public ResponseEntity<EmployeDetailResponse> getByCode(@PathVariable String code) {
        log.info("API - Récupération employé par code: {}", code);

        EmployeDetailResponse response = employeService.getByCode(code);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer les employés d'une pharmacie
     *
     * GET /api/v1/employes/pharmacie/{pharmacieId}
     */
    @GetMapping("/pharmacie/{pharmacieId}")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'ADMIN')")
    public ResponseEntity<List<EmployeResponse>> getByPharmacie(@PathVariable UUID pharmacieId) {
        log.info("API - Récupération employés de la pharmacie: {}", pharmacieId);

        List<EmployeResponse> responses = employeService.getByPharmacie(pharmacieId);

        return ResponseEntity.ok(responses);
    }

    /**
     * Récupérer les employés actifs d'une pharmacie
     *
     * GET /api/v1/employes/pharmacie/{pharmacieId}/actifs
     */
    @GetMapping("/pharmacie/{pharmacieId}/actifs")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'ADMIN')")
    public ResponseEntity<List<EmployeResponse>> getActifsByPharmacie(@PathVariable UUID pharmacieId) {
        log.info("API - Récupération employés actifs de la pharmacie: {}", pharmacieId);

        List<EmployeResponse> responses = employeService.getActifsByPharmacie(pharmacieId);

        return ResponseEntity.ok(responses);
    }

    /**
     * Récupérer mes employés (toutes mes pharmacies)
     *
     * GET /api/v1/employes/mes-employes
     */
    @GetMapping("/mes-employes")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<List<EmployeResponse>> getMesEmployes(
            @RequestHeader("X-Pharmacien-Id") UUID pharmacienId  // TODO: Extraire du JWT
    ) {
        log.info("API - Récupération employés du pharmacien: {}", pharmacienId);

        List<EmployeResponse> responses = employeService.getByPharmacien(pharmacienId);

        return ResponseEntity.ok(responses);
    }

    /**
     * Rechercher des employés par nom dans une pharmacie
     *
     * GET /api/v1/employes/pharmacie/{pharmacieId}/search?nom=ali
     */
    @GetMapping("/pharmacie/{pharmacieId}/search")
    @PreAuthorize("hasAnyRole('PHARMACIEN', 'ADMIN')")
    public ResponseEntity<List<EmployeResponse>> searchByNom(
            @PathVariable UUID pharmacieId,
            @RequestParam String nom
    ) {
        log.info("API - Recherche employés par nom: {} dans pharmacie: {}", nom, pharmacieId);

        List<EmployeResponse> responses = employeService.searchByNom(pharmacieId, nom);

        return ResponseEntity.ok(responses);
    }

    // ═══════════════════════════════════════════════════════════
    // MODIFICATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Modifier un employé
     *
     * PUT /api/v1/employes/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<EmployeResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEmployeRequest request,
            @RequestHeader("X-Pharmacien-Id") UUID pharmacienId  // TODO: Extraire du JWT
    ) {
        log.info("API - Modification employé: {} par pharmacien: {}", id, pharmacienId);

        EmployeResponse response = employeService.update(id, request, pharmacienId);

        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════════════
    // GESTION DES PERMISSIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Mettre à jour toutes les permissions d'un employé
     *
     * PUT /api/v1/employes/{id}/permissions
     */
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<EmployeResponse> updatePermissions(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePermissionsRequest request,
            @RequestHeader("X-Pharmacien-Id") UUID pharmacienId  // TODO: Extraire du JWT
    ) {
        log.info("API - Mise à jour permissions employé: {} par pharmacien: {}", id, pharmacienId);

        EmployeResponse response = employeService.updatePermissions(id, request, pharmacienId);

        return ResponseEntity.ok(response);
    }

    /**
     * Ajouter une permission à un employé
     *
     * POST /api/v1/employes/{id}/permissions/{permission}
     */
    @PostMapping("/{id}/permissions/{permission}")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<EmployeResponse> addPermission(
            @PathVariable UUID id,
            @PathVariable TypePermission permission,
            @RequestHeader("X-Pharmacien-Id") UUID pharmacienId  // TODO: Extraire du JWT
    ) {
        log.info("API - Ajout permission {} à employé: {}", permission, id);

        EmployeResponse response = employeService.addPermission(id, permission, pharmacienId);

        return ResponseEntity.ok(response);
    }

    /**
     * Retirer une permission à un employé
     *
     * DELETE /api/v1/employes/{id}/permissions/{permission}
     */
    @DeleteMapping("/{id}/permissions/{permission}")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<EmployeResponse> removePermission(
            @PathVariable UUID id,
            @PathVariable TypePermission permission,
            @RequestHeader("X-Pharmacien-Id") UUID pharmacienId  // TODO: Extraire du JWT
    ) {
        log.info("API - Retrait permission {} de employé: {}", permission, id);

        EmployeResponse response = employeService.removePermission(id, permission, pharmacienId);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer toutes les permissions disponibles
     *
     * GET /api/v1/employes/permissions
     */
    @GetMapping("/permissions")
    public ResponseEntity<TypePermission[]> getAllPermissions() {
        return ResponseEntity.ok(TypePermission.values());
    }

    /**
     * Récupérer les permissions par défaut
     *
     * GET /api/v1/employes/permissions/defaut
     */
    @GetMapping("/permissions/defaut")
    public ResponseEntity<TypePermission[]> getDefaultPermissions() {
        return ResponseEntity.ok(TypePermission.getPermissionsDefaut());
    }

    // ═══════════════════════════════════════════════════════════
    // GESTION DU STATUT
    // ═══════════════════════════════════════════════════════════

    /**
     * Suspendre un employé
     *
     * POST /api/v1/employes/{id}/suspendre
     */
    @PostMapping("/{id}/suspendre")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<ApiResponse<EmployeResponse>> suspendre(
            @PathVariable UUID id,
            @RequestParam(required = false) String motif,
            @RequestHeader("X-Pharmacien-Id") UUID pharmacienId  // TODO: Extraire du JWT
    ) {
        log.info("API - Suspension employé: {} - Motif: {}", id, motif);

        EmployeResponse response = employeService.suspendre(id, motif, pharmacienId);

        return ResponseEntity.ok(ApiResponse.success("Employé suspendu", response));
    }

    /**
     * Réactiver un employé
     *
     * POST /api/v1/employes/{id}/reactiver
     */
    @PostMapping("/{id}/reactiver")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<ApiResponse<EmployeResponse>> reactiver(
            @PathVariable UUID id,
            @RequestHeader("X-Pharmacien-Id") UUID pharmacienId  // TODO: Extraire du JWT
    ) {
        log.info("API - Réactivation employé: {}", id);

        EmployeResponse response = employeService.reactiver(id, pharmacienId);

        return ResponseEntity.ok(ApiResponse.success("Employé réactivé", response));
    }

    /**
     * Désactiver définitivement un employé (licenciement/démission)
     *
     * DELETE /api/v1/employes/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<ApiResponse<Void>> desactiver(
            @PathVariable UUID id,
            @RequestParam(required = false) String motif,
            @RequestHeader("X-Pharmacien-Id") UUID pharmacienId  // TODO: Extraire du JWT
    ) {
        log.info("API - Désactivation employé: {} - Motif: {}", id, motif);

        employeService.desactiver(id, motif, pharmacienId);

        return ResponseEntity.ok(ApiResponse.success("Employé désactivé", null));
    }
}
