package sn.sunufarmasi.shared.exception;

/**
 * Exception levée en cas de trop de requêtes (rate limiting)
 * HTTP Status: 429 TOO MANY REQUESTS
 *
 * @author WeCan
 * @since 1.0.0
 */
public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException(String message) {
        super(message);
    }

    public TooManyRequestsException(String message, Throwable cause) {
        super(message, cause);
    }
}