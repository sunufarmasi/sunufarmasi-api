package sn.sunufarmasi.shared.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import sn.sunufarmasi.shared.dto.ApiResponse;
import sn.sunufarmasi.shared.dto.ErrorResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global des exceptions pour l'API PharmaGo
 * Intercepte toutes les exceptions et retourne des réponses JSON standardisées
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Gestion des exceptions métier (ApiException)
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(
            ApiException ex,
            WebRequest request
    ) {
        log.error("API Exception: {} - {}", ex.getErrorCode(), ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );

        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                errorResponse
        );

        return new ResponseEntity<>(response, ex.getStatus());
    }

    /**
     * Gestion des erreurs de validation (@Valid, @Validated)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_ERROR",
                "Erreur de validation des données",
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );

        ApiResponse<Map<String, String>> response = new ApiResponse<>(
                false,
                "Erreur de validation des données",
                errors,
                errorResponse,
                null
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gestion des erreurs de contraintes de validation (@NotNull, @Size, etc.)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
            ConstraintViolationException ex,
            WebRequest request
    ) {
        log.warn("Constraint violation: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(propertyPath, message);
        });

        ErrorResponse errorResponse = new ErrorResponse(
                "CONSTRAINT_VIOLATION",
                "Violation de contrainte de validation",
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );

        ApiResponse<Map<String, String>> response = new ApiResponse<>(
                false,
                "Violation de contrainte de validation",
                errors,
                errorResponse,
                null
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gestion des erreurs de type de paramètre
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            WebRequest request
    ) {
        log.warn("Type mismatch: {} for parameter {}", ex.getValue(), ex.getName());

        String message = String.format(
                "Le paramètre '%s' doit être de type '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );

        ErrorResponse errorResponse = new ErrorResponse(
                "TYPE_MISMATCH",
                message,
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );

        ApiResponse<Void> response = ApiResponse.error(message, errorResponse);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gestion des erreurs d'authentification Spring Security
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex,
            WebRequest request
    ) {
        log.error("Authentication error: {}", ex.getMessage());

        String message = "Erreur d'authentification";
        String errorCode = "AUTHENTICATION_ERROR";

        if (ex instanceof BadCredentialsException) {
            message = "Email ou mot de passe incorrect";
            errorCode = "INVALID_CREDENTIALS";
        }

        ErrorResponse errorResponse = new ErrorResponse(
                errorCode,
                message,
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );

        ApiResponse<Void> response = ApiResponse.error(message, errorResponse);

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Gestion des erreurs d'accès refusé Spring Security
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request
    ) {
        log.error("Access denied: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "ACCESS_DENIED",
                "Accès refusé. Vous n'avez pas les permissions nécessaires",
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );

        ApiResponse<Void> response = ApiResponse.error(
                "Accès refusé",
                errorResponse
        );

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * Gestion des exceptions non gérées
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(
            Exception ex,
            WebRequest request
    ) {
        log.error("Unhandled exception: ", ex);

        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_ERROR",
                "Une erreur interne s'est produite. Veuillez réessayer plus tard",
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );

        ApiResponse<Void> response = ApiResponse.error(
                "Erreur interne du serveur",
                errorResponse
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Gestion des IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request
    ) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "ILLEGAL_ARGUMENT",
                ex.getMessage(),
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );

        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                errorResponse
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gestion des NullPointerException
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Void>> handleNullPointerException(
            NullPointerException ex,
            WebRequest request
    ) {
        log.error("Null pointer exception: ", ex);

        ErrorResponse errorResponse = new ErrorResponse(
                "NULL_POINTER",
                "Une valeur requise est manquante",
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );

        ApiResponse<Void> response = ApiResponse.error(
                "Erreur de traitement des données",
                errorResponse
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}