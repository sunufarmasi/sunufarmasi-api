package sn.sunufarmasi.subscription.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO Response pour SubscriptionPlan
 *
 * @author WeCan
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubscriptionPlanResponse(
        String id,
        String code,
        String nom,
        String description,
        Integer prix,
        Integer dureeJours,
        boolean avecPublicite,
        boolean actif,
        String badge,  // "RECOMMANDÉ", "ÉCONOMIE", etc.
        String prixFormate  // "750 FCFA/mois"
) {
    /**
     * Constructeur avec formatage
     */
    public static SubscriptionPlanResponse from(
            String id, String code, String nom, String description,
            Integer prix, Integer dureeJours, boolean avecPublicite, boolean actif
    ) {
        String badge = null;
        String prixFormate = null;

        if ("ANNUAL".equals(code)) {
            badge = "RECOMMANDÉ - Économie 1000 FCFA";
            prixFormate = prix + " FCFA/an";
        } else if ("MONTHLY".equals(code)) {
            prixFormate = prix + " FCFA/mois";
        } else if ("FREE_TRIAL".equals(code)) {
            badge = "15 JOURS GRATUITS";
            prixFormate = "Gratuit";
        }

        return new SubscriptionPlanResponse(
                id, code, nom, description, prix, dureeJours,
                avecPublicite, actif, badge, prixFormate
        );
    }
}