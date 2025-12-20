package sn.sunufarmasi.pharmacie.exception;

/**
 * Exception levée quand une pharmacie existe déjà
 *
 * @author WeCan
 * @since 1.0.0
 */
public class PharmacieAlreadyExistsException extends RuntimeException {

    public PharmacieAlreadyExistsException(String message) {
        super(message);
    }

    public PharmacieAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}