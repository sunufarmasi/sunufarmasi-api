package sn.sunufarmasi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration JWT
 *
 * @author WeCan
 * @since 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtConfig {

    /**
     * Clé secrète pour signer les tokens (min 64 caractères pour HS512)
     */
    private String secret = "SunuFarmasiSecretKey2025VeryLongSecretKeyForHS512AlgorithmAtLeast64Characters";

    /**
     * Durée de validité du token d'accès (en millisecondes)
     * Défaut: 24 heures
     */
    private long accessTokenValidity = 86400000L;

    /**
     * Durée de validité du refresh token (en millisecondes)
     * Défaut: 7 jours
     */
    private long refreshTokenValidity = 604800000L;

    /**
     * Préfixe du token dans le header Authorization
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Nom du header contenant le token
     */
    private String header = "Authorization";

    /**
     * Alias pour compatibilité
     */
    public String getHeaderName() {
        return header;
    }

    /**
     * Issuer (émetteur) du token
     */
    private String issuer = "sunufarmasi.sn";

    /**
     * Audience (destinataire) du token
     */
    private String audience = "sunufarmasi-api";
}