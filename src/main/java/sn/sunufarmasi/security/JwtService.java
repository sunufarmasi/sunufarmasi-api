package sn.sunufarmasi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sn.sunufarmasi.user.entity.User;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service de gestion des tokens JWT
 * Compatible avec JJWT 0.12.x
 *
 * @author WeCan
 * @since 1.0.0
 */
@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    @Value("${jwt.access-token-expiration:86400000}")
    private Long accessTokenExpiration; // 24h par défaut

    @Value("${jwt.refresh-token-expiration:604800000}")
    private Long refreshTokenExpiration; // 7 jours par défaut

    /**
     * Extraire le username (email) du token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extraire une claim spécifique
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Générer un access token
     */
    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId().toString());
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("nom", user.getNomComplet());
        return buildToken(extraClaims, user.getEmail(), accessTokenExpiration);
    }

    /**
     * Générer un refresh token
     */
    public String generateRefreshToken(User user) {
        return buildToken(new HashMap<>(), user.getEmail(), refreshTokenExpiration);
    }

    /**
     * Construire un token
     */
    private String buildToken(Map<String, Object> extraClaims, String subject, Long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Vérifier si le token est valide pour un User
     */
    public boolean isTokenValid(String token, User user) {
        try {
            final String username = extractUsername(token);
            return (username.equals(user.getEmail())) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token invalide: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Vérifier si le token est expiré
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extraire la date d'expiration
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extraire toutes les claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Obtenir la clé de signature
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Durée d'expiration access token (en secondes)
     */
    public Long getAccessTokenExpiration() {
        return accessTokenExpiration / 1000;
    }

    /**
     * Durée d'expiration refresh token (en secondes)
     */
    public Long getRefreshTokenExpiration() {
        return refreshTokenExpiration / 1000;
    }
}