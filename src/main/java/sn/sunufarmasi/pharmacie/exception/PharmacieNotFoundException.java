package sn.sunufarmasi.pharmacie.exception;

/**
 * Exception levée quand une pharmacie n'est pas trouvée
 *
 * @author WeCan
 * @since 1.0.0
 */
public class PharmacieNotFoundException extends RuntimeException {

    public PharmacieNotFoundException(String message) {
        super(message);
    }

    public PharmacieNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}