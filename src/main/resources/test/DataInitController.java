package sn.sunufarmasi.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller d'initialisation des données de test
 * ⚠️ À DÉSACTIVER EN PRODUCTION !
 * 
 * Permet de créer un admin et des données de test sans authentification
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/init")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Initialisation", description = "⚠️ Endpoints d'initialisation - DÉSACTIVER EN PRODUCTION")
public class DataInitController {

    private final PasswordEncoder passwordEncoder;
    
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Créer un compte admin
     * POST /api/v1/init/admin
     */
    @PostMapping("/admin")
    @Transactional
    @Operation(summary = "Créer un compte admin", description = "⚠️ À désactiver en production")
    public ResponseEntity<Map<String, Object>> createAdmin(
            @RequestParam(defaultValue = "admin@sunufarmasi.sn") String email,
            @RequestParam(defaultValue = "password123") String password,
            @RequestParam(defaultValue = "Admin") String prenom,
            @RequestParam(defaultValue = "System") String nom,
            @RequestParam(defaultValue = "+221770000000") String telephone
    ) {
        log.warn("⚠️ Création d'un compte admin via endpoint public!");

        // Vérifier si l'admin existe déjà
        Long count = (Long) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM users WHERE email = :email")
                .setParameter("email", email)
                .getSingleResult();

        if (count > 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Un utilisateur avec cet email existe déjà",
                    "email", email
            ));
        }

        // Hasher le mot de passe
        String hashedPassword = passwordEncoder.encode(password);

        // Créer l'admin
        entityManager.createNativeQuery("""
            INSERT INTO users (id, email, password, nom, prenom, telephone, role, statut, email_verifie, created_at, updated_at)
            VALUES (gen_random_uuid(), :email, :password, :nom, :prenom, :telephone, 'ADMIN', 'ACTIF', true, NOW(), NOW())
            """)
                .setParameter("email", email)
                .setParameter("password", hashedPassword)
                .setParameter("nom", nom)
                .setParameter("prenom", prenom)
                .setParameter("telephone", telephone)
                .executeUpdate();

        log.info("✅ Admin créé: {}", email);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Compte admin créé avec succès");
        response.put("email", email);
        response.put("password", password);
        response.put("role", "ADMIN");

        return ResponseEntity.ok(response);
    }

    /**
     * Créer un compte pharmacien
     * POST /api/v1/init/pharmacien
     */
    @PostMapping("/pharmacien")
    @Transactional
    @Operation(summary = "Créer un compte pharmacien")
    public ResponseEntity<Map<String, Object>> createPharmacien(
            @RequestParam String email,
            @RequestParam(defaultValue = "password123") String password,
            @RequestParam String prenom,
            @RequestParam String nom,
            @RequestParam String telephone
    ) {
        log.warn("⚠️ Création d'un compte pharmacien via endpoint public!");

        // Vérifier si existe déjà
        Long count = (Long) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM users WHERE email = :email")
                .setParameter("email", email)
                .getSingleResult();

        if (count > 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Un utilisateur avec cet email existe déjà"
            ));
        }

        String hashedPassword = passwordEncoder.encode(password);

        entityManager.createNativeQuery("""
            INSERT INTO users (id, email, password, nom, prenom, telephone, role, statut, email_verifie, created_at, updated_at)
            VALUES (gen_random_uuid(), :email, :password, :nom, :prenom, :telephone, 'PHARMACIEN', 'ACTIF', true, NOW(), NOW())
            """)
                .setParameter("email", email)
                .setParameter("password", hashedPassword)
                .setParameter("nom", nom)
                .setParameter("prenom", prenom)
                .setParameter("telephone", telephone)
                .executeUpdate();

        log.info("✅ Pharmacien créé: {}", email);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Compte pharmacien créé avec succès",
                "email", email,
                "password", password,
                "role", "PHARMACIEN"
        ));
    }

    /**
     * Vérifier un mot de passe (pour debug)
     * POST /api/v1/init/verify-password
     */
    @PostMapping("/verify-password")
    @Operation(summary = "Vérifier un mot de passe")
    public ResponseEntity<Map<String, Object>> verifyPassword(
            @RequestParam String email,
            @RequestParam String password
    ) {
        try {
            String hashedPassword = (String) entityManager.createNativeQuery(
                    "SELECT password FROM users WHERE email = :email")
                    .setParameter("email", email)
                    .getSingleResult();

            boolean matches = passwordEncoder.matches(password, hashedPassword);

            return ResponseEntity.ok(Map.of(
                    "email", email,
                    "passwordMatches", matches,
                    "hashedPassword", hashedPassword.substring(0, 20) + "..."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Utilisateur non trouvé: " + email
            ));
        }
    }

    /**
     * Obtenir le hash BCrypt d'un mot de passe
     * GET /api/v1/init/hash?password=xxx
     */
    @GetMapping("/hash")
    @Operation(summary = "Hasher un mot de passe avec BCrypt")
    public ResponseEntity<Map<String, String>> hashPassword(
            @RequestParam(defaultValue = "password123") String password
    ) {
        String hash = passwordEncoder.encode(password);
        return ResponseEntity.ok(Map.of(
                "password", password,
                "bcryptHash", hash
        ));
    }

    /**
     * Liste les utilisateurs (debug)
     * GET /api/v1/init/users
     */
    @GetMapping("/users")
    @Operation(summary = "Lister les utilisateurs existants")
    public ResponseEntity<?> listUsers() {
        var result = entityManager.createNativeQuery("""
            SELECT id, email, nom, prenom, role, statut, created_at 
            FROM users 
            ORDER BY created_at DESC 
            LIMIT 20
            """)
                .getResultList();

        return ResponseEntity.ok(Map.of(
                "count", result.size(),
                "users", result
        ));
    }

    /**
     * Statistiques des données
     * GET /api/v1/init/stats
     */
    @GetMapping("/stats")
    @Operation(summary = "Statistiques des données en base")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        String[] tables = {"users", "pays", "regions", "departements", "communes", 
                          "syndicats", "pharmacies", "employes", "produits", "stocks",
                          "gardes", "ventes", "commandes", "mutuelles", "notifications"};

        for (String table : tables) {
            try {
                Long count = (Long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM " + table)
                        .getSingleResult();
                stats.put(table, count);
            } catch (Exception e) {
                stats.put(table, "Table non trouvée");
            }
        }

        return ResponseEntity.ok(stats);
    }

    /**
     * Réinitialiser le mot de passe d'un utilisateur
     * POST /api/v1/init/reset-password
     */
    @PostMapping("/reset-password")
    @Transactional
    @Operation(summary = "Réinitialiser le mot de passe d'un utilisateur")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @RequestParam String email,
            @RequestParam(defaultValue = "password123") String newPassword
    ) {
        String hashedPassword = passwordEncoder.encode(newPassword);

        int updated = entityManager.createNativeQuery(
                "UPDATE users SET password = :password, updated_at = NOW() WHERE email = :email")
                .setParameter("password", hashedPassword)
                .setParameter("email", email)
                .executeUpdate();

        if (updated > 0) {
            log.info("✅ Mot de passe réinitialisé pour: {}", email);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Mot de passe réinitialisé",
                    "email", email,
                    "newPassword", newPassword
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Utilisateur non trouvé"
            ));
        }
    }
}
