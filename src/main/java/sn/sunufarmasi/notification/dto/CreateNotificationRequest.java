package sn.sunufarmasi.notification.dto;

import jakarta.validation.constraints.*;
import sn.sunufarmasi.notification.enums.*;

import java.util.List;
import java.util.UUID;

/**
 * DTO pour créer une notification
 */
public record CreateNotificationRequest(

        @NotNull TypeNotification type,
        @NotNull CanalNotification canal,

        UUID destinataireId,
        String destinataireEmail,
        String destinataireTelephone,

        @NotBlank @Size(max = 200) String titre,
        @NotBlank @Size(max = 2000) String message,
        @Size(max = 160) String messageCourt,

        String lienAction,
        String icone,

        String referenceType,
        UUID referenceId,
        String referenceNumero,

        @Min(1) @Max(10) Integer priorite

) {}
