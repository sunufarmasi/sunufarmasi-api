package sn.sunufarmasi.notification.dto;

import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO pour la configuration des notifications
 */
public record ConfigNotificationDTO(
        UUID id,
        UUID userId,

        // Canaux
        Boolean appActive,
        Boolean emailActive,
        Boolean smsActive,
        Boolean whatsappActive,
        Boolean pushActive,

        // Types
        Boolean notifStock,
        Boolean notifCommande,
        Boolean notifMutuelle,
        Boolean notifGarde,
        Boolean notifVente,
        Boolean notifSysteme,

        // Silence
        LocalTime heureDebutSilence,
        LocalTime heureFinSilence,
        Boolean silenceWeekend,

        // Résumé
        Boolean resumeQuotidien,
        LocalTime heureResume,
        Boolean resumeHebdomadaire,
        Integer jourResume,

        // Seuils
        Boolean seuilRuptureEmail,
        Boolean seuilRuptureSms,
        Integer joursAvantPeremption
) {}
