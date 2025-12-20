package sn.sunufarmasi.subscription.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import sn.sunufarmasi.subscription.entity.SubscriptionStatus;

import java.time.LocalDateTime;

/**
 * DTO Response pour Subscription
 *
 * @author WeCan
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubscriptionResponse(
        String id,
        SubscriptionPlanResponse plan,
        SubscriptionStatus status,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime startsAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime expiresAt,

        boolean autoRenew,
        boolean isTrial,
        boolean isActive,
        boolean isExpired,
        boolean isExpiringSoon,
        Long daysRemaining,
        String message  // "Expire dans 3 jours", etc.
) {
    /**
     * Générer un message utilisateur
     */
    public static String generateMessage(boolean isActive, boolean isExpired,
                                         boolean isExpiringSoon, Long daysRemaining,
                                         boolean isTrial) {
        if (isExpired) {
            return isTrial ?
                    "Votre essai gratuit a expiré. Abonnez-vous pour continuer !" :
                    "Votre abonnement a expiré. Renouvelez pour continuer !";
        }

        if (isExpiringSoon) {
            return "Expire dans " + daysRemaining + " jour(s)";
        }

        if (isActive && isTrial) {
            return "Période d'essai - " + daysRemaining + " jour(s) restant(s)";
        }

        if (isActive) {
            return "Abonnement actif - " + daysRemaining + " jour(s) restant(s)";
        }

        return null;
    }
}