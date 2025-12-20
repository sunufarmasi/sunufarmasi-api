package sn.sunufarmasi.pharmacie.exception;

/**
 * Exception levée quand un pharmacien n'est pas trouvé
 *
 * @author WeCan
 * @since 1.0.0
 */
public class PharmacienNotFoundException extends RuntimeException {

    public PharmacienNotFoundException(String message) {
        super(message);
    }

    public PharmacienNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}