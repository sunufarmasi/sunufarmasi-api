package sn.sunufarmasi.shared.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO Record pour les détails d'une erreur
 * Utilisé dans ApiResponse pour fournir des informations détaillées sur les erreurs
 *
 * @author AL Amine
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String code,
        String message,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,

        String path
) {
    /**
     * Constructeur simplifié sans path
     */
    public ErrorResponse(String code, String message, LocalDateTime timestamp) {
        this(code, message, timestamp, null);
    }

    /**
     * Constructeur avec message seulement
     */
    public ErrorResponse(String message) {
        this("ERROR", message, LocalDateTime.now(), null);
    }
}