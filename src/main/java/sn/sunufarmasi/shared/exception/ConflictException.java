package sn.sunufarmasi.shared.exception;

/**
 * Exception levée en cas de conflit (ex: email/téléphone déjà existant)
 * HTTP Status: 409 CONFLICT
 *
 * @author WeCan
 * @since 1.0.0
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}