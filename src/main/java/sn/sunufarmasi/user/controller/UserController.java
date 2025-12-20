package sn.sunufarmasi.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.shared.dto.ApiResponse;
import sn.sunufarmasi.user.dto.UserDto.*;
import sn.sunufarmasi.user.entity.User.RoleUser;
import sn.sunufarmasi.user.entity.User.StatutUser;
import sn.sunufarmasi.user.service.UserService;

import java.util.List;
import java.util.UUID;

/**
 * Controller pour Authentification User (email/password)
 * 
 * ROUTES:
 * - /api/v1/users/auth/* → Login/Register avec email/password (Pharmaciens, Syndicats, Admins)
 * - /api/v1/users/me → Profil utilisateur connecté
 * - /api/v1/admin/users/* → Gestion admin des utilisateurs
 * 
 * NOTE: Les patients utilisent /api/v1/auth/* avec OTP (AuthController séparé)
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "Authentification et gestion des utilisateurs (email/password)")
public class UserController {

    private final UserService userService;

    // ═══════════════════════════════════════════════════════════════════════════
    // AUTHENTIFICATION EMAIL/PASSWORD (PUBLIC)
    // Distinct de /api/v1/auth/* qui est pour les patients avec OTP
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Connexion avec email/password
     * POST /api/v1/users/auth/login
     */
    @PostMapping("/users/auth/login")
    @Operation(summary = "Connexion (email/password)", 
               description = "Pour Pharmaciens, Syndicats, Admins. Les patients utilisent /api/v1/auth/login avec OTP")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/v1/users/auth/login - email: {}", request.email());
        
        AuthResponse response = userService.login(request);
        
        return ResponseEntity.ok(ApiResponse.success("Connexion réussie", response));
    }

    /**
     * Inscription avec email/password
     * POST /api/v1/users/auth/register
     */
    @PostMapping("/users/auth/register")
    @Operation(summary = "Inscription (email/password)",
               description = "Créer un compte avec email/password")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/v1/users/auth/register - email: {}", request.email());
        
        AuthResponse response = userService.register(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inscription réussie", response));
    }

    /**
     * Rafraîchir le token
     * POST /api/v1/users/auth/refresh
     */
    @PostMapping("/users/auth/refresh")
    @Operation(summary = "Rafraîchir le token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("POST /api/v1/users/auth/refresh");
        
        AuthResponse response = userService.refreshToken(request.refreshToken());
        
        return ResponseEntity.ok(ApiResponse.success("Token rafraîchi", response));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PROFIL UTILISATEUR CONNECTÉ
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Mon profil
     * GET /api/v1/users/me
     */
    @GetMapping("/users/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mon profil")
    public ResponseEntity<ApiResponse<UserResponse>> getMonProfil(Authentication authentication) {
        log.info("GET /api/v1/users/me - User: {}", authentication.getName());
        
        String userId = (String) authentication.getPrincipal();
        UserResponse response = userService.getById(userId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Modifier mon profil
     * PUT /api/v1/users/me
     */
    @PutMapping("/users/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Modifier mon profil")
    public ResponseEntity<ApiResponse<UserResponse>> updateMonProfil(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        log.info("PUT /api/v1/users/me - User: {}", authentication.getName());
        
        String userId = (String) authentication.getPrincipal();
        UserResponse response = userService.updateProfile(userId, request);
        
        return ResponseEntity.ok(ApiResponse.success("Profil mis à jour", response));
    }

    /**
     * Changer mon mot de passe
     * PUT /api/v1/users/me/password
     */
//    @PutMapping("/users/me/password")
//    @SecurityRequirement(name = "bearerAuth")
//    @Operation(summary = "Changer mon mot de passe")
//    public ResponseEntity<ApiResponse<Void>> changePassword(
//            Authentication authentication,
//            @Valid @RequestBody ChangePasswordRequest request
//    ) {
//        log.info("PUT /api/v1/users/me/password - User: {}", authentication.getName());
//
//        String userId = (String) authentication.getPrincipal();
//        userService.changePassword(userId, request);
//
//        return ResponseEntity.ok(ApiResponse.success("Mot de passe modifié"));
//    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ADMINISTRATION (ADMIN ONLY)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Liste tous les utilisateurs
     * GET /api/v1/admin/users
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Liste tous les utilisateurs (Admin)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(Pageable pageable) {
        log.info("GET /api/v1/admin/users");
        
        Page<UserResponse> page = userService.getAll(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    /**
     * Détail d'un utilisateur
     * GET /api/v1/admin/users/{id}
     */
    @GetMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Détail utilisateur (Admin)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        log.info("GET /api/v1/admin/users/{}", id);
        
        UserResponse response = userService.getById(id);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Utilisateurs par rôle
     * GET /api/v1/admin/users/role/{role}
     */
    @GetMapping("/admin/users/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Utilisateurs par rôle (Admin)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@PathVariable RoleUser role) {
        log.info("GET /api/v1/admin/users/role/{}", role);
        
        List<UserResponse> users = userService.getByRole(role);
        
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Rechercher des utilisateurs
     * GET /api/v1/admin/users/search?q=xxx
     */
    @GetMapping("/admin/users/search")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Rechercher utilisateurs (Admin)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(@RequestParam String q) {
        log.info("GET /api/v1/admin/users/search?q={}", q);
        
        List<UserResponse> users = userService.search(q);
        
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Modifier un utilisateur
     * PUT /api/v1/admin/users/{id}
     */
    @PutMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Modifier utilisateur (Admin)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        log.info("PUT /api/v1/admin/users/{}", id);
        
        UserResponse response = userService.updateProfile(id, request);
        
        return ResponseEntity.ok(ApiResponse.success("Utilisateur mis à jour", response));
    }

    /**
     * Changer le statut d'un utilisateur
     * PUT /api/v1/admin/users/{id}/statut?statut=ACTIF
     */
    @PutMapping("/admin/users/{id}/statut")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Changer statut utilisateur (Admin)")
    public ResponseEntity<ApiResponse<UserResponse>> changeStatut(
            @PathVariable UUID id,
            @RequestParam StatutUser statut
    ) {
        log.info("PUT /api/v1/admin/users/{}/statut?statut={}", id, statut);
        
        UserResponse response = userService.changeStatut(id, statut);
        
        return ResponseEntity.ok(ApiResponse.success("Statut modifié en " + statut, response));
    }

    /**
     * Activer un utilisateur
     * PUT /api/v1/admin/users/{id}/activer
     */
    @PutMapping("/admin/users/{id}/activer")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Activer utilisateur (Admin)")
    public ResponseEntity<ApiResponse<UserResponse>> activerUser(@PathVariable UUID id) {
        log.info("PUT /api/v1/admin/users/{}/activer", id);
        
        UserResponse response = userService.changeStatut(id, StatutUser.ACTIF);
        
        return ResponseEntity.ok(ApiResponse.success("Utilisateur activé", response));
    }

    /**
     * Suspendre un utilisateur
     * PUT /api/v1/admin/users/{id}/suspendre
     */
    @PutMapping("/admin/users/{id}/suspendre")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Suspendre utilisateur (Admin)")
    public ResponseEntity<ApiResponse<UserResponse>> suspendreUser(@PathVariable UUID id) {
        log.info("PUT /api/v1/admin/users/{}/suspendre", id);
        
        UserResponse response = userService.changeStatut(id, StatutUser.SUSPENDU);
        
        return ResponseEntity.ok(ApiResponse.success("Utilisateur suspendu", response));
    }
}
