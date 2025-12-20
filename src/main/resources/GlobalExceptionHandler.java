//package sn.sunufarmasi.pharmacie.exception;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Gestionnaire global des exceptions
// *
// * @author WeCan
// * @since 1.0.0
// */
//@RestControllerAdvice
//@Slf4j
//public class GlobalExceptionHandler {
//
//    /**
//     * Erreur de validation des données (DTO)
//     */
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
//        log.error("Erreur de validation: {}", ex.getMessage());
//
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach(error -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//
//        ErrorResponse response = new ErrorResponse(
//                HttpStatus.BAD_REQUEST.value(),
//                "Erreur de validation",
//                errors,
//                LocalDateTime.now()
//        );
//
//        return ResponseEntity.badRequest().body(response);
//    }
//
//    /**
//     * Pharmacien non trouvé
//     */
//    @ExceptionHandler(PharmacienNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handlePharmacienNotFoundException(PharmacienNotFoundException ex) {
//        log.error("Pharmacien non trouvé: {}", ex.getMessage());
//
//        ErrorResponse response = new ErrorResponse(
//                HttpStatus.NOT_FOUND.value(),
//                ex.getMessage(),
//                null,
//                LocalDateTime.now()
//        );
//
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//    }
//
//    /**
//     * Pharmacien existe déjà
//     */
//    @ExceptionHandler(PharmacienAlreadyExistsException.class)
//    public ResponseEntity<ErrorResponse> handlePharmacienAlreadyExistsException(PharmacienAlreadyExistsException ex) {
//        log.error("Pharmacien existe déjà: {}", ex.getMessage());
//
//        ErrorResponse response = new ErrorResponse(
//                HttpStatus.CONFLICT.value(),
//                ex.getMessage(),
//                null,
//                LocalDateTime.now()
//        );
//
//        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
//    }
//
//    /**
//     * Pharmacie non trouvée
//     */
//    @ExceptionHandler(PharmacieNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handlePharmacieNotFoundException(PharmacieNotFoundException ex) {
//        log.error("Pharmacie non trouvée: {}", ex.getMessage());
//
//        ErrorResponse response = new ErrorResponse(
//                HttpStatus.NOT_FOUND.value(),
//                ex.getMessage(),
//                null,
//                LocalDateTime.now()
//        );
//
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//    }
//
//    /**
//     * Pharmacie existe déjà
//     */
//    @ExceptionHandler(PharmacieAlreadyExistsException.class)
//    public ResponseEntity<ErrorResponse> handlePharmacieAlreadyExistsException(PharmacieAlreadyExistsException ex) {
//        log.error("Pharmacie existe déjà: {}", ex.getMessage());
//
//        ErrorResponse response = new ErrorResponse(
//                HttpStatus.CONFLICT.value(),
//                ex.getMessage(),
//                null,
//                LocalDateTime.now()
//        );
//
//        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
//    }
//
//    /**
//     * Exception métier générale
//     */
//    @ExceptionHandler(BusinessException.class)
//    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
//        log.error("Erreur métier: {}", ex.getMessage());
//
//        ErrorResponse response = new ErrorResponse(
//                HttpStatus.BAD_REQUEST.value(),
//                ex.getMessage(),
//                null,
//                LocalDateTime.now()
//        );
//
//        return ResponseEntity.badRequest().body(response);
//    }
//
//    /**
//     * Erreur interne du serveur
//     */
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
//        log.error("Erreur interne: ", ex);
//
//        ErrorResponse response = new ErrorResponse(
//                HttpStatus.INTERNAL_SERVER_ERROR.value(),
//                "Une erreur interne est survenue",
//                null,
//                LocalDateTime.now()
//        );
//
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//    }
//
//    /**
//     * DTO de réponse d'erreur
//     */
//    public record ErrorResponse(
//            int status,
//            String message,
//            Map<String, String> errors,
//            LocalDateTime timestamp
//    ) {}
//}