package sn.sunufarmasi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import sn.sunufarmasi.config.JwtConfig;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provider JWT pour la génération et validation des tokens
 * Compatible avec JJWT 0.12.x
 *
 * @author WeCan
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;

    /**
     * Obtenir la clé secrète pour signer les tokens
     * JJWT 0.12+ requiert une SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ═══════════════════════════════════════════════════════════
    // GÉNÉRATION DE TOKENS
    // ═══════════════════════════════════════════════════════════

    /**
     * Générer un token d'accès à partir d'une authentification Spring Security
     */
    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateAccessToken(
                userDetails.getUsername(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Générer un token d'accès avec username et liste de rôles
     */
    public String generateAccessToken(String username, List<String> roles) {
        return generateTokenInternal(username, roles, jwtConfig.getAccessTokenValidity());
    }

    /**
     * Générer un token d'accès avec username et un seul rôle
     * Utilisé par EmployeService
     */
    public String generateToken(String userId, String role) {
        return generateTokenInternal(userId, Collections.singletonList(role), jwtConfig.getAccessTokenValidity());
    }

    /**
     * Générer un refresh token (sans rôles)
     */
    public String generateRefreshToken(String username) {
        return generateTokenInternal(username, null, jwtConfig.getRefreshTokenValidity());
    }

    /**
     * Générer un refresh token avec un rôle
     * Utilisé par EmployeService
     */
    public String generateRefreshToken(String userId, String role) {
        return generateTokenInternal(userId, Collections.singletonList(role), jwtConfig.getRefreshTokenValidity());
    }

    /**
     * Méthode interne pour générer un token JWT
     * Compatible JJWT 0.12.x
     */
    private String generateTokenInternal(String subject, List<String> roles, long validity) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validity);

        Map<String, Object> claims = new HashMap<>();

        if (roles != null && !roles.isEmpty()) {
            claims.put("roles", roles);
        }

        // JJWT 0.12.x syntax
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer(jwtConfig.getIssuer())
                .audience().add(jwtConfig.getAudience()).and()
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    // ═══════════════════════════════════════════════════════════
    // EXTRACTION D'INFORMATIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Extraire le username (email ou userId) du token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * Alias pour getUsernameFromToken
     */
    public String getSubjectFromToken(String token) {
        return getUsernameFromToken(token);
    }

    /**
     * Extraire les rôles du token
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null && claims.containsKey("roles")) {
            return (List<String>) claims.get("roles");
        }
        return Collections.emptyList();
    }

    /**
     * Extraire la date d'expiration du token
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }

    /**
     * Extraire tous les claims du token
     * Compatible JJWT 0.12.x - utilise parser().verifyWith()
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Erreur lors de l'extraction des claims: {}", e.getMessage());
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // VALIDATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Valider un token JWT
     * Compatible JJWT 0.12.x
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (SecurityException ex) {
            log.error("Signature JWT invalide: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Token JWT invalide: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Token JWT expiré: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Token JWT non supporté: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("Claims JWT vide: {}", ex.getMessage());
        }

        return false;
    }

    /**
     * Vérifier si un token est expiré
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Valider un token pour un utilisateur spécifique
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = getUsernameFromToken(token);
        return (username != null &&
                username.equals(userDetails.getUsername()) &&
                !isTokenExpired(token));
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Obtenir le temps restant avant expiration (en secondes)
     */
    public long getTimeToExpiration(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            if (expiration == null) {
                return 0;
            }
            long now = System.currentTimeMillis();
            long expiryTime = expiration.getTime();
            return Math.max(0, (expiryTime - now) / 1000);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Obtenir la durée de validité du token d'accès (en secondes)
     * Utilisé par EmployeService
     */
    public long getExpirationTime() {
        return jwtConfig.getAccessTokenValidity() / 1000;
    }

    /**
     * Obtenir la durée de validité du token d'accès (en millisecondes)
     */
    public long getAccessTokenValidityMs() {
        return jwtConfig.getAccessTokenValidity();
    }

    /**
     * Obtenir la durée de validité du refresh token (en millisecondes)
     */
    public long getRefreshTokenValidityMs() {
        return jwtConfig.getRefreshTokenValidity();
    }

    /**
     * Extraire le token du header Authorization
     */
    public String extractTokenFromBearer(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(jwtConfig.getTokenPrefix())) {
            return bearerToken.substring(jwtConfig.getTokenPrefix().length());
        }
        return null;
    }

    /**
     * Vérifier si le header contient un token Bearer
     */
    public boolean hasBearerToken(String bearerToken) {
        return bearerToken != null && bearerToken.startsWith(jwtConfig.getTokenPrefix());
    }
}