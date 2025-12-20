package sn.sunufarmasi.pharmacie.exception;

/**
 * Exception métier générale
 *
 * @author WeCan
 * @since 1.0.0
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}