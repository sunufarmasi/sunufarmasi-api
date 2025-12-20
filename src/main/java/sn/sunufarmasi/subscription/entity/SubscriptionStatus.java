package sn.sunufarmasi.subscription.entity;

/**
 * Statuts d'un abonnement
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum SubscriptionStatus {
    /**
     * Abonnement actif et valide
     */
    ACTIVE,

    /**
     * Abonnement expiré
     */
    EXPIRED,

    /**
     * Abonnement annulé par l'utilisateur
     */
    CANCELLED,

    /**
     * Abonnement suspendu (par admin)
     */
    SUSPENDED,

    /**
     * En attente de paiement
     */
    PENDING_PAYMENT
}