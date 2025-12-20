package sn.sunufarmasi.notification.dto;

import sn.sunufarmasi.notification.enums.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO Response pour une notification
 */
public record NotificationResponse(
        UUID id,
        TypeNotification type,
        String typeLibelle,
        String categorie,
        String niveau,
        CanalNotification canal,
        StatutNotification statut,
        String titre,
        String message,
        String lienAction,
        String icone,
        String referenceType,
        UUID referenceId,
        String referenceNumero,
        Boolean estLu,
        LocalDateTime dateLecture,
        LocalDateTime dateCreation,
        LocalDateTime dateEnvoi,
        Integer priorite
) {}
