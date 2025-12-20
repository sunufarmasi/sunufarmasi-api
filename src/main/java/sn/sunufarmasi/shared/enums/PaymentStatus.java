package sn.sunufarmasi.shared.enums;

/**
 * Enum représentant les statuts de paiement
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum PaymentStatus {
    /**
     * Paiement en attente
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
     * Remboursé
     */
    REFUNDED
}