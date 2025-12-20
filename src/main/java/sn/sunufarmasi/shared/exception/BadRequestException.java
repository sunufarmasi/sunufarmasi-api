package sn.sunufarmasi.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception levée lorsqu'une requête est invalide ou mal formée
 * Retourne un code HTTP 400 (BAD_REQUEST)
 *
 * @author AL Amine
 * @since 1.0.0
 */
public class BadRequestException extends ApiException {

    /**
     * Constructeur avec message simple
     *
     * @param message Message d'erreur
     */
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }

    /**
     * Constructeur avec code erreur personnalisé
     *
     * @param message Message d'erreur
     * @param errorCode Code erreur métier
     */
    public BadRequestException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }

    /**
     * Constructeur avec cause
     *
     * @param message Message d'erreur
     * @param cause Exception d'origine
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST", cause);
    }

    /**
     * Exception pour champ invalide
     *
     * @param fieldName Nom du champ
     * @param message Message d'erreur
     */
    public static BadRequestException invalidField(String fieldName, String message) {
        return new BadRequestException(
                String.format("Champ '%s' invalide : %s", fieldName, message),
                "INVALID_FIELD"
        );
    }

    /**
     * Exception pour valeur manquante
     *
     * @param fieldName Nom du champ
     */
    public static BadRequestException missingField(String fieldName) {
        return new BadRequestException(
                String.format("Le champ '%s' est obligatoire", fieldName),
                "MISSING_FIELD"
        );
    }

    /**
     * Exception pour valeur dupliquée
     *
     * @param fieldName Nom du champ
     * @param value Valeur en double
     */
    public static BadRequestException duplicateValue(String fieldName, Object value) {
        return new BadRequestException(
                String.format("La valeur '%s' pour le champ '%s' existe déjà", value, fieldName),
                "DUPLICATE_VALUE"
        );
    }
}