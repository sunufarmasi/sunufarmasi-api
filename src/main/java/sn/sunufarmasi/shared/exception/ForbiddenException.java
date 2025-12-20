package sn.sunufarmasi.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception levée lorsqu'un utilisateur authentifié n'a pas les permissions nécessaires
 * Retourne un code HTTP 403 (FORBIDDEN)
 *
 * @author WeCan
 * @since 1.0.0
 */
public class ForbiddenException extends ApiException {

    /**
     * Constructeur avec message simple
     *
     * @param message Message d'erreur
     */
    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    /**
     * Constructeur avec code erreur personnalisé
     *
     * @param message Message d'erreur
     * @param errorCode Code erreur métier
     */
    public ForbiddenException(String message, String errorCode) {
        super(message, HttpStatus.FORBIDDEN, errorCode);
    }

    /**
     * Exception pour accès à une ressource interdite
     *
     * @param resource Nom de la ressource
     */
    public static ForbiddenException accessDenied(String resource) {
        return new ForbiddenException(
                String.format("Vous n'avez pas l'autorisation d'accéder à : %s", resource),
                "ACCESS_DENIED"
        );
    }

    /**
     * Exception pour rôle insuffisant
     *
     * @param requiredRole Rôle requis
     */
    public static ForbiddenException insufficientRole(String requiredRole) {
        return new ForbiddenException(
                String.format("Cette action nécessite le rôle : %s", requiredRole),
                "INSUFFICIENT_ROLE"
        );
    }

    /**
     * Exception pour opération non autorisée
     *
     * @param operation Nom de l'opération
     */
    public static ForbiddenException operationNotAllowed(String operation) {
        return new ForbiddenException(
                String.format("Opération non autorisée : %s", operation),
                "OPERATION_NOT_ALLOWED"
        );
    }

    /**
     * Exception pour ressource appartenant à un autre utilisateur
     */
    public static ForbiddenException notResourceOwner() {
        return new ForbiddenException(
                "Cette ressource ne vous appartient pas",
                "NOT_RESOURCE_OWNER"
        );
    }

    /**
     * Exception pour abonnement requis
     */
    public static ForbiddenException subscriptionRequired() {
        return new ForbiddenException(
                "Cette fonctionnalité nécessite un abonnement actif",
                "SUBSCRIPTION_REQUIRED"
        );
    }

    /**
     * Exception pour compte non validé par admin
     */
    public static ForbiddenException accountNotValidated() {
        return new ForbiddenException(
                "Votre compte est en attente de validation par un administrateur",
                "ACCOUNT_NOT_VALIDATED"
        );
    }
}