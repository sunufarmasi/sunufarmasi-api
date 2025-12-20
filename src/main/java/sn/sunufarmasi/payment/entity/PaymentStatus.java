package sn.sunufarmasi.payment.entity;

/**
 * Statuts d'un paiement
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum PaymentStatus {
    /**
     * Paiement en attente de confirmation
     */
    PENDING,

    /**
     * Paiement réussi
     */
    SUCCESS,

    /**
     * Paiement échoué
     */
    FAILED,

    /**
     * Paiement annulé
     */
    CANCELLED,

    /**
     * Paiement remboursé
     */
    REFUNDED
}