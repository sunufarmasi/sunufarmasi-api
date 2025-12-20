package sn.sunufarmasi.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception de base pour toutes les exceptions métier de l'API PharmaGo
 *
 * @author AL Amine
 * @since 1.0.0
 */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    /**
     * Constructeur complet
     *
     * @param message Message d'erreur
     * @param status Code HTTP
     * @param errorCode Code erreur métier
     */
    public ApiException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    /**
     * Constructeur avec message et status
     *
     * @param message Message d'erreur
     * @param status Code HTTP
     */
    public ApiException(String message, HttpStatus status) {
        this(message, status, status.name());
    }

    /**
     * Constructeur avec cause
     *
     * @param message Message d'erreur
     * @param status Code HTTP
     * @param errorCode Code erreur métier
     * @param cause Exception d'origine
     */
    public ApiException(String message, HttpStatus status, String errorCode, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }
}