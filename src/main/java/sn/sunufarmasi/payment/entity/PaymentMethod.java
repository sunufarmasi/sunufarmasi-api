package sn.sunufarmasi.payment.entity;

/**
 * Méthodes de paiement disponibles
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum PaymentMethod {
    /**
     * Paiement via Orange Money
     */
    ORANGE_MONEY,

    /**
     * Paiement via Wave
     */
    WAVE,

    /**
     * Paiement via Free Money (Tigo)
     */
    FREE_MONEY,

    /**
     * Paiement en espèces (à valider manuellement)
     */
    CASH
}