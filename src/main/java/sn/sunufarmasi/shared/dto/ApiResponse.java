package sn.sunufarmasi.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO Record générique pour toutes les réponses de l'API PharmaGo
 * Structure standardisée : success, message, data, error, metadata
 *
 * @param <T> Type de données retournées
 * @author AL Amine
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        ErrorResponse error,
        MetadataResponse metadata
) {

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTEURS STATIQUES POUR SUCCÈS
    // ═══════════════════════════════════════════════════════════

    /**
     * Réponse de succès simple avec données
     *
     * @param data Données à retourner
     * @return ApiResponse avec success=true
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, null, data, null, null);
    }

    /**
     * Réponse de succès avec message et données
     *
     * @param message Message de succès
     * @param data Données à retourner
     * @return ApiResponse avec success=true
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null, null);
    }

    /**
     * Réponse de succès avec message seulement (sans données)
     *
     * @param message Message de succès
     * @return ApiResponse avec success=true
     */
    public static <T> ApiResponse<T> successMessage(String message) {
        return new ApiResponse<>(true, message, null, null, null);
    }

    /**
     * Réponse de succès avec données et pagination
     *
     * @param data Données à retourner
     * @param pagination Informations de pagination
     * @return ApiResponse avec success=true et metadata
     */
    public static <T> ApiResponse<T> successWithPagination(T data, PaginationResponse pagination) {
        MetadataResponse metadata = new MetadataResponse(pagination);
        return new ApiResponse<>(true, null, data, null, metadata);
    }

    /**
     * Réponse de succès complète
     *
     * @param message Message de succès
     * @param data Données à retourner
     * @param pagination Informations de pagination
     * @return ApiResponse avec success=true
     */
    public static <T> ApiResponse<T> success(String message, T data, PaginationResponse pagination) {
        MetadataResponse metadata = new MetadataResponse(pagination);
        return new ApiResponse<>(true, message, data, null, metadata);
    }

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTEURS STATIQUES POUR ERREURS
    // ═══════════════════════════════════════════════════════════

    /**
     * Réponse d'erreur simple
     *
     * @param message Message d'erreur
     * @return ApiResponse avec success=false
     */
    public static <T> ApiResponse<T> error(String message) {
        ErrorResponse error = new ErrorResponse(message);
        return new ApiResponse<>(false, message, null, error, null);
    }

    /**
     * Réponse d'erreur avec code
     *
     * @param message Message d'erreur
     * @param errorCode Code d'erreur
     * @return ApiResponse avec success=false
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        ErrorResponse error = new ErrorResponse(
                errorCode,
                message,
                java.time.LocalDateTime.now(),
                null
        );
        return new ApiResponse<>(false, message, null, error, null);
    }

    /**
     * Réponse d'erreur complète avec ErrorResponse
     *
     * @param message Message d'erreur
     * @param errorResponse Détails de l'erreur
     * @return ApiResponse avec success=false
     */
    public static <T> ApiResponse<T> error(String message, ErrorResponse errorResponse) {
        return new ApiResponse<>(false, message, null, errorResponse, null);
    }

    /**
     * Réponse d'erreur avec données (pour erreurs de validation par exemple)
     *
     * @param message Message d'erreur
     * @param data Données d'erreur (ex: champs invalides)
     * @param errorResponse Détails de l'erreur
     * @return ApiResponse avec success=false
     */
    public static <T> ApiResponse<T> error(String message, T data, ErrorResponse errorResponse) {
        return new ApiResponse<>(false, message, data, errorResponse, null);
    }

    // ═══════════════════════════════════════════════════════════
    // NESTED RECORDS
    // ═══════════════════════════════════════════════════════════

    /**
     * Record pour les métadonnées de réponse (pagination, etc.)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MetadataResponse(
            PaginationResponse pagination
    ) {}

    /**
     * Record pour les informations de pagination
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PaginationResponse(
            int currentPage,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious,
            boolean isFirst,
            boolean isLast
    ) {
        /**
         * Constructeur simplifié depuis Spring Data Page
         */
        public static PaginationResponse from(org.springframework.data.domain.Page<?> page) {
            return new PaginationResponse(
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.hasNext(),
                    page.hasPrevious(),
                    page.isFirst(),
                    page.isLast()
            );
        }
    }
}