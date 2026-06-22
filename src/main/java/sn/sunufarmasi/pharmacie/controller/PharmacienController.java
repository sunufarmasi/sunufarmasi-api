package sn.sunufarmasi.pharmacie.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import sn.sunufarmasi.pharmacie.dto.request.LoginPharmacienRequest;
import sn.sunufarmasi.pharmacie.dto.request.RegisterPharmacienRequest;
import sn.sunufarmasi.pharmacie.dto.request.UpdatePharmacienRequest;
import sn.sunufarmasi.pharmacie.dto.response.AuthResponse;
import sn.sunufarmasi.pharmacie.dto.response.PharmacienResponse;
import sn.sunufarmasi.pharmacie.service.PharmacienService;

import java.util.UUID;

/**
 * Controller REST pour la gestion des pharmaciens
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/pharmaciens")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PharmacienController {

    private final PharmacienService pharmacienService;

    /**
     * Inscription d'un nouveau pharmacien
     *
     * POST /api/v1/pharmaciens/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterPharmacienRequest request) {
        log.info("API - Inscription pharmacien: {}", request.telephone());

        AuthResponse response = pharmacienService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Connexion d'un pharmacien
     *
     * POST /api/v1/pharmaciens/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginPharmacienRequest request) {
        log.info("API - Connexion pharmacien: {}", request.telephone());

        AuthResponse response = pharmacienService.login(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer le profil du pharmacien connecté
     *
     * GET /api/v1/pharmaciens/me
     * Header: Authorization: Bearer {token}
     */
    @GetMapping("/me")
    public ResponseEntity<PharmacienResponse> getProfile(
            @RequestHeader("X-Pharmacien-Id") UUID pharmacienId // TODO: Extraire du JWT
    ) {
        log.info("API - Récupération profil pharmacien: {}", pharmacienId);

        PharmacienResponse response = pharmacienService.getProfile(pharmacienId);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer un pharmacien par ID
     *
     * GET /api/v1/pharmaciens/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PharmacienResponse> getById(@PathVariable UUID id) {
        log.info("API - Récupération pharmacien: {}", id);

        PharmacienResponse response = pharmacienService.getProfile(id);

        return ResponseEntity.ok(response);
    }

    /**
     * Vérifier si le pharmacien peut créer une pharmacie
     *
     * GET /api/v1/pharmaciens/me/can-create-pharmacie
     */
    @GetMapping("/me/can-create-pharmacie")
    public ResponseEntity<Boolean> canCreatePharmacie(
            @RequestHeader("X-Pharmacien-Id") UUID pharmacienId // TODO: Extraire du JWT
    ) {
        log.info("API - Vérification création pharmacie pour: {}", pharmacienId);

        boolean canCreate = pharmacienService.canCreatePharmacie(pharmacienId);

        return ResponseEntity.ok(canCreate);
    }

    /**
     * Modifier un pharmacien (Admin)
     *
     * PUT /api/v1/pharmaciens/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PharmacienResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePharmacienRequest request
    ) {
        log.info("API Admin - Modification pharmacien: {}", id);
        return ResponseEntity.ok(pharmacienService.update(id, request));
    }

    /**
     * Lister tous les pharmaciens (ADMIN)
     * GET /api/v1/pharmaciens
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PharmacienResponse>> getAll() {
        log.info("API - Liste tous les pharmaciens");
        return ResponseEntity.ok(pharmacienService.getAll());
    }

    /**
     * Supprimer un pharmacien (Admin)
     * DELETE /api/v1/pharmaciens/{id}/admin
     */
    @DeleteMapping("/{id}/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable UUID id) {
        log.info("API Admin - Suppression pharmacien: {}", id);
        pharmacienService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}