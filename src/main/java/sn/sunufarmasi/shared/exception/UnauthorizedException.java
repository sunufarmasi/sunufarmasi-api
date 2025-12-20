package sn.sunufarmasi.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception levée lorsqu'un utilisateur n'est pas authentifié
 * Retourne un code HTTP 401 (UNAUTHORIZED)
 *
 * @author WeCan
 * @since 1.0.0
 */
public class UnauthorizedException extends ApiException {

    /**
     * Constructeur avec message simple
     *
     * @param message Message d'erreur
     */
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }

    /**
     * Constructeur avec code erreur personnalisé
     *
     * @param message Message d'erreur
     * @param errorCode Code erreur métier
     */
    public UnauthorizedException(String message, String errorCode) {
        super(message, HttpStatus.UNAUTHORIZED, errorCode);
    }

    /**
     * Exception pour token JWT invalide
     */
    public static UnauthorizedException invalidToken() {
        return new UnauthorizedException(
                "Token d'authentification invalide ou expiré",
                "INVALID_TOKEN"
        );
    }

    /**
     * Exception pour token JWT manquant
     */
    public static UnauthorizedException missingToken() {
        return new UnauthorizedException(
                "Token d'authentification manquant",
                "MISSING_TOKEN"
        );
    }

    /**
     * Exception pour identifiants incorrects
     */
    public static UnauthorizedException invalidCredentials() {
        return new UnauthorizedException(
                "Email ou mot de passe incorrect",
                "INVALID_CREDENTIALS"
        );
    }

    /**
     * Exception pour compte non vérifié
     */
    public static UnauthorizedException accountNotVerified() {
        return new UnauthorizedException(
                "Compte non vérifié. Veuillez vérifier votre téléphone avec le code OTP",
                "ACCOUNT_NOT_VERIFIED"
        );
    }

    /**
     * Exception pour compte désactivé
     */
    public static UnauthorizedException accountDisabled() {
        return new UnauthorizedException(
                "Votre compte a été désactivé. Contactez l'administrateur",
                "ACCOUNT_DISABLED"
        );
    }

    /**
     * Exception pour session expirée
     */
    public static UnauthorizedException sessionExpired() {
        return new UnauthorizedException(
                "Votre session a expiré. Veuillez vous reconnecter",
                "SESSION_EXPIRED"
        );
    }
}