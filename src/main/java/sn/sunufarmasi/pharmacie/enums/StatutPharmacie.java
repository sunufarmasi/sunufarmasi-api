package sn.sunufarmasi.pharmacie.enums;

/**
 * Statut d'une pharmacie dans le système
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum StatutPharmacie {
    /**
     * Inscription effectuée, en attente de validation par le syndicat
     */
    EN_ATTENTE,

    /**
     * Validée par le syndicat, peut utiliser la plateforme
     */
    VALIDEE,

    /**
     * Active et visible publiquement (statut opérationnel normal)
     */
    ACTIVE,

    /**
     * Rejetée par le syndicat (documents non conformes)
     */
    REJETEE,

    /**
     * Suspendue temporairement (non-paiement, problème, etc.)
     */
    SUSPENDUE,

    /**
     * Fermée définitivement
     */
    FERMEE
}