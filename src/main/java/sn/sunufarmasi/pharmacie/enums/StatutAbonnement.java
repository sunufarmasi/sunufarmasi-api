package sn.sunufarmasi.pharmacie.enums;

/**
 * Statut d'un abonnement (pharmacie ou syndicat)
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum StatutAbonnement {
    /**
     * En attente de premier paiement
     */
    EN_ATTENTE,

    /**
     * Abonnement actif et payé
     */
    ACTIF,

    /**
     * Abonnement expiré (non-renouvellement)
     */
    EXPIRE,

    /**
     * Abonnement suspendu (non-paiement, problème)
     */
    SUSPENDU,

    /**
     * Abonnement annulé par l'utilisateur
     */
    ANNULE
}