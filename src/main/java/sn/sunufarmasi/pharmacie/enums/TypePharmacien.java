package sn.sunufarmasi.pharmacie.enums;

/**
 * Type de pharmacien
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum TypePharmacien {
    /**
     * Propriétaire de la pharmacie
     * - Tous les droits
     * - Peut créer des employés
     * - Gère l'abonnement
     */
    PROPRIETAIRE,

    /**
     * Gérant salarié de la pharmacie
     * - Droits étendus
     * - Ne peut pas modifier l'abonnement
     */
    GERANT,

    /**
     * Pharmacien remplaçant temporaire
     * - Droits limités
     * - Accès temporaire
     */
    REMPLACANT
}