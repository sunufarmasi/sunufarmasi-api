package sn.sunufarmasi.pharmacie.exception;

/**
 * Exception levée quand un pharmacien existe déjà
 *
 * @author WeCan
 * @since 1.0.0
 */
public class PharmacienAlreadyExistsException extends RuntimeException {

    public PharmacienAlreadyExistsException(String message) {
        super(message);
    }

    public PharmacienAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}