package sn.sunufarmasi.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO Record pour les réponses paginées
 * Alternative simplifiée pour retourner des listes paginées
 *
 * @param <T> Type des éléments de la liste
 * @author WeCan
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious,
        boolean isFirst,
        boolean isLast
) {

    /**
     * Créer PageResponse depuis Spring Data Page
     *
     * @param page Page Spring Data
     * @return PageResponse
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
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

    /**
     * Créer PageResponse vide
     *
     * @return PageResponse vide
     */
    public static <T> PageResponse<T> empty() {
        return new PageResponse<>(
                List.of(),
                0,
                0,
                0,
                0,
                false,
                false,
                true,
                true
        );
    }
}